package com.sensor.sensor_backend.model;

import java.util.HashMap;
import java.util.Map;

public class SensorService {

    private static SensorService instance;
    private SensorService() {}

    private Map<Integer, SensorInfo> map = new HashMap<>();

    public void addSensor(int port, Long id, String name) {
        SensorInfo sensorInfo = new SensorInfo(id, name);
        map.put(port, sensorInfo);
    }

    public SensorInfo getSensorByPort(int port) {
        return map.get(port);
    }

    public static synchronized SensorService getInstance() {
        if (instance == null) {
            instance = new SensorService();
        }
        return instance;
    }

}

