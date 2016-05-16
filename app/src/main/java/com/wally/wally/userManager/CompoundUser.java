package com.wally.wally.userManager;

import android.os.AsyncTask;

import com.wally.wally.datacontroller.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Meravici on 5/12/2016.
 */
public class CompoundUser implements SocialUser {
    List<SocialUser> socialUsers;

    public CompoundUser() {
        socialUsers = new ArrayList<>();
    }

    public User getBaseUser() {
        if (socialUsers.size() > 0) {
            return socialUsers.get(0).getBaseUser();
        }
        return null;
    }

    public void addSocialUser(SocialUser user) {
        socialUsers.add(user);
    }

    @Override
    public String getName() {
        if (socialUsers.size() > 0) {
            return socialUsers.get(0).getName();
        }
        return null;
    }

    @Override
    public String getAvatarUrl() {
        if (socialUsers.size() > 0) {
            return socialUsers.get(0).getAvatarUrl();
        }
        return null;
    }

    @Override
    public String getCoverUrl() {
        if (socialUsers.size() > 0) {
            return socialUsers.get(0).getCoverUrl();
        }
        return null;
    }

    @Override
    public void getFriends(final FriendsLoadListener friendsLoadListener) {
        new AsyncTask<Void, Void, List<SocialUser>>() {
            @Override
            protected List<SocialUser> doInBackground(Void... params) {
                final List<SocialUser> result = new ArrayList<>();
                CountDownLatch latch = new CountDownLatch(socialUsers.size());
                for (SocialUser user : socialUsers) {
                    user.getFriends(new FriendsLoadListener() {
                        @Override
                        public void onFriendsLoad(List<SocialUser> friends) {
                            updateFriends(result, friends);
                        }
                    });
                }
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(List<SocialUser> result) {
                super.onPostExecute(socialUsers);
                friendsLoadListener.onFriendsLoad(result);
            }
        };
    }

    @Override
    public SocialUser withName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SocialUser withAvatar(String avatarUrl) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SocialUser withCover(String coverUrl) {
        throw new UnsupportedOperationException();
    }

    private synchronized void updateFriends(List<SocialUser> dest, List<SocialUser> src) {
        dest.addAll(src);
    }
}