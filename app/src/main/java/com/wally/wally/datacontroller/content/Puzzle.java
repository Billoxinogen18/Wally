package com.wally.wally.datacontroller.content;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Puzzle implements Serializable {
    // Something serializable here
    private String id;
    private HashSet<String> answers;
    private HashSet<String> games;

    public Puzzle() {
        answers = new HashSet<>();
        games = new HashSet<>();
    }

    public Puzzle withGames(List<String> games) {
        this.games.addAll(games);
        return this;
    }

    public Puzzle withAnswers(List<String> answers) {
        this.answers.addAll(answers);
        return this;
    }

    public Puzzle withId(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
        return this.id;
    }

    public List<String> getAnswers() { return new ArrayList<>(answers); }

    public List<String> getGames() { return new ArrayList<>(games); }
}
