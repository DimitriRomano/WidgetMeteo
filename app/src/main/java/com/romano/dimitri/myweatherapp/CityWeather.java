package com.romano.dimitri.myweatherapp;

import java.io.Serializable;

public class CityWeather implements Serializable {
    private String name;
    private String temp;
    private String condition;
    private String icon;

    public CityWeather() {
        //default;
    }

    public CityWeather(String name, String temp, String condition, String icon) {
        this.name = name;
        this.temp = temp;
        this.condition = condition;
        this.icon = icon;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
