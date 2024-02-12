package org.parser.leonbets.model;

import lombok.Data;

@Data
public class League {
    private long id;
    private int topOrder;
    private boolean top;
    private String name;
    private String nameDefault;
    private String url;
    private Sport sport;
    private Region region;
}
