package com.wally.wally.userManager;

import com.wally.wally.datacontroller.user.User;

/**
 * Created by Meravici on 5/12/2016.
 */
public class SocialUserFactory {
    public static SocialUser getSocialUser(User baseUser){
        CompoundUser compoundUser = new CompoundUser();

        if(baseUser.getGgId() != null){
            compoundUser.addSocialUser(new GoogleUser(baseUser));
        }

        if(baseUser.getFbId() != null){
            compoundUser.addSocialUser(new FacebookUser(baseUser));
        }

        return compoundUser;
    }
}
