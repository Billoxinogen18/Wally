package com.wally.wally.datacontroller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.wally.wally.datacontroller.user.UserManager;

import org.junit.After;
import org.junit.Before;
import java.util.Arrays;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserManagerTest {
    private DatabaseReference mockRef;
    private UserManager testSubject;

    @Before
    public void init() {
        UserInfo mockInfo = mock(UserInfo.class);
        when(mockInfo.getUid()).thenReturn("googleId");

        FirebaseUser mockUser = mock(FirebaseUser.class);
        when(mockUser.getUid()).thenReturn("firebaseId");
        doReturn(Arrays.asList(mockInfo, mockInfo))
                .when(mockUser).getProviderData();

        FirebaseAuth mockAuth = mock(FirebaseAuth.class);
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);

        mockRef = mock(DatabaseReference.class);
        when(mockRef.child(anyString())).thenReturn(mockRef);
    }


    @After
    public void finish() {}

}
