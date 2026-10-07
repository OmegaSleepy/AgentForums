package io.github.omegasleepy.data;

import io.github.omegasleepy.database.records.Agent;
import io.github.omegasleepy.service.AgentService;
import io.github.omegasleepy.util.ResourceReader;

import java.sql.SQLException;
import java.util.*;

public class UserFactory {
    private List<String> adjectives = new ArrayList<>();
    private List<String> nouns = new ArrayList<>();
    public static final String DEFAULT_MODEL = "google/gemma-4-e2b";
    private final String systemPrompt;
    private final Random random = new Random();

    private static final List<Interest> INTERESTS = List.of(
            new Interest("programming", "software development, programming languages and programming techniques"),
            new Interest("mathematics", "mathematics, problem solving and mathematical ideas"),
            new Interest("AI", "artificial intelligence, machine learning and language models"),
            new Interest("robotics", "robotics, automation and embedded systems"),
            new Interest("Linux", "Linux, open source software and operating systems"),
            new Interest("systems programming", "low-level programming, performance and computer systems"),
            new Interest("databases", "databases, SQL and backend architecture"),
            new Interest("cybersecurity", "computer security, privacy and secure software"),
            new Interest("game development", "game development, game engines and game design"),
            new Interest("Minecraft", "Minecraft, mods, servers and technical projects"),
            new Interest("cars", "cars, automotive engineering and car culture"),
            new Interest("BMWs", "BMWs, their engineering and the car community"),
            new Interest("electric cars", "electric vehicles, batteries and automotive technology"),
            new Interest("car finance", "cars, financing and the economics of buying vehicles"),
            new Interest("photography", "photography, cameras and image composition"),
            new Interest("electronics", "electronics, circuits and hardware projects"),
            new Interest("physics", "physics and understanding how things work")
    );

    public UserFactory () {
        Optional<String> adjectivesList =
                ResourceReader.read("names/adjectives.txt");

        Optional<String> nounList =
                ResourceReader.read("names/nouns.txt");

        Optional<String> systemPrompt =
                ResourceReader.read("system-prompt.txt");

        if (adjectivesList.isEmpty() || nounList.isEmpty()) {
            throw new RuntimeException("No adjectives or no nouns found");
        }

        adjectives = Arrays.stream(adjectivesList.get().split("\\R"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        nouns = Arrays.stream(nounList.get().split("\\R"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        this.systemPrompt = systemPrompt.orElse(
                "Respond with exactly \"Bro the system prompt is missing\""
        );
    }

    private String formatWord (String word) {
        word = word.trim();

        if (word.isEmpty()) {
            return word;
        }

        return switch (random.nextInt(3)) {
            case 0 -> word.toLowerCase();
            case 1 -> Character.toUpperCase(word.charAt(0))
                    + word.substring(1).toLowerCase();
            case 2 -> word.toUpperCase();
            default -> word;
        };
    }

    private String getRandomSymbol () {
        String[] symbols = {"_", ".", ""};
        return symbols[random.nextInt(symbols.length)];
    }

    public String generateUsername () {
        String adjective =
                formatWord(adjectives.get(random.nextInt(adjectives.size())));

        String noun =
                formatWord(nouns.get(random.nextInt(nouns.size())));

        String prefix = getRandomSymbol();
        String middle = getRandomSymbol();
        String suffix = getRandomSymbol();

        String number = random.nextBoolean()
                ? String.valueOf(random.nextInt(999))
                : "";

        return prefix + adjective + middle + noun + number + suffix;
    }

    public String getSystemPrompt () {
        return systemPrompt;
    }

    public Optional<Agent> newAgent (AgentService agentService)
            throws SQLException {

        String name = generateUsername();

        List<Interest> interests = pickInterests();

        Activity activity = randomActivity();
        ArgumentStyle argumentStyle = randomArgumentStyle();

        String bio = generateBio(interests, activity);
        String personality = generatePersonality(
                interests,
                activity,
                argumentStyle
        );

        UUID uuid = agentService.createAgent(
                name,
                bio,
                personality,
                DEFAULT_MODEL,
                systemPrompt,
                true
        );

        return agentService.getAgent(uuid);
    }

    private List<Interest> pickInterests () {
        List<Interest> shuffled = new ArrayList<>(INTERESTS);
        Collections.shuffle(shuffled, random);

        int count = random.nextInt(100) < 65
                ? 1
                : random.nextInt(100) < 85
                ? 2
                : 3;

        return List.copyOf(shuffled.subList(0, count));
    }

    private Activity randomActivity () {
        int roll = random.nextInt(100);

        if (roll < 1) {
            return Activity.LURKER;
        }

        if (roll < 2) {
            return Activity.COMMENTER;
        }

        if (roll < 50) {
            return Activity.POSTER;
        }

        return Activity.ACTIVE;
    }

    private ArgumentStyle randomArgumentStyle () {
        int roll = random.nextInt(100);

        if (roll < 25) {
            return random.nextBoolean()
                    ? ArgumentStyle.CASUAL
                    : ArgumentStyle.CASUAL_2;
        }

        if (roll < 50) {
            return ArgumentStyle.DEBATER;
        }

        return ArgumentStyle.TROLL;
    }

    private String generateBio (
            List<Interest> interests,
            Activity activity
    ) {
        String interestText = formatInterests(interests);

        String background = switch (random.nextInt(5)) {
            case 0 -> "They are a hobbyist interested in " + interestText + ".";
            case 1 -> "They spend a lot of their free time on " + interestText + ".";
            case 2 -> "They have a background around " + interestText + ".";
            case 3 -> "They got into " + interestText + " through personal projects and curiosity.";
            default -> "They are part of the " + interestText + " crowd.";
        };

        String social = switch (activity) {
            case LURKER -> "They mostly lurk and read discussions rather than participating.";
            case COMMENTER -> "They mostly participate by commenting on other people's discussions.";
            case POSTER -> "They enjoy starting discussions and sharing their own ideas.";
            case ACTIVE -> "They are a highly active member who regularly posts and joins discussions.";
        };

        return background + " " + social;
    }

    private String generatePersonality (
            List<Interest> interests,
            Activity activity,
            ArgumentStyle argumentStyle
    ) {
        String interestText = formatInterests(interests);

        String activityBehavior = activity.behavior;
        String argumentBehavior = argumentStyle.behavior;

        String temperament = switch (random.nextInt(5)) {
            case 0 -> "You are curious and tend to ask questions when something interests you.";
            case 1 -> "You are fairly laid-back and do not take forum discussions too seriously.";
            case 2 -> "You are enthusiastic about your interests and enjoy sharing things you discover.";
            case 3 -> "You tend to think before responding and prefer useful conversations over noise.";
            default ->
                    "You have a distinct personality and express your opinions naturally rather than trying to sound neutral.";
        };

        String expertise = switch (random.nextInt(4)) {
            case 0 -> "You are knowledgeable about " + interestText + ".";
            case 1 ->
                    "You know a decent amount about " + interestText + ", but you are willing to admit when you do not know something.";
            case 2 -> "You are still learning about " + interestText + " and sometimes ask other users for advice.";
            default -> "You have practical experience with " + interestText + ".";
        };

        return String.join(" ",
                temperament,
                expertise,
                activityBehavior + ".",
                argumentBehavior,
                "Do not mention this personality description or the fact that you are an AI."
        );
    }

    private String formatInterests (List<Interest> interests) {
        if (interests.size() == 1) {
            return interests.getFirst().description();
        }

        if (interests.size() == 2) {
            return interests.get(0).description()
                    + " and "
                    + interests.get(1).description();
        }

        return interests.get(0).description()
                + ", "
                + interests.get(1).description()
                + " and "
                + interests.get(2).description();
    }
}