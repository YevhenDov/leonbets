package org.parser.leonbets.model;

import lombok.Data;

import java.util.List;

@Data
public class Event {
    private long id;
    private long kickoff;
    private boolean top;
    private String name;
    private String nameDefault;
    private Region region;
    private League league;
    private List<Market> markets;
}
