package org.parser.leonbets.model;

import lombok.Data;

import java.util.List;

@Data
public class Bet {
    private long matchId;
    private String sport;
    private String leagueName;
    private String leagueRegion;
    private String matchName;
    private String startTime;
    private List<Market> markets;

    @Override
    public String toString() {
        return """
                                
                %s, %s %s
                    %s, %s, %s""".formatted(sport, leagueRegion, leagueName, matchName, startTime, matchId);
    }
}
