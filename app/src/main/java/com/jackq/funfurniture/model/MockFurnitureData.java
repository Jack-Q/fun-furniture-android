package com.jackq.funfurniture.model;

import java.util.ArrayList;

/**
 * Created by jackq on 11/29/16.
 */

public class MockFurnitureData {
    public static ArrayList<Furniture> mockFurniture;

    static {
        for (int i = 0; i < 20; i++) {
            mockFurniture.add(new Furniture(i + 1,
                    "Furniture" + i,
                    "Information about item" + i,
                    i % 3,
                    i * 123.2f,
                    "",
                    new ArrayList<String>()
            ));
        }
    }
}
