package com.wally.wally.tip;


public class Tip {
    private String title;
    private String message;

    public Tip() {}

    public Tip withTitle(String title) {
        this.title = title;
        return this;
    }

    public Tip withMessage(String message) {
        this.message = message;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "Tip{" +
                "title='" + title + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
