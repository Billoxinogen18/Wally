package com.wally.wally.datacontroller.content;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Puzzle implements Serializable {
    private String id;
    private boolean isSolved;
    private String markerURL;
    private String unsolvedMarkerURL;
    private HashSet<String> answers;
    private HashSet<String> successors;

    public Puzzle() {
        answers = new HashSet<>();
        successors = new HashSet<>();
    }

    public Puzzle withSuccessors(List<String> games) {
        if (games != null) {
            this.successors.addAll(games);
        }
        return this;
    }

    public Puzzle withAnswers(List<String> answers) {
        if (answers != null) {
            this.answers.addAll(answers);
        }
        return this;
    }

    public Puzzle withId(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
        return this.id;
    }

    public Puzzle withIsSolved(boolean isSolved) {
        this.isSolved = isSolved;
        return this;
    }

    public boolean isSolved() {
        return isSolved;
    }

    public String getMarkerURL() {
        return markerURL;
    }

    public Puzzle withMarkerURL(String markerURL) {
        this.markerURL = markerURL;
        return this;
    }

    public String getUnsolvedMarkerURL() {
        return unsolvedMarkerURL;
    }

    public Puzzle withUnsolvedMarkerURL(String unsolvedMarkerURL) {
        this.unsolvedMarkerURL = unsolvedMarkerURL;
        return this;
    }

    public List<String> getAnswers() {
        return new ArrayList<>(answers);
    }

    public List<String> getSuccessors() {
        return new ArrayList<>(successors);
    }
}
