package org.parser.leonbets.model;

import lombok.Data;

import java.util.List;

@Data
public class Sport {
    private long id;
    private String name;
    private String family;
    private List<Region> regions;
}
