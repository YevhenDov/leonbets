package org.parser.leonbets.service;

import com.google.gson.Gson;
import org.parser.leonbets.model.Match;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Callable;

public class EventCollector implements Callable<Long> {

    private static final String URL_MATCHES_BY_LEAGUE = "https://leonua4.com/api-2/betline/events/all?ctag=ru-UA&league_id=%s&hideClosed=true&flags=reg,urlv2,mm2,rrc,nodup";

    private Long leagueId;
    private HttpResponse<String> response;

    public EventCollector(Long leagueId) {
        this.leagueId = leagueId;
    }

    @Override
    public Long call() throws Exception {
        long eventId;
        try {
            String matchString;
            matchString = getResponse(new URI(URL_MATCHES_BY_LEAGUE.formatted(leagueId)));

            Gson gson = new Gson();
            Match match = gson.fromJson(matchString, Match.class);

            eventId = match.getEvents().get(0).getId();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            throw new Exception("Issue in event parsing. League id: %s. %s".formatted(leagueId, e.getMessage()));
        }

        return eventId;
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
