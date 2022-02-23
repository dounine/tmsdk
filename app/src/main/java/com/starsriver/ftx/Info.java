package com.starsriver.ftx;

public class Info {
    private String version;
    private String brand;
    private String model;

    public Info(String version, String brand, String model) {
        this.version = version;
        this.brand = brand;
        this.model = model;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
