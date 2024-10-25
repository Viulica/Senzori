package com.sensor.sensor_backend.client;
import com.sensor.sensor_backend.model.Sensor;

import java.io.IOException;


public class Main {
    public static void main(String[] args) throws IOException {

        Sensor sensor1 = new Sensor(1L, "Sensor 1", "127.0.0.1", 8080);
        Sensor sensor2 = new Sensor(2L, "Sensor 2", "127.0.0.1", 8081);
        Sensor sensor3 = new Sensor(3L, "Sensor 3", "127.0.0.1", 8082);

        sensor1.registerSensorAsync();
        try {
        Thread.sleep(2000);
        } catch (InterruptedException e) {
        e.printStackTrace();
    }
        sensor2.registerSensorAsync();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sensor3.registerSensorAsync();


    }
}

