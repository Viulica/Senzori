package com.sensor.sensor_backend.model;

public class SensorNeighborDTO {
    private double latitude;
    private double longitude;
    private String ip;
    private int port;

    public SensorNeighborDTO(double latitude, double longitude, String ip, int port) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.ip = ip;
        this.port = port;
    }

    // Getters and Setters
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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }


}
