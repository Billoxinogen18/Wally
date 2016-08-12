package com.wally.wally.tip;

public interface TipService {
    class Tag {
        public static final String LEARNING = "Learning";
        public static final String LOCALIZATION = "Localization";

        public static final String FITTING = "Fitting";
    }

    Tip getRandom();
    Tip getRandom(String tag);
    void disableTip(String id);
}
