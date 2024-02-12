package org.parser.leonbets.model;

import lombok.Data;

import java.util.List;

@Data
public class Region {
    private long id;
    private String name;
    private String nameDefault;
    private String family;
    private String url;
    private List<League> leagues;
}
