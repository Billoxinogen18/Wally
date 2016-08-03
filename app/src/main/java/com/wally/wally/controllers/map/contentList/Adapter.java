package com.wally.wally.controllers.map.contentList;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.GoogleApiClient;
import com.wally.wally.R;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.userManager.SocialUser;

/**
 * Created by Meravici on 6/20/2016. yea
 */
public class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements PagingRetriever.ContentPageRetrieveListener {
    public static final String TAG = Adapter.class.getSimpleName();
    private static final int PROGRESS_VIEW_TYPE = 73;

    private PagingRetriever dataSource;
    private GoogleApiClient googleApiClient;
    private Context context;
    private SocialUser userProfile;
    private ViewItem.OnClickListener onClickListener;


    private boolean hasNext = true;

    private Handler mainHandler;


    public Adapter(Context context, GoogleApiClient googleApiClient, PagingRetriever dataSource) {
        this.context = context;
        this.googleApiClient = googleApiClient;
        this.dataSource = dataSource;

        this.mainHandler = new Handler(Looper.getMainLooper());
        dataSource.registerLoadListener(this);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return PROGRESS_VIEW_TYPE;
        }

        return 0;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == PROGRESS_VIEW_TYPE) {
            View v = LayoutInflater
                    .from(context).inflate(R.layout.maps_content_list_progress_item, parent, false);
            return new ProgressViewHolder(v);
        } else {
            View v = LayoutInflater.from(context).inflate(R.layout.maps_content_list_item, parent, false);
            return new MainListItemViewHolder(v);
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position == getItemCount() - 1) {
            ProgressViewHolder mHolder = (ProgressViewHolder) holder;
            mHolder.toHide.setVisibility(hasNext ? View.VISIBLE : View.GONE);
        } else {
            MainListItemViewHolder mHolder = (MainListItemViewHolder) holder;
            Content content = dataSource.get(position);
            mHolder.viewItem.clear();
            mHolder.viewItem.setContent(content, googleApiClient);
            mHolder.viewItem.showUserInfo(userProfile == null);
            mHolder.viewItem.setTitle("" + (position + 1));
            mHolder.viewItem.setOnClickListener(onClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return dataSource.size() + 1;
    }

    public void setOnClickListener(ViewItem.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }


    @Override
    public void onNextPageLoad(final int pageLength) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                notifyItemRangeInserted(getItemCount(), pageLength);
            }
        });
    }

    @Override
    public void onNextPageFail() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                hasNext = false;
                notifyItemChanged(getItemCount() - 1);
            }
        });

    }

    public void setUserProfile(SocialUser userProfile) {
        this.userProfile = userProfile;
    }

    private class ProgressViewHolder extends RecyclerView.ViewHolder {
        View toHide;

        public ProgressViewHolder(View itemView) {
            super(itemView);
            toHide = itemView.findViewById(R.id.progressbar);
        }
    }

    private class MainListItemViewHolder extends RecyclerView.ViewHolder {
        public ViewItem viewItem;

        public MainListItemViewHolder(View itemView) {
            super(itemView);
            viewItem = (ViewItem) itemView.findViewById(R.id.card);
        }
    }

}
