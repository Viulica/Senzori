package com.sensor.sensor_backend.model;
import com.sensor.grpc.SensorGrpc;
import com.sensor.grpc.SensorServiceGrpc;
import com.sensor.sensor_backend.api.SensorApiService;
import com.sensor.sensor_backend.service.ApiClient;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Math.abs;


public class Sensor {
    private final LocalDateTime startTime;
    private Long id;
    private String name;
    private double latitude;
    private double longitude;
    private static final String CSV_FILE_PATH = "src/main/resources/readings.csv";
    private String ip;
    private int port;
    private transient SensorApiService sensorApiService;
    private transient List<SensorReadings> ocitanja;
    private SensorService sensorService = SensorService.getInstance();
    private Server grpcServer;
    private int grpcPort;



    private SensorApiService getSensorApiService() {
        if (this.sensorApiService == null) {
            this.sensorApiService = ApiClient.getSensorApiService();
        }
        return this.sensorApiService;
    }

    public Sensor(Long id, String name, String ip, int port) {
        this.id = id;
        this.name = name;
        this.startTime = LocalDateTime.now();
        this.ip = ip;
        this.port = port;
        this.ocitanja = new ArrayList<>();
        lokacija();
    }


    public void startGeneratingReadings() {
        // System.out.println("Starting generating readings for sensor: " + name);
            while (true) {
                SensorReadings reading = generirajOcitavanje();
                // System.out.println("Generirano očitanje za senzor " + this.getName() + ": " + reading);

                SensorNeighborDTO closestNeighbor = requestClosestNeighbor();

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
    }


    private void lokacija() {
        Random random = new Random();
        this.latitude = 45.75 + (random.nextDouble() * (45.85 - 45.75));
        this.longitude = 15.87 + (random.nextDouble() * (16 - 15.87));
    }

    public void registerSensorAsync() {
        new Thread(() -> {
            register();
            try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
            startGeneratingReadings();

        }).start();
    }


    public void register() {
        SensorApiService service = getSensorApiService();
        Call<Void> call = service.registerSensor(this);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    if (response.code() == 201) {
                        String locationHeader = response.headers().get("Location");
                        System.out.println("Sensor registered successfully. Location: " + locationHeader);

                        try {
                            setGrpcPort(startGrpcServer(Sensor.this));
                            System.out.println("Sensor grpc server started on port " + getGrpcPort());
                        } catch (IOException e) {
                            System.out.println("Failed to start gRPC server: " + e.getMessage());
                        }

                        sensorService.addSensor(Sensor.this.getPort(), Sensor.this.getId(), Sensor.this.getName());
                        System.out.println("sensor " + name + " start time: " + startTime);

                    } else {
                        System.out.println("Unexpected status code: " + response.code());
                    }
                } else {
                    System.out.println("Failed to register sensor. HTTP Status: " + response.code());
                    System.exit(1);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                System.out.println("Error during sensor registration: " + t.getMessage());
            }
        });
    }


    public static String[] getReading(int rowIndex) {
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
            String line;
            int currentRow = 0;

            while ((line = br.readLine()) != null) {
                if (currentRow == rowIndex) {
                    return line.split(",");
                }
                currentRow++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public SensorReadings generirajOcitavanje() {

        // System.out.println("generiram ocitavanja za senzor" + name);

        long brojAktivnihSekundi = abs(ChronoUnit.SECONDS.between(startTime, LocalDateTime.now()));
        // System.out.println("broj aktivnih sekundi za senzor " + name + " je: " + brojAktivnihSekundi);
        brojAktivnihSekundi++;
        int row = (int) ((brojAktivnihSekundi % 100) + 1);
        // System.out.println("Row chosen for sensor  " + name + ": " + row);

        String[] readings = getReading(row);

        if (readings == null) {
            throw new IllegalArgumentException("Ne mogu pronaći očitanje za redak " + row);
        }

        double temperature = Double.parseDouble(readings[0]);
        double pressure = Double.parseDouble(readings[1]);
        double humidity = Double.parseDouble(readings[2]);

        Double co = (readings[3] != null && !readings[3].isEmpty()) ? Double.parseDouble(readings[3]) : null;
        Double no2 = (readings[4] != null && !readings[4].isEmpty()) ? Double.parseDouble(readings[4]) : null;
        Double so2 = (readings.length > 5 && readings[5] != null && !readings[5].isEmpty()) ? Double.parseDouble(readings[5]) : null;


        SensorReadings newReading = new SensorReadings(temperature, pressure, humidity, co, no2, so2, this);
        ocitanja.add(newReading);

        return newReading;
    }

    public SensorNeighborDTO requestClosestNeighbor() {
        SensorApiService sensorApiService = ApiClient.getSensorApiService();

        Call<SensorNeighborDTO> call = sensorApiService.getClosestNeighbor(this.id);

        try {
            Response<SensorNeighborDTO> response = call.execute();
            if (response.isSuccessful() && response.body() != null) {

                SensorNeighborDTO closestNeighbor = response.body();
                Long id = sensorService.getSensorByPort(closestNeighbor.getPort()).getId();
                String name = sensorService.getSensorByPort(closestNeighbor.getPort()).getName();

                Sensor s = new Sensor(id, name, closestNeighbor.getIp(), closestNeighbor.getPort());
                sendGrpcRequestToNeighbor(s);

                return closestNeighbor;
            } else {
                System.out.println("Failed to get closest neighbor: " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int startGrpcServer(Sensor sensor) throws IOException {
        grpcServer = ServerBuilder.forPort(0)
                .addService(new SensorServiceImpl(sensor))
                .build()
                .start();

        int assignedPort = grpcServer.getPort();
        System.out.println("gRPC server for sensor " + sensor.getName() + " started on port " + assignedPort);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down gRPC server for sensor: " + sensor.getName());
            stopGrpcServer();
        }));

        return assignedPort;
    }

    public void stopGrpcServer() {
        if (grpcServer != null) {
            grpcServer.shutdown();
        }
    }

    private void sendGrpcRequestToNeighbor(Sensor sensor) throws IOException {
        int serverPort = 0;
        while (sensor.getGrpcPort() == 0) {
            try {
                Thread.sleep(100);
                serverPort = sensor.getGrpcPort();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        ManagedChannel channel = ManagedChannelBuilder.forAddress(sensor.getIp(), serverPort)
                .usePlaintext()
                .build();

        SensorServiceGrpc.SensorServiceBlockingStub stub = SensorServiceGrpc.newBlockingStub(channel);

        SensorGrpc.SensorIdRequest request = SensorGrpc.SensorIdRequest.newBuilder()
                .setId(this.id)
                .build();

        try {
            SensorGrpc.SensorReadingsResponse response = stub.requestReadings(request);
            System.out.println("gRPC odgovor od susjeda: " + response.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            channel.shutdown();
        }
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getInfo() {
        return this.id + this.name + this.ip + this.port;
    }


    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }
    public List<SensorReadings> getOcitanja() {
        return ocitanja;
    }

    public void setOcitanja(List<SensorReadings> ocitanja) {
        this.ocitanja = ocitanja;
    }

    public int getGrpcPort() {
        return grpcPort;
    }

    public void setGrpcPort(int grpcPort) {
        this.grpcPort = grpcPort;
    }

    public int getPort() {
        return port;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Sensor{id=" + id + ", name='" + name + "'}";
    }

    static class SensorServiceImpl extends SensorServiceGrpc.SensorServiceImplBase {

        private final Sensor sensor;

        public SensorServiceImpl(Sensor sensor) {
            this.sensor = sensor;
        }

        @Override
        public void requestReadings(SensorGrpc.SensorIdRequest request, StreamObserver<SensorGrpc.SensorReadingsResponse> responseObserver) {
            System.out.println("Received gRPC request for readings from sensor ID: " + request.getId());

            SensorReadings lastReading = sensor.ocitanja.isEmpty() ? null : sensor.ocitanja.get(sensor.ocitanja.size() - 1);
            String message = (lastReading != null) ? "Returning last reading: " + lastReading.toString() : "No readings available";

            SensorGrpc.SensorReadingsResponse response = SensorGrpc.SensorReadingsResponse.newBuilder()
                    .setSuccess(lastReading != null)
                    .setMessage(message)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void sendReadings(SensorGrpc.SensorReadingsRequest request, StreamObserver<SensorGrpc.SensorReadingsResponse> responseObserver) {
            System.out.println("Received readings from sensor ID: " + request.getId());

            String message = "Readings received: " + request.getTemperature() + "°C, " + request.getPressure() + " Pa";
            System.out.println(message);

            SensorGrpc.SensorReadingsResponse response = SensorGrpc.SensorReadingsResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Readings received successfully")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

}


