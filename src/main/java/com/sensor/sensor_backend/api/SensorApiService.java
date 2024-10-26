package com.sensor.sensor_backend.api;

import com.sensor.sensor_backend.model.Sensor;
import com.sensor.sensor_backend.model.SensorDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SensorApiService {

    @POST("/api/sensors/register")
    Call<Void> registerSensor(@Body SensorDTO sensorDTO);

    @GET("/api/sensors/{id}/closest-neighbor")
    Call<SensorDTO> getClosestNeighbor(@Path("id") Long sensorId);

    @GET("/api/sensors/{id}")
    Call<Sensor> getSensorById(@Path("id") Long id);

}
