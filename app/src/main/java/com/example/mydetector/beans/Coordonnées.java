package com.example.mydetector.beans;

public class Coordonnées {
    public float x, y;

    public Coordonnées(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Coordonnées() {
    }

    @Override
    public String toString() {
        return x + ", " + y;
    }
}
