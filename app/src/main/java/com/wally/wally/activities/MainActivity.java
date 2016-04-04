package com.wally.wally.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.wally.wally.R;
import com.wally.wally.components.FloatingActionMenu;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_CHOOSE_PHOTO = 129;
    private FloatingActionMenu mFloatingActionMenu;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFloatingActionMenu = (FloatingActionMenu) findViewById(R.id.fam);
        mFloatingActionMenu.setupWithDimmingView(findViewById(R.id.dimming_view),
                ContextCompat.getColor(this, R.color.fam_dim_color));

        View btnMap = findViewById(R.id.btn_map);
        assert btnMap != null;
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(getBaseContext(), MapsActivity.class);
                startActivity(mapIntent);
            }
        });
    }


    public void newNote(View v) {
        mFloatingActionMenu.collapse(true);
        Toast.makeText(this, "not implemented yet", Toast.LENGTH_SHORT).show();
    }

    public void newPhoto(View v) {
        mFloatingActionMenu.collapse(false);
        startActivityForResult(ChoosePhotoActivity.newIntent(this), REQUEST_CODE_CHOOSE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE_PHOTO) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "onActivityResult() called with: " + "requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
            } else {
                // TODO user canceled or error happened.
            }
        }
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
}
