package com.example.mydetector.beans;

import java.util.List;

public class listItem {

    private static List<Coordonnées> coords;
    private static final listItem instance = new listItem();

    public listItem() {
    }

    public static List<Coordonnées> getCoords() {
        return coords;
    }

    public static void setCoords(List<Coordonnées> coords) {
        listItem.coords = coords;
    }


    public static listItem getInstance() {
        return instance;
    }

}
