package org.parser.leonbets;

import org.parser.leonbets.service.ParserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LeonbetsParserApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeonbetsParserApplication.class, args);
        ParserService parserService = new ParserService();

        parserService.printBets(parserService.parse());
    }
}
