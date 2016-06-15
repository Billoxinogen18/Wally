package com.wally.wally.datacontroller.content;

import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;

import com.wally.wally.App;
import com.wally.wally.R;
import com.wally.wally.datacontroller.user.Id;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Visibility implements Serializable {

    private SocialVisibility socialVisibility;
    private Date visibleUntil;
    private boolean isAuthorAnonymous;
    private boolean isPreviewVisible;

    public Visibility() {
        socialVisibility = new SocialVisibility(SocialVisibility.PUBLIC);
        isPreviewVisible = true;
        isAuthorAnonymous = false;
    }

    public Visibility withSocialVisibility(SocialVisibility socialVisibility) {
        this.socialVisibility = socialVisibility;
        return this;
    }

    public Visibility withTimeVisibility(Date visibleUntil) {
        this.visibleUntil = visibleUntil;
        return this;
    }

    public Visibility withAuthorAnonymous(boolean isAuthorAnonymous) {
        this.isAuthorAnonymous = isAuthorAnonymous;
        return this;
    }

    public Visibility withVisiblePreview(boolean isPreviewVisible) {
        this.isPreviewVisible = isPreviewVisible;
        return this;
    }

    public SocialVisibility getSocialVisibility() {
        return socialVisibility;
    }

    public Date getVisibleUntil() {
        return visibleUntil;
    }

    public boolean isAuthorAnonymous() {
        return isAuthorAnonymous;
    }

    public boolean isPreviewVisible() {
        return isPreviewVisible;
    }

    @Override
    public String toString() {
        return "Visibility{" +
                "socialVisibility=" + socialVisibility +
                ", visibleUntil=" + visibleUntil +
                ", isAuthorAnonymous=" + isAuthorAnonymous +
                ", isPreviewVisible=" + isPreviewVisible +
                '}';
    }

    public static class SocialVisibility implements Serializable {
        public static final int PRIVATE = 0;
        public static final int PUBLIC = 1;
        public static final int PEOPLE = 2;

        private int mode;
        private List<Id> sharedWith;

        public SocialVisibility(@SocialVisibilityMode int mode) {
            sharedWith = new ArrayList<>();
            setMode(mode);
        }


        public static int getSize() {
            return 3;
        }

        public static String getStringRepresentation(@SocialVisibilityMode int mode) {
            return App.getContext().getResources().getStringArray(R.array.social_visibility)[mode];
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

        public List<Id> getSharedWith() {
            return sharedWith;
        }

        public SocialVisibility withSharedWith(List<Id> sharedWith) {
            this.sharedWith = sharedWith;
            return this;
        }

        @IntDef({PRIVATE, PUBLIC, PEOPLE})
        @Retention(RetentionPolicy.SOURCE)
        public @interface SocialVisibilityMode {
        }
    }
}
