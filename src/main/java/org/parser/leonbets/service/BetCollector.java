package org.parser.leonbets.service;

import com.google.gson.Gson;
import org.parser.leonbets.model.Bet;
import org.parser.leonbets.model.Event;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;
import java.util.concurrent.Callable;

public class BetCollector implements Callable<Bet> {

    private final static String URL_EVENT_BY_EVENT_ID = "https://leonua4.com/api-2/betline/event/all?ctag=ru-UA&eventId=%s&flags=reg,urlv2,mm2,rrc,nodup,smg,outv2";
    private final static String DATE_PATTERN = "yyyy-MM-dd HH:mm";

    private final Long eventId;
    private HttpResponse<String> response;

    public BetCollector(Long eventId) {
        this.eventId = eventId;
    }

    @Override
    public Bet call() throws Exception {

        String eventString;
        try {
            eventString = getResponse(new URI(URL_EVENT_BY_EVENT_ID.formatted(eventId)));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        Gson gson = new Gson();
        Event event = gson.fromJson(eventString, Event.class);

        Bet bet = new Bet();
        try {
            bet.setSport(event.getLeague().getSport().getName());
            bet.setLeagueName(event.getLeague().getName());
            bet.setMatchName(event.getName());
            bet.setStartTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getKickoff()),
                    TimeZone.getDefault().toZoneId()).format(DateTimeFormatter.ofPattern(DATE_PATTERN)));
            bet.setMatchId(eventId);
            bet.setLeagueRegion(event.getLeague().getRegion().getName());
            bet.setMarkets(event.getMarkets());
        } catch (NullPointerException e) {
            throw new Exception("Issue in Bet building. Event id: %s, %s".formatted(eventId, e.getMessage()));
        }

        return bet;
    }

    private String getResponse(URI uri) {
        var client = getClient();
        var request = getRequest(uri);
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return response.body();
    }

    private static HttpRequest getRequest(URI uri) {
        return HttpRequest.newBuilder()
                .uri(uri)
                .build();
    }

    private HttpClient getClient() {
        return HttpClient.newBuilder().build();
    }
}
