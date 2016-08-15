package com.wally.wally.tip;

public interface TipService {
    class Tag {
        public static final String LEARNING = "learning";
        public static final String LOCALIZATION = "localization";

        public static final String FITTING = "fitting";
    }

    Tip getRandom();
    Tip getRandom(String tag);
    void disableTip(String id);
}
