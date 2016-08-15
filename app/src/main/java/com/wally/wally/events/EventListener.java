package com.wally.wally.events;

/**
 * Created by Meravici on 8/8/2016. yea
 */
public interface EventListener {
    void onTangoReady();

    void onLearningStart();

    void onTangoOutOfDate();

    void onLearningFinish();

    void onLocalizationStart();

    void onLocalizationStartAfterLearning();

    void onLocalizationFinishAfterLearning();

    void onLocalizationFinishAfterSavedAdf();
}