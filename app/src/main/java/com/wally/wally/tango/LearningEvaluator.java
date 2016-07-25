package com.wally.wally.tango;


import android.util.Log;

import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoPoseData;

import org.rajawali3d.math.Quaternion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LearningEvaluator implements TangoUpdater.ValidPoseListener {
    public static final String TAG = LearningEvaluator.class.getSimpleName();
    public static final int TIMEOUT_S = 20;
    private static final int ANGLE_RESOLUTION = 8;

    private static final int MIN_ANGLE_COUNT = 10;
    private static final int MIN_CELL_COUNT = 4;
    private static final int MIN_TIME_S = 20;
    private static final int MAX_TIME_S = 60;

    private List<Cell> cells;
    private long startTime;
    private long latestUpdateTime;
    private LearningEvaluatorListener listener;

//    public void addCallback(final LearningEvaluatorListener listener) {
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(TIMEOUT_S * 1000);
//                } catch (InterruptedException e) {
//                    listener.onLearningFailed();
//                }
//                listener.onLearningFinish();
//            }
//        }).start();
//    }

    public void addLearningEvaluatorListener(final LearningEvaluatorListener listener){
        this.listener = listener;
        start();
    }

    @Override
    public synchronized void onValidPose(TangoPoseData pose) {
        if (System.currentTimeMillis() - latestUpdateTime < 100){
            return;
        }
        latestUpdateTime = System.currentTimeMillis();

        int x = (int)(pose.translation[0]/Cell.CELL_SIZE_M);
        int y = (int)(pose.translation[1]/Cell.CELL_SIZE_M);
        Cell c = new Cell();
        c.x = x;
        c.y = y;
        Quaternion q = new Quaternion(pose.rotation[0],pose.rotation[1],pose.rotation[2],pose.rotation[3]);
        double pitch = Math.toDegrees(Math.asin(-2.0*(q.x*q.z - q.w*q.y)));
        if (pitch < 0) pitch += 360;
        int angleIndex = (int)(pitch / 360 * ANGLE_RESOLUTION) % ANGLE_RESOLUTION;
        c.angleVisited[angleIndex] = true;
        int index = cells.indexOf(c);
        if (index == -1){
            cells.add(c);
        } else {
            cells.get(index).angleVisited = c.angleVisited;
        }
        Log.d(TAG, "onValidPose() getAngleCount = " + getAngleCount() + " size = " + cells.size() + "cells : " +cells);

        if (canFinish()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    listener.onLearningFinish();
                }
            }).start();
        }
    }

    private boolean canFinish(){
        int angleCount = getAngleCount();
        int size = cells.size();
        long time = System.currentTimeMillis() - startTime;
        if (angleCount >= MIN_ANGLE_COUNT && size >= MIN_CELL_COUNT && time > MIN_TIME_S*1000){
            return true;
        }
        if (time > MAX_TIME_S*1000){
            return true;
        }
        return false;
    }

    private int getAngleCount(){
        int res = 0;
        for (Cell c : cells){
            for (int i = 0; i<ANGLE_RESOLUTION; i++){
                if (c.angleVisited[i]) res++;
            }
        }
        return res;
    }

    public void start(){
        cells = new ArrayList<>();
        startTime = System.currentTimeMillis();
        latestUpdateTime = startTime;
    }

    interface LearningEvaluatorListener{
        void onLearningFinish();
        void onLearningFailed();
    }

    class Cell{
        public static final double CELL_SIZE_M = 1; //Cell size in meters.
        public int x;
        public int y;
        public boolean[] angleVisited = new boolean[ANGLE_RESOLUTION];

        @Override
        public boolean equals(Object o){
            Cell c = (Cell) o;
            if (c.x == x && c.y == y) {
                return true;
            } else{
                return false;
            }
        }

        @Override
        public String toString() {
            return "(" + x + "," +y + ")";
        }
    }
}