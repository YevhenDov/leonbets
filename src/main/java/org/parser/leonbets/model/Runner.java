package org.parser.leonbets.model;

import lombok.Data;

@Data
public class Runner {
    private long id;
    private double price;
    private String name;
    private String priceStr;

    @Override
    public String toString() {
        return "\t\t\t" + name + ", " + priceStr + ", " + id;
    }
}
