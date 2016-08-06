package com.wally.wally.tip;

public interface TipService {
    class Tag {
        public static final String LEARNING = "Learning";
        public static final String LOCALIZATION = "Localization";
    }

    Tip getRandom();
    Tip getRandom(String tag);
}
