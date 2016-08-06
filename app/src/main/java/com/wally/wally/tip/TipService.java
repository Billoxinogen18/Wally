package com.wally.wally.tip;

public interface TipService {
    class Tag {
        public static final String LEARNING = "learning";
        public static final String LOCALIZATION = "localization";
    }

    Tip getRandom();
    Tip getRandom(String tag);
}
