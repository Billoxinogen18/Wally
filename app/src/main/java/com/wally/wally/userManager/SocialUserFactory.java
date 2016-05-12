package com.wally.wally.userManager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.wally.wally.datacontroller.user.User;

/**
 * Created by Meravici on 5/12/2016.
 */
public class SocialUserFactory {
    public static final String TAG = SocialUserFactory.class.getSimpleName();

    public void getSocialUser(final User baseUser, final Context context, final UserLoadListener userLoadListener) {
        final CompoundUser compoundUser = new CompoundUser();
        if (baseUser.getGgId() != null) {
            getGoogleUser(baseUser, context, new UserLoadListener() {
                @Override
                public void onUserLoad(SocialUser user) {
                    compoundUser.addSocialUser(user);
                    // TODO თურამეა იომ ატვეჩაი ვარო
                    if (baseUser.getFbId() != null) {
                        compoundUser.addSocialUser(new FacebookUser(baseUser));
                    }
                    userLoadListener.onUserLoad(compoundUser);
                }
            });
        }
    }

    private void getGoogleUser(final User baseUser, Context context, final UserLoadListener userLoadListener) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        Plus.PeopleApi.load(googleApiClient, baseUser.getGgId()).setResultCallback(
                new ResultCallback<People.LoadPeopleResult>() {
                    @Override
                    public void onResult(@NonNull People.LoadPeopleResult peopleData) {
                        if (peopleData.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {
                            PersonBuffer personBuffer = peopleData.getPersonBuffer();
                            try {
                                Person person = personBuffer.get(0);

                                SocialUser googleUser = new GoogleUser(baseUser)
                                        .withName(person.getDisplayName())
                                        .withAvatar(person.getImage().getUrl())
                                        .withCover(person.getCover().getCoverPhoto().getUrl());

                                userLoadListener.onUserLoad(googleUser);
                            } finally {
                                personBuffer.release();
                            }
                        } else {
                            userLoadListener.onUserLoad(null);
                            Log.e(TAG, "Error requesting people data: " + peopleData.getStatus());
                        }
                    }
        });
    }

    public interface UserLoadListener {
        void onUserLoad(SocialUser user);
    }
}
