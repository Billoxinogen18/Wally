package com.wally.wally.events;

public class WallyEvent {

    public static final String
    ON_PAUSE = "Pause";

    public static final String
    ON_RESUME = "Resume";

    public static final String
    TANGO_READY = "TangoReady";

    public static final String
    TANGO_OUT_OF_DATE = "TangoOutOfDate";

    public static final String
    LEARNING_START = "LearningStart";

    public static final String
    LEARNING_FINISH = "LearningFinish";

    public static final String
    LOCALIZATION_START = "LocStart";

    public static final String
    LOCALIZATION_START_AFTER_LEARNING = "LocStartAfterLearning";

    public static final String
    LOCALIZATION_FINISH_AFTER_LEARNING = "LocFinishAfterLearning";

    public static final String
    LOCALIZATION_FINISH_AFTER_SAVED_ADF = "LocFinishAfterSavedAdf";

    public static final String
    LOCALIZATION_FINISH_AFTER_CLOUD_ADF = "LocFinishAfterCloudAdf";

    public static final String
    LOCALIZATION_START_ON_CLOUD_ADF = "LocStartOnCloudAdf";

    public static final String
    ON_LOCALIZATION_LOST = "LocLost";

    public static final String
    ON_NEW_CONTENT_DIALOG_SHOW = "NewContentDialogShow";

    private final String id;

    private WallyEvent(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static WallyEvent createEventWithId(String id) {
        return new WallyEvent(id);
    }
}