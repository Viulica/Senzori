package com.sensor.sensor_backend.controllers;

import com.sensor.sensor_backend.model.Sensor;
import com.sensor.sensor_backend.model.SensorDTO;
import com.sensor.sensor_backend.model.SensorReadings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/sensors")
public class SensorController {

    private HashMap<Long, SensorDTO> sensors = new HashMap<>();
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
