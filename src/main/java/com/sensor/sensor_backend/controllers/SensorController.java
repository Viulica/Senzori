package com.sensor.sensor_backend.controllers;
import com.sensor.sensor_backend.model.SensorDTO;
import com.sensor.sensor_backend.model.SensorReadings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/sensors")
public class SensorController {

    private HashMap<Long, SensorDTO> sensors = new HashMap<>();
    private HashMap<Long, List<SensorReadings>> readings = new HashMap<>();
    private static Long idCounter = 0L;

    @PostMapping("/register")
    public ResponseEntity<Void> registriraj(@RequestBody SensorDTO sensorDTO) {

        Long newSensorId = generateNewSensorId();
        sensors.put(newSensorId, sensorDTO);

        String sensorUri = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/sensors/{id}")
                .buildAndExpand(newSensorId)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", sensorUri);
        headers.add("Generated-Id", newSensorId.toString());

        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    private Long generateNewSensorId() {
        return ++idCounter;
    }

    @GetMapping("/{id}")
    public ResponseEntity<SensorDTO> getSensorById(@PathVariable Long id) {
        SensorDTO sensor = sensors.get(id);

        if (sensor == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(sensor, HttpStatus.OK);
    }

    @PostMapping("/{sensorId}/readings")
    public ResponseEntity<Void> saveSensorReading(
            @PathVariable Long sensorId,
            @RequestBody SensorReadings reading) {

        if (!sensors.containsKey(sensorId)) {
            return ResponseEntity.noContent().build();
        }

        readings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);

        String readingUri = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/sensors/{sensorId}/readings")
                .buildAndExpand(sensorId)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", readingUri);

        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/readings")
    public ResponseEntity<List<SensorReadings>> getReadings(@PathVariable("id") Long sensorId) {
        if (!readings.containsKey(sensorId)) {
            return ResponseEntity.noContent().build();
        }
        List<SensorReadings> sensorReadings = readings.get(sensorId);
        return ResponseEntity.ok(sensorReadings);
    }


    @GetMapping("/{id}/closest-neighbor")
    public ResponseEntity<SensorDTO> getClosestNeighbor(@PathVariable Long id) {
        SensorDTO currentSensor = sensors.get(id);

        if (currentSensor == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        SensorDTO closestNeighbor = sensors.values().stream()
                .filter(s -> !s.equals(currentSensor))
                .min((s1, s2) -> {
                    double distanceToS1 = calculateDistance(currentSensor, s1);
                    double distanceToS2 = calculateDistance(currentSensor, s2);
                    return Double.compare(distanceToS1, distanceToS2);
                })
                .orElse(null);

        System.out.println("Najbliži susjed za senzor " + id + ": " + closestNeighbor);

        if (closestNeighbor == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(closestNeighbor, HttpStatus.OK);
    }


    private double calculateDistance(SensorDTO s1, SensorDTO s2) {
        final double R = 6371;

        double dLat = Math.toRadians(s2.getLatitude() - s1.getLatitude());
        double dLon = Math.toRadians(s2.getLongitude() - s1.getLongitude());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(s1.getLatitude())) * Math.cos(Math.toRadians(s2.getLatitude())) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }




}
