package com.wally.wally.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.atap.tangoservice.Tango;
import com.wally.wally.Fragments.NewContentDialogFragment;
import com.wally.wally.R;
import com.wally.wally.dal.Content;

public class MainActivity extends AppCompatActivity implements NewContentDialogFragment.NewContentDialogListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View btnMap = findViewById(R.id.btn_map);
        assert btnMap != null;
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(getBaseContext(), MapsActivity.class);
                startActivity(mapIntent);
            }
        });
        Tango t = new Tango(this);
    }

    @SuppressLint("InflateParams")
    public void newContent(View v) {
        NewContentDialogFragment dialog = new NewContentDialogFragment();
        dialog.show(getSupportFragmentManager(), "NewContentDialogFragment");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onContentCreated(Content content) {
        Log.d(TAG, "onContentCreated() called with: " + "content = [" + content + "]");
    }
}
