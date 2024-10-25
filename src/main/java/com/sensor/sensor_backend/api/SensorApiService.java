package com.sensor.sensor_backend.api;

import com.sensor.sensor_backend.model.Sensor;
import com.sensor.sensor_backend.model.SensorNeighborDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SensorApiService {

    // Registracija senzora
    @POST("/api/sensors/register")
    Call<Void> registerSensor(@Body Sensor sensor);

    @GET("/api/sensors/{id}/closest-neighbor")
    Call<SensorNeighborDTO> getClosestNeighbor(@Path("id") Long sensorId);

}
