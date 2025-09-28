package com.yuki920.bedwarsstats;

public enum DisplayMode {
    OVERALL("Overall"),
    SOLO("Solo"),
    DOUBLES("Doubles"),
    THREES("Threes"),
    FOURS("Fours");

    private final String name;

    DisplayMode(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}