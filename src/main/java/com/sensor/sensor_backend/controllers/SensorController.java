package com.sensor.sensor_backend.controllers;

import com.sensor.sensor_backend.model.Sensor;
import com.sensor.sensor_backend.model.SensorNeighborDTO;
import com.sensor.sensor_backend.model.SensorReadings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sensors")
public class SensorController {

    private List<Sensor> sensors = new ArrayList<>();
    private List<SensorReadings> sensorReadings = new ArrayList<>();

    @PostMapping("/register")
    public ResponseEntity<Void> registriraj(@RequestBody Sensor sensor) {

        sensors.add(sensor);
        Long newSensorId = sensor.getId();

        String sensorUri = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/sensors/{id}")
                .buildAndExpand(newSensorId)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", sensorUri);

        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }
    @GetMapping
    public List<Sensor> getAllSensors() {
        return sensors;
    }


    @GetMapping("/{id}")
    public ResponseEntity<Sensor> getSensorById(@PathVariable Long id) {
        Sensor sensor = sensors.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (sensor == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(sensor, HttpStatus.OK);
    }

    @GetMapping("/{id}/closest-neighbor")
    public ResponseEntity<SensorNeighborDTO> getClosestNeighbor(@PathVariable Long id) {
        Sensor currentSensor = sensors.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElse(null);


        if (currentSensor == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Sensor closestNeighbor = sensors.stream()
                .filter(s -> !s.getId().equals(id))
                .min((s1, s2) -> {
                    double distanceToS1 = calculateDistance(currentSensor, s1);
                    double distanceToS2 = calculateDistance(currentSensor, s2);

                    System.out.println("Comparing distances:");
                    System.out.println("Distance from " + currentSensor.getName() + " to " + s1.getName() + " is " + distanceToS1);
                    System.out.println("Distance from " + currentSensor.getName() + " to " + s2.getName() + " is " + distanceToS2);

                    return Double.compare(distanceToS1, distanceToS2);
                })
                .orElse(null);


        System.out.println("closest neighbor to sensor " + currentSensor + ": " + closestNeighbor);

        if (closestNeighbor == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        SensorNeighborDTO neighborDTO = new SensorNeighborDTO(
                closestNeighbor.getLatitude(),
                closestNeighbor.getLongitude(),
                closestNeighbor.getIp(),
                closestNeighbor.getPort()
        );

        return new ResponseEntity<>(neighborDTO, HttpStatus.OK);
    }

    private double calculateDistance(Sensor s1, Sensor s2) {
        final int R = 6371;

        double latDistance = Math.toRadians(s2.getLatitude() - s1.getLatitude());
        double lonDistance = Math.toRadians(s2.getLongitude() - s1.getLongitude());

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(s1.getLatitude())) * Math.cos(Math.toRadians(s2.getLatitude()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }



}
