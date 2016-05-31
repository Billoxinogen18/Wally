package com.wally.wally.datacontroller.content;

import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;

import com.wally.wally.App;
import com.wally.wally.R;
import com.wally.wally.datacontroller.user.Id;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Visibility implements Serializable {

    private SocialVisibility socialVisibility;
    private RangeVisibility rangeVisibility;
    private Date visibleUntil;
    private boolean isPreviewVisible;

    public Visibility() {
        socialVisibility = new SocialVisibility(SocialVisibility.PUBLIC);
        rangeVisibility = new RangeVisibility(RangeVisibility.DISTANT);
        isPreviewVisible = true;
    }

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

    @Override
    public String toString() {
        return "{" +
                "social=" + socialVisibility +
                ", range=" + rangeVisibility +
                ", duration=" + visibleUntil +
                ", hasPreview=" + isPreviewVisible +
                '}';
    }

    public static class SocialVisibility implements Serializable {
        public static final int PRIVATE = 0;
        public static final int PUBLIC = 1;
        public static final int PEOPLE = 2;
        public static final int ANONYMOUS = 3;

        private int mode;
        private List<Id> sharedWith;

        public SocialVisibility(@SocialVisibilityMode int mode) {
            setMode(mode);
        }


        public static int getSize() {
            return 4;
        }

        public static String getStringRepresentation(@SocialVisibilityMode int range) {
            return App.getContext().getResources().getStringArray(R.array.social_visibility)[range];
        }

        public static
        @DrawableRes
        int toDrawableRes(@SocialVisibilityMode int range) {
            switch (range) {
                case PRIVATE:
                    return R.drawable.ic_private_visibility_black_24dp;
                case PUBLIC:
                    return R.drawable.ic_public_visibility_24dp;
                case PEOPLE:
                    return R.drawable.ic_people_visibility_black;
                case ANONYMOUS:
                    return R.drawable.ic_anonymous_visibility_black_24dp;
                default:
                    throw new IllegalArgumentException("Unsupported image");
            }
        }

        public
        @SocialVisibilityMode
        int getMode() {
            return mode;
        }

        public void setMode(@SocialVisibilityMode int mode) {
            this.mode = mode;
        }

        @Override
        public String toString() {
            return getStringRepresentation(mode);
        }

        @IntDef({PRIVATE, PUBLIC, PEOPLE, ANONYMOUS})
        @Retention(RetentionPolicy.SOURCE)
        public @interface SocialVisibilityMode {
        }

        public List<Id> getSharedWith() {
            return sharedWith;
        }

        public SocialVisibility withSharedWith(List<Id> sharedWith) {
            this.sharedWith = sharedWith;
            return this;
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

        public static List<String> toList() {
            return Arrays.asList(
                    App.getContext().getResources().getStringArray(R.array.visibility_ranges));
        }

        public static String getStringRepresentation(@RangeVisibilityMode int range) {
            return App.getContext().getResources().getStringArray(R.array.visibility_ranges)[range];
        }

        public
        @RangeVisibilityMode
        int getRange() {
            return range;
        }

        public void setRange(@RangeVisibilityMode int range) {
            this.range = range;
        }

        @Override
        public String toString() {
            return RangeVisibility.getStringRepresentation(range);
        }

        @IntDef({HERE, NEAR, LOCAL, DISTANT, FAR})
        @Retention(RetentionPolicy.SOURCE)
        public @interface RangeVisibilityMode {
        }
    }
}
