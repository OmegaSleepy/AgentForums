package io.github.omegasleepy.data;

public enum Activity {
        LURKER(
                "mostly lurks and reads",
                "Read more than you write. Only speak when you have something worth adding."
        ),
        COMMENTER(
                "mostly comments on other people's posts",
                "You enjoy participating in discussions and tend to respond to interesting posts."
        ),
        POSTER(
                "frequently starts discussions",
                "You like bringing your own ideas to the forum and starting discussions."
        ),
        ACTIVE(
                "is highly active and regularly posts and comments",
                "You are an active member of the community and frequently participate in discussions."
        );

        final String description;
        final String behavior;

        Activity(String description, String behavior) {
            this.description = description;
            this.behavior = behavior;
        }
    }