package com.wally.wally.datacontroller;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.ContentManager;
import com.wally.wally.datacontroller.content.FirebaseContent;
import com.wally.wally.datacontroller.content.Visibility;
import com.wally.wally.datacontroller.firebase.FirebaseObject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import java.net.SocketAddress;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class ContentManagerTest {
    private static final String TEST_ID = "Test_Id";
    private ContentManager testSubject;

    private DatabaseReference rooms;
    private StorageReference storage;
    private DatabaseReference contents;

    @Before
    public void init() {
        storage = mock(StorageReference.class);
        when(storage.child(anyString())).thenReturn(storage);
        rooms = mock(DatabaseReference.class);
        when(rooms.child(anyString())).thenReturn(rooms);

        contents = mock(DatabaseReference.class);
        when(contents.child(anyString())).thenReturn(contents);
        when(contents.push()).thenReturn(contents);
        when(contents.getParent()).thenReturn(contents);

        testSubject = new ContentManager(rooms, contents, storage);
    }


    @After
    public void finish() {}

    @Test
    public void saveTest() {
        final Content c = DebugUtils.generateRandomContent();
        when(contents.getKey()).thenReturn(TEST_ID);
        testSubject.save(c);
        assertEquals(TEST_ID, c.getId());
        ArgumentCaptor<FirebaseObject> captor =
                ArgumentCaptor.forClass(FirebaseObject.class);
        verify(contents).setValue(captor.capture());
        FirebaseContent object = (FirebaseContent) captor.getValue();
        assertEquals(c.getTitle(), object.getTitle());
        assertEquals(c.getNote(), object.getNote());
    }

    @Test
    public void savePublicTest() {
        final Content c = DebugUtils.generateRandomContent();
        c.getVisibility().withSocialVisibility(Visibility.PUBLIC);
        testSubject.save(c);
        verify(contents, never()).child(TEST_ID);
        verify(contents).push();
        verify(contents).child("Public");
    }

    @Test
    public void savePrivateTest() {
        final Content c = DebugUtils.generateRandomContent();
        c.getVisibility().withSocialVisibility(Visibility.PRIVATE);
        testSubject.save(c);
        verify(contents, never()).child(TEST_ID);
        verify(contents, times(1)).push();
        verify(contents).child(c.getAuthorId());
    }

    @Test
    public void saveSharedTest() {
        final Content c = DebugUtils.generateRandomContent();
        Visibility.SocialVisibility shared =
                new Visibility.SocialVisibility(Visibility.SocialVisibility.PEOPLE);
        c.getVisibility().withSocialVisibility(shared);
        testSubject.save(c);
        verify(contents, never()).child(TEST_ID);
        verify(contents, times(1)).push();
        verify(contents).child("Shared");
    }
}
