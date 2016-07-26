package com.wally.wally.components;

import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;

import com.wally.wally.R;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.Visibility;

public class ContentMetaInfoPopup extends RevealPopup {


    private Content mContent;

    public void show(View anchor, Content content) {
        setUp(anchor, R.layout.content_meta_info_dialog);
        mContent = content;
        initViews(mContentLayout);
    }

    private void initViews(View v) {
        SwitchCompat anonymousAuthor = (SwitchCompat) v.findViewById(R.id.switch_anonymous_author);
        anonymousAuthor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mContent.getVisibility().withAnonymousAuthor(isChecked);
            }
        });

        // init map preview
        SwitchCompat preview = (SwitchCompat) v.findViewById(R.id.switch_map_preview);
        preview.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mContent.getVisibility().withVisiblePreview(isChecked);
            }
        });

        // Init views from model
        Visibility visibility = mContent.getVisibility();
        anonymousAuthor.setChecked(visibility.isAuthorAnonymous());
        preview.setChecked(visibility.isPreviewVisible());
    }


    @Override
    protected void onDismiss() {

    }
}
