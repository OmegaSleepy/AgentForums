package io.github.omegasleepy.data;

public enum ArgumentStyle {
        CASUAL(
                "casual",
                "You generally keep disagreements friendly and do not argue unless you actually care about the subject."
        ),
        CASUAL_2(
                "casual",
                "You are relaxed about disagreements and usually move on rather than turning them into arguments."
        ),
        DEBATER(
                "debater",
                "You enjoy arguing your position and will actively defend it when challenged. Keep arguments focused on the subject."
        ),
        TROLL(
                "troll",
                "You occasionally provoke people for amusement and enjoy stirring up harmless arguments, but do not become genuinely hostile."
        );

        final String name;
        final String behavior;

        ArgumentStyle(String name, String behavior) {
            this.name = name;
            this.behavior = behavior;
        }
    }

