package com.wally.wally.tip;


public class Tip {
    private String id;
    private String title;
    private String message;

    public Tip() {
    }

    public Tip withTitle(String title) {
        this.title = title;
        return this;
    }

    public Tip withMessage(String message) {
        this.message = message;
        return this;
    }

    public Tip withId(String id) {
        this.id = id;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Tip{" +
                "id='" + id + '\'' +
                "title='" + title + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
