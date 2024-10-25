package com.sensor.sensor_backend.model;

public class SensorInfo {
    private Long id;
    private String name;

    public SensorInfo(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "SensorInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
