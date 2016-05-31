package com.wally.wally.userManager;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.wally.wally.datacontroller.user.Id;
import com.wally.wally.datacontroller.user.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SocialUserFactory {
    public static final String TAG = SocialUserFactory.class.getSimpleName();
    private static final int DEFAULT_AVATAR_SIZE = 256;
    private Map<Id, SocialUser> userCache;

    public SocialUserFactory() {
        userCache = new HashMap<>();
    }

    public void getSocialUser(final User baseUser, final GoogleApiClient googleApiClient, final UserManager.UserLoadListener userLoadListener) {
        final CompoundUser compoundUser = new CompoundUser();
        if (userCache.containsKey(baseUser.getId())) {
            userLoadListener.onUserLoad(userCache.get(baseUser.getId()));
        } else if (baseUser.getGgId() != null) {
            getGoogleUser(baseUser, googleApiClient, new UserManager.UserLoadListener() {
                @Override
                public void onUserLoad(SocialUser user) {
                    compoundUser.addSocialUser(user);
                    // TODO თურამეა იომ ატვეჩაი ვარო
                    // if (baseUser.getFbId() != null) {
                    // compoundUser.addSocialUser(new FacebookUser(baseUser));
                    // }
                    userCache.put(baseUser.getId(), compoundUser);
                    userLoadListener.onUserLoad(compoundUser);
                }
            });
        }
    }

    private void getGoogleUser(final User baseUser, final GoogleApiClient googleApiClient, final UserManager.UserLoadListener userLoadListener) {
        Plus.PeopleApi.load(googleApiClient, baseUser.getGgId().getId()).setResultCallback(
                new ResultCallback<People.LoadPeopleResult>() {
                    @Override
                    public void onResult(@NonNull People.LoadPeopleResult peopleData) {
                        if (peopleData.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {
                            PersonBuffer personBuffer = peopleData.getPersonBuffer();
                            try {
                                Person person = personBuffer.get(0);
                                Log.d(TAG, "onResult: " + person.getDisplayName());
                                final SocialUser googleUser = toSocialUser(baseUser, person);

                                Plus.PeopleApi.loadVisible(googleApiClient, null).setResultCallback(
                                        new ResultCallback<People.LoadPeopleResult>() {
                                            @Override
                                            public void onResult(@NonNull People.LoadPeopleResult peopleData) {
                                                PersonBuffer personBuffer = peopleData.getPersonBuffer();
                                                try {
                                                    List<SocialUser> friends = new ArrayList<>();
                                                    for (Person person : personBuffer) {
                                                        friends.add(toSocialUser(null, person));
                                                    }
                                                    googleUser.withFriends(friends);
                                                    userLoadListener.onUserLoad(googleUser);
                                                } finally {
                                                    personBuffer.release();
                                                }
                                            }
                                        });
                            } finally {
                                personBuffer.release();
                            }
                        } else {
                            Log.e(TAG, "onResult: Error requesting people data" + peopleData.getStatus());
                            userLoadListener.onUserLoad(new DummyUser(baseUser));
                        }
                    }
                });
    }

    private SocialUser toSocialUser(User baseUser, Person person){
        Log.d(TAG, "toSocialUser() called with: " + "person = [" + person.getDisplayName() + "]");
        SocialUser socialUser = new GoogleUser(baseUser)
                .withDisplayName(person.getDisplayName())
                .withAvatar(person.getImage().getUrl() + "&sz=" + DEFAULT_AVATAR_SIZE);

        if(person.hasCover() && person.getCover().hasCoverPhoto()){
            socialUser.withCover(person.getCover().getCoverPhoto().getUrl());
        }

        if(person.hasName()){
            socialUser.withFirstName(person.getName().getGivenName());
        }

        return socialUser;
    }
}
