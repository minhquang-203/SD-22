package org.example.templatejava6.Uv.entity;

public class WeatherData {

    private double temp;
    private double uvIndex;

    // Các hàm Get/Set bắt buộc phải có để Spring Boot chuyển thành JSON
    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getUvIndex() {
        return uvIndex;
    }

    public void setUvIndex(double uvIndex) {
        this.uvIndex = uvIndex;
    }

}
