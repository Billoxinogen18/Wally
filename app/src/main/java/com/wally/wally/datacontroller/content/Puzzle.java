package com.wally.wally.datacontroller.content;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Puzzle implements Serializable {
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
        answer = answer.toLowerCase();
        for (String s : this.answers) {
            if (s.toLowerCase().equals(answer)) {
                return true;
            }
        }
        return false;
    }
}
