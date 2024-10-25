package com.sensor.sensor_backend.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CsvReader {
    private List<String[]> readings;

    public CsvReader(String filePath) {
        readings = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(filePath)))) {            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                readings.add(values);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[] getReading(int row) {
        if (row >= 0 && row < readings.size()) {
            return readings.get(row);
        }
        return null;
    }

    public int getTotalRows() {
        return readings.size();
    }
}
