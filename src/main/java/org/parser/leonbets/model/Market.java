package org.parser.leonbets.model;

import lombok.Data;

import java.util.List;

@Data
public class Market {
    private String name;
    private List<Runner> runners;

    @Override
    public String toString() {
        return "\t\t" + name;
    }
}
