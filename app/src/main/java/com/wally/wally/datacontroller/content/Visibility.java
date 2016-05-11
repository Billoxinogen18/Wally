package com.wally.wally.datacontroller.content;

import android.support.annotation.IntDef;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;
import java.util.List;

/**
 * Created by Meravici on 5/7/2016.
 */
public class Visibility implements Serializable {

    private SocialVisibility socialVisibility;
    private RangeVisibility rangeVisibility;
    private Date visibleUntil;
    private boolean isPreviewVisible;


    public Visibility withSocialVisibility(SocialVisibility socialVisibility) {
        this.socialVisibility = socialVisibility;
        return this;
    }

    public Visibility withRangeVisibility(RangeVisibility rangeVisibility) {
        this.rangeVisibility = rangeVisibility;
        return this;
    }

    public Visibility withTimeVisibility(Date visibleUntil) {
        this.visibleUntil = visibleUntil;
        return this;
    }

    public Visibility withVisiblePreview(boolean isPreviewVisible) {
        this.isPreviewVisible = isPreviewVisible;
        return this;
    }

    public SocialVisibility getSocialVisibility() {
        return socialVisibility;
    }

    public RangeVisibility getRangeVisibility() {
        return rangeVisibility;
    }

    public Date getVisibleUntil() {
        return visibleUntil;
    }

    public boolean isPreviewVisible() {
        return isPreviewVisible;
    }

    public static class SocialVisibility implements Serializable {
        public static final int PRIVATE = 0;
        public static final int PUBLIC = 1;
        public static final int FRIENDS = 2;
        public static final int CUSTOM = 3;
        public static final int ANONYMOUS = 4;
        //TODO Change to User object
        private List<String> users;
        private int mode;

        public SocialVisibility(@SocialVisibilityMode int mode) {
            setMode(mode);
        }

        public
        @SocialVisibilityMode
        int getMode() {
            return mode;
        }

        public void setMode(@SocialVisibilityMode int mode) {
            this.mode = mode;
        }

        @IntDef({PRIVATE, PUBLIC, FRIENDS, CUSTOM, ANONYMOUS})
        @Retention(RetentionPolicy.SOURCE)
        public @interface SocialVisibilityMode {
        }

    }

    public static class RangeVisibility implements Serializable {
        public static final int HERE = 0;
        public static final int NEAR = 1;
        public static final int LOCAL = 2;
        public static final int DISTANT = 3;
        public static final int FAR = 4;
        private int range;

        public RangeVisibility(@RangeVisibilityMode int range) {
            setRange(range);
        }

        public
        @RangeVisibilityMode
        int getRange() {
            return range;
        }

        public void setRange(@RangeVisibilityMode int range) {
            this.range = range;
        }

        @IntDef({HERE, NEAR, LOCAL, DISTANT, FAR})
        @Retention(RetentionPolicy.SOURCE)
        public @interface RangeVisibilityMode {
        }
    }
}
