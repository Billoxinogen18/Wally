package com.wally.wally.components;

import com.wally.wally.userManager.SocialUser;

/**
 * Interface to listen user select/deselect
 * Created by Meravici on 6/1/2016.
 */
public interface UserSelectListener {
    void onUserSelect(SocialUser user);
    void onUserDeselect(SocialUser user);
}
