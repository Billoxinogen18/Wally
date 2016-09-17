package com.wally.wally.tip;

public interface TipService {

    class Tag {
        public static final String LEARNING = "learning";
        public static final String LOCALIZATION = "localization";
        public static final String FITTING = "fitting";
        public static final String LOCALIZATION_AFTER_LEARNING = "localization_after_learning";
        public static final String MAP_PUBLIC_FEED = "map_public_feed";
        public static final String MAP_PROFILE = "map_profile";
        public static final String MAP_PERSON = "map_person";
        public static final String NEW_CONTENT = "new_content";
    }

    Tip getRandom();

    boolean isDisabled(String type);

    Tip getRandom(String tag);

    void disableTip(String id);
}
