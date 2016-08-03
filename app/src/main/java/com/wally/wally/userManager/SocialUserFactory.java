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
    private Map<Id, SocialUser> userCache;

    public SocialUserFactory() {
        userCache = new HashMap<>();
    }

    public void getSocialUser(final User baseUser, final GoogleApiClient googleApiClient, final SocialUserManager.UserLoadListener userLoadListener) {
        final CompoundUser compoundUser = new CompoundUser();
        if (userCache.containsKey(baseUser.getId())) {
            userLoadListener.onUserLoad(userCache.get(baseUser.getId()));
        } else if (baseUser.getGgId() != null) {
            getGoogleUser(baseUser, googleApiClient, new SocialUserManager.UserLoadListener() {
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

                @Override
                public void onUserLoadFailed() {
                    userLoadListener.onUserLoadFailed();
                }
            });
        }
    }

    private void getGoogleUser(final User baseUser, final GoogleApiClient googleApiClient, final SocialUserManager.UserLoadListener userLoadListener) {
        Plus.PeopleApi.load(googleApiClient, baseUser.getGgId().getId()).setResultCallback(
                new ResultCallback<People.LoadPeopleResult>() {
                    @Override
                    public void onResult(@NonNull People.LoadPeopleResult peopleData) {
                        if (peopleData.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {
                            PersonBuffer personBuffer = peopleData.getPersonBuffer();
                            if(personBuffer != null) {
                                try {
                                    Person person = personBuffer.get(0);
                                    final SocialUser googleUser = toSocialUser(baseUser, person);

                                    Plus.PeopleApi.loadVisible(googleApiClient, null).setResultCallback(
                                            new ResultCallback<People.LoadPeopleResult>() {
                                                @Override
                                                public void onResult(@NonNull People.LoadPeopleResult peopleData) {
                                                    PersonBuffer personBuffer = peopleData.getPersonBuffer();
                                                    if (personBuffer != null) {
                                                        try {
                                                            List<SocialUser> friends = new ArrayList<>();
                                                            for (Person person : personBuffer) {
                                                                friends.add(toSocialUser(new User(null).withGgId(person.getId()), person));
                                                            }
                                                            googleUser.withFriends(friends);
                                                        } finally {
                                                            personBuffer.release();
                                                        }
                                                    }
                                                    userLoadListener.onUserLoad(googleUser);
                                                }
                                            });
                                } finally {
                                    personBuffer.release();
                                }
                            }else{
                                userLoadListener.onUserLoadFailed();
                            }
                        } else {
                            Log.e(TAG, "onResult: Error requesting people data" + peopleData.getStatus());
                            userLoadListener.onUserLoadFailed();
                        }
                    }
                });
    }

    private SocialUser toSocialUser(User baseUser, Person person) {
        SocialUser socialUser = new GoogleUser(baseUser);

        if (person.hasDisplayName())
            socialUser.withDisplayName(person.getDisplayName());
        if (person.hasImage())
            socialUser.withAvatar(person.getImage().getUrl());

        if (person.hasCover() && person.getCover().hasCoverPhoto()) {
            socialUser.withCover(person.getCover().getCoverPhoto().getUrl());
        }

        if (person.hasName() && person.getName().hasGivenName()) {
            socialUser.withFirstName(person.getName().getGivenName());
        }

        return socialUser;
    }
}
