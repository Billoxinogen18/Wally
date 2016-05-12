package com.wally.wally.userManager;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xato on 5/12/2016.
 */
public class CompoundUser implements SocialUser {
    List<AbstractSocialUser> socialUsers;

    public CompoundUser() {
        socialUsers = new ArrayList<>();
    }

    public String getId(){
        if(socialUsers.size() > 0){
            return socialUsers.get(0).getId();
        }
        return null;
    }

    public void addSocialUser(AbstractSocialUser user){
        socialUsers.add(user);
    }

    @Override
    public String getName() {
        if(socialUsers.size() > 0){
            return socialUsers.get(0).getName();
        }
        return null;
    }

    @Override
    public URL getAvatarUrl() {
        if(socialUsers.size() > 0){
            return socialUsers.get(0).getAvatarUrl();
        }
        return null;
    }

    @Override
    public URL getCoverUrl() {
        if(socialUsers.size() > 0){
            return socialUsers.get(0).getCoverUrl();
        }
        return null;
    }

    @Override
    public List<AbstractSocialUser> getFriends() {
        List<AbstractSocialUser> friends = new ArrayList<>();
        for(AbstractSocialUser user : socialUsers){
            friends.addAll(user.getFriends());
        }
        return friends;
    }
}
