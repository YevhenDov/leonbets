package org.parser.leonbets.model;

import lombok.Data;

import java.util.List;

@Data
public class Match {
    private long id;
    private String name;
    private List<Event> events;
}
