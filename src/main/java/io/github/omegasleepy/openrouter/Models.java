package io.github.omegasleepy.openrouter;

public enum Models {
    GEMMA_4_26B_A4B_IT("google/gemma-4-26b-a4b-it:free"),
    GEMMA_4_31B_IT("google/gemma-4-31b-it:free"),
    NORTH_MINI_CODE("cohere/north-mini-code:free"),
    INKLING("thinkingmachines/inkling:free"),
    INKLING_SMALL("thinkingmachines/inkling-small:free");

    final String name;

    Models(String name) {
        this.name = name;
    }

}
