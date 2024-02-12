package org.parser.leonbets.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import org.parser.leonbets.model.Bet;
import org.parser.leonbets.model.League;
import org.parser.leonbets.model.Market;
import org.parser.leonbets.model.Region;
import org.parser.leonbets.model.Runner;
import org.parser.leonbets.model.Sport;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class ParserService {

    private static final String SPORT_SOCCER = "Soccer";
    private static final String SPORT_TENNIS = "Tennis";
    private static final String SPORT_HOCKEY = "IceHockey";
    private static final String SPORT_BASKETBALL = "Basketball";
    private static final Integer THREAD_POOL_SIZE = 3;
    private static final String URL_ALL_SPORTS = "https://leonua4.com/api-2/betline/sports?ctag=ru-UA&flags=urlv2";

    public final Logger LOGGER = Logger.getLogger(this.getClass().getName());
    private HttpResponse<String> response;

    public List<Bet> parse() {
        List<Sport> sports = getSports();
        List<Long> topLeaguesIdsBySports = getTopLeaguesIdsBySports(sports);
        List<Long> eventsIdsByLeagues = getEventsIdsByLeagues(topLeaguesIdsBySports);

        return getBetsByEventsIds(eventsIdsByLeagues);
    }

    public void printBets(List<Bet> bets) {
        for (Bet bet : bets) {
            System.out.println(bet);
            for (Market market : bet.getMarkets()) {
                System.out.println(market);
                for (Runner runner : market.getRunners()) {
                    System.out.println(runner);
                }
            }
        }
    }

    private List<Sport> getSports() {
        String sportsString;
        try {
            sportsString = getResponse(new URI(URL_ALL_SPORTS));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        Gson gson = new Gson();
        Type sportListType = new TypeToken<List<Sport>>() {}.getType();
        List<Sport> sports = gson.fromJson(sportsString, sportListType);

        return sports.stream()
                .filter(sport ->
                        sport.getFamily().equals(SPORT_SOCCER) ||
                                sport.getFamily().equals(SPORT_TENNIS) ||
                                sport.getFamily().equals(SPORT_HOCKEY) ||
                                sport.getFamily().equals(SPORT_BASKETBALL))
                .toList();
    }

    private List<Long> getTopLeaguesIdsBySports(List<Sport> sports) {
        List<Long> topLeaguesIds = new ArrayList<>();
        for (Sport sport : sports) {
            for (Region region : sport.getRegions()) {
                List<Long> filteredLeaguesIds = region.getLeagues().parallelStream()
                        .filter(League::isTop)
                        .map(League::getId)
                        .toList();

                topLeaguesIds.addAll(filteredLeaguesIds);
            }
        }

        return topLeaguesIds;
    }

    private List<Long> getEventsIdsByLeagues(List<Long> leaguesIds) {
        List<Long> eventsIds = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        List<Callable<Long>> tasks = new ArrayList<>();
        for (Long leagueId : leaguesIds) {
            tasks.add(new EventCollector(leagueId));
        }

        try {
            List<Future<Long>> futures = executor.invokeAll(tasks);

            for (Future<Long> future : futures) {
                try {
                    eventsIds.add(future.get());
                } catch (Exception e) {
                    LOGGER.warning(e.getMessage());
                }

            }
        } catch (InterruptedException e) {
            LOGGER.warning("Thread interrupted while executing tasks: " + e.getMessage());
        } finally {
            executor.shutdown();
        }

        return eventsIds;
    }

    private List<Bet> getBetsByEventsIds(List<Long> eventIds) {
        List<Bet> bets = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        List<Callable<Bet>> tasks = new ArrayList<>();
        for (Long eventId : eventIds) {
            tasks.add(new BetCollector(eventId));
        }

        try {
            List<Future<Bet>> futures = executor.invokeAll(tasks);

            for (Future<Bet> future : futures) {
                try {
                    bets.add(future.get());
                } catch (Exception e) {
                    LOGGER.warning(e.getMessage());
                }
            }
        } catch (InterruptedException e) {
            LOGGER.warning("Thread interrupted while executing tasks: " + e.getMessage());
        } finally {
            executor.shutdown();
        }

        return bets;
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
