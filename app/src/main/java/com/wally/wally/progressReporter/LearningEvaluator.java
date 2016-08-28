package com.wally.wally.progressReporter;

import com.wally.wally.config.Config;
import com.wally.wally.config.LearningEvaluatorConstants;

import org.rajawali3d.math.Quaternion;

import java.util.ArrayList;
import java.util.List;

public class LearningEvaluator implements ProgressReporter {
    private static final double RATIO = 0.6;

    private int minTimeMs;
    private int maxTimeMs;
    private int minCellCount;
    private int minAngleCount;
    private int angleResolution;

    private List<Cell> cells;
    private long startTime;
    private long latestUpdateTime;
    private LearningEvaluatorListener listener;
    private boolean isFinished;
    // This is not actual progress
    private int featureCounter;
    private int iteration;

    private ProgressListener progressListener;

    public LearningEvaluator(Config config) {
        minTimeMs = config.getInt(LearningEvaluatorConstants.MIN_TIME_S) * 1000;
        maxTimeMs = config.getInt(LearningEvaluatorConstants.MAX_TIME_S) * 1000;
        minCellCount = config.getInt(LearningEvaluatorConstants.MIN_CELL_COUNT);
        minAngleCount = config.getInt(LearningEvaluatorConstants.MIN_ANGLE_COUNT);
        angleResolution = config.getInt(LearningEvaluatorConstants.ANGLE_RESOLUTION);
    }

    public void addLearningEvaluatorListener(final LearningEvaluatorListener listener) {
        this.listener = listener;
        isFinished = false;
        iteration++;
        start();
    }

    public synchronized void onValidPose(double[] translation, double[] rotation) {
        if (System.currentTimeMillis() - latestUpdateTime < 100 || isFinished) {
            return;
        }
        latestUpdateTime = System.currentTimeMillis();

        int x = (int) (translation[0] / Cell.CELL_SIZE_M);
        int y = (int) (translation[1] / Cell.CELL_SIZE_M);
        Cell cell = new Cell();
        cell.x = x;
        cell.y = y;

        // Add cell if new
        int index = cells.indexOf(cell);
        if (index == -1) {
            cells.add(cell);
            featureCounter++;
        } else {
            cell = cells.get(index);
        }

        Quaternion q = new Quaternion(rotation[0], rotation[1], rotation[2], rotation[3]);
        double yaw = Math.toDegrees(q.getYaw());
        if (yaw < 0) yaw += 360;
        if (yaw < 0 || yaw > 360) {
            throw new IllegalStateException("Yaw is illegal [yaw = " + yaw + "]");
        }

        // Update angle
        int angleIndex = ((int) (yaw / 360 * angleResolution)) % angleResolution;
        if (!cell.angleVisited[angleIndex]) {
            cell.angleVisited[angleIndex] = true;
            featureCounter++;
        }

        progressListener.onProgressUpdate(this, getProgress());

        if (canFinish() && !isFinished) {
            isFinished = true;
            progressListener.onProgressUpdate(this, 1);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    listener.onLearningFinish();
                    progressListener.onProgressUpdate(LearningEvaluator.this, 1);
                }
            }).start();
        }
    }

    private double getProgress() {
        long timePassed = System.currentTimeMillis() - startTime;
        double timePercent = (double) timePassed / minTimeMs;
        double newProgress = Math.min(((double) featureCounter) / (minCellCount + minAngleCount), timePercent);
        newProgress = Math.min(newProgress, 1);

        double previousProgress = 0;
        for (int i = 1; i < iteration; i++) {
            previousProgress += (1 - previousProgress) * RATIO;
        }
        double currentProgress = previousProgress + (1 - previousProgress) * RATIO * newProgress;
        if (currentProgress > 1 || currentProgress < 0) {
            throw new IllegalStateException("Progress is valid! progress = [" + currentProgress + "]");
        }
        return currentProgress;
    }

    private boolean canFinish() {
        int angleCount = getAngleCount();
        int size = cells.size();
        long time = System.currentTimeMillis() - startTime;
        return angleCount >= minAngleCount && size >= minCellCount && time > minTimeMs || time > maxTimeMs;
    }

    private int getAngleCount() {
        int res = 0;
        for (Cell c : cells) {
            for (int i = 0; i < angleResolution; i++) {
                if (c.angleVisited[i]) res++;
            }
        }
        return res;
    }

    private LearningEvaluator start() {
        cells = new ArrayList<>();
        startTime = System.currentTimeMillis();
        latestUpdateTime = startTime;
        featureCounter = 0;
        return this;
    }

    @Override
    public void addProgressListener(ProgressListener listener) {
        progressListener = listener;
    }


    public interface LearningEvaluatorListener {
        void onLearningFinish();

        void onLearningFailed();
    }

    class Cell {
        public static final double CELL_SIZE_M = 1; //Cell size in meters.
        public int x;
        public int y;
        public boolean[] angleVisited = new boolean[angleResolution];

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Cell)) {
                return false;
            }
            Cell c = (Cell) o;
            return c.x == x && c.y == y;
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }
}