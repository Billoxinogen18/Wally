package com.wally.wally.datacontroller.content;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Puzzle implements Serializable{
    // Something serializable here
    private HashSet<String> answers;

    public Puzzle() {
        answers = new HashSet<>();
    }

    public Puzzle withAnswers(List<String> answers) {
        this.answers.addAll(answers);
        return this;
    }

    public List<String> getAnswers() {
        return new ArrayList<>(answers);
    }

    public boolean checkAnswer(String answer) {
        return this.answers.contains(answer);
    }
}
