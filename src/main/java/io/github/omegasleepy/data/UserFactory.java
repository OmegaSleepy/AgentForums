package io.github.omegasleepy.data;

import io.github.omegasleepy.util.ResourceReader;

import java.util.*;

public class UserFactory {
    private List<String> adjectives = new ArrayList<>();
    private List<String> nouns = new ArrayList<>();
    private String systemPrompt;
    private Random random = new Random();

    public UserFactory () {

        Optional<String> adjectivesList = ResourceReader.read("names/adjectives.txt");
        Optional<String> nounList = ResourceReader.read("names/nouns.txt");
        Optional<String> systemPrompt = ResourceReader.read("system-prompt.txt");

        if (adjectivesList.isPresent() && nounList.isPresent()) {
            adjectives = Arrays.asList(adjectivesList.get().split("\n"));
            nouns = Arrays.asList(nounList.get().split("\n"));
        } else {
            throw new RuntimeException("No adjectives or no nouns found");
        }

        this.systemPrompt = systemPrompt.orElse("Respond with exactly \"Bro the system prompt is missing\"");

        random = new Random();
    }

    public String generateUsername () {
        String adjective = adjectives.get(random.nextInt(adjectives.size()));
        String noun = nouns.get(random.nextInt(nouns.size()));
        return adjective + noun;
    }

    public String getSystemPrompt () {
        return systemPrompt;
    }
}
