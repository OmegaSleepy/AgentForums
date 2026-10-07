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
    }

    private String formatWord(String word) {
        word = word.trim();
        if (word.isEmpty()) return word;
        int choice = random.nextInt(3);
        return switch (choice) {
            case 0 -> word.toLowerCase();
            case 1 -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
            case 2 -> word.toUpperCase();
            default -> word;
        };
    }

    private String getRandomSymbol() {
        String[] symbols = {"_", ".", ""};
        return symbols[random.nextInt(symbols.length)];
    }

    public String generateUsername () {
        String adjective = formatWord(adjectives.get(random.nextInt(adjectives.size())));
        String noun = formatWord(nouns.get(random.nextInt(nouns.size())));

        String prefix = getRandomSymbol();
        String middle = getRandomSymbol();

        String number = random.nextBoolean() ? String.valueOf(random.nextInt(999)) : "";

        String suffix = getRandomSymbol();

        return prefix + adjective + middle + noun + number + suffix;
    }

    public String getSystemPrompt () {
        return systemPrompt;
    }
}