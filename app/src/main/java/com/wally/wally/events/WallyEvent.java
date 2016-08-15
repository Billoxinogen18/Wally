package com.wally.wally.events;

public class WallyEvent {
    public static final String
    TANGO_READY ="onTangoReady";
    
    public static final String
    LEARNING_START ="onLearningStart";

    public static final String
    TANGO_OUT_OF_DATE ="onTangoOutOfDate";

    public static final String
    LEARNING_FINISH ="onLearningFinish";

    public static final String
    LOCALIZATION_START ="onLocalizationStart";

    public static final String
    LOCALIZATION_START_AFTER_LEARNING ="onLocalizationStartAfterLearning";

    public static final String
    LOCALIZATION_FINISH_AFTER_LEARNING ="onLocalizationFinishAfterLearning";

    public static final String
    LOCALIZATION_FINISH_AFTER_SAVED_ADF ="onLocalizationFinishAfterSavedAdf";

    public static WallyEvent createEventWithId(String id) {
        return null;
    }
}