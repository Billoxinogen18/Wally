package com.wally.wally.endlessScroll;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.GoogleApiClient;
import com.wally.wally.R;
import com.wally.wally.components.ContentListViewItem;
import com.wally.wally.datacontroller.content.Content;

/**
 * Created by Meravici on 6/20/2016. yea
 */
public class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ContentPagingRetriever.ContentPageRetrieveListener {
    private static final String TAG = MainAdapter.class.getSimpleName();
    private static final int PROGRESS_VIEW_TYPE = 73;

    private ContentPagingRetriever dataSource;
    private GoogleApiClient googleApiClient;
    private Context context;
    private ContentListViewItem.OnClickListener onClickListener;

    private boolean hasNext = true;
    private boolean hasPrevious = false;

    private Handler mainHandler;

    public MainAdapter(Context context, GoogleApiClient googleApiClient, ContentPagingRetriever dataSource) {
        this.context = context;
        this.googleApiClient = googleApiClient;
        this.dataSource = dataSource;

        this.mainHandler = new Handler(Looper.getMainLooper());
        dataSource.registerLoadListener(this);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1 || position == 0) {
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

            ContentListViewItem contentListViewItem = new ContentListViewItem(context);
            return new MainListItemViewHolder(context, contentListViewItem);
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (position == 0) {
            ProgressViewHolder mHolder = (ProgressViewHolder) holder;
            mHolder.toHide.setVisibility(hasPrevious ? View.VISIBLE : View.GONE);
        } else if (position == getItemCount() - 1) {
            ProgressViewHolder mHolder = (ProgressViewHolder) holder;
            mHolder.toHide.setVisibility(hasNext ? View.VISIBLE : View.GONE);
        } else {
            MainListItemViewHolder mHolder = (MainListItemViewHolder) holder;
            Content content = dataSource.get(position - 1);
            mHolder.contentListViewItem.clear();
            mHolder.contentListViewItem.setContent(content, googleApiClient);
            mHolder.contentListViewItem.setPosition(position);
            mHolder.contentListViewItem.setOnClickListener(onClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return dataSource.size() + 2;
    }

    public void setOnClickListener(ContentListViewItem.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public void onInit() {
        Log.d(TAG, "onInit: ");
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onFail() {

    }

    @Override
    public void onBeforeNextPageLoad() {

    }

    @Override
    public void onNextPageLoad(final int pageLength) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                hasPrevious = true;
                notifyItemRangeRemoved(0, dataSource.pageLength);
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

    @Override
    public void onBeforePreviousPageLoad() {

    }

    @Override
    public void onPreviousPageLoad(int pageLength) {
        Log.d(TAG, "onPreviousPageLoad: ");
        hasNext = true;
        notifyItemRangeInserted(0, pageLength);
        notifyItemRangeRemoved(getItemCount() - 1, dataSource.pageLength);
    }

    @Override
    public void onPreviousPageFail() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                hasPrevious = false;
                notifyItemChanged(0);
            }
        });
    }

    public class ProgressViewHolder extends RecyclerView.ViewHolder {
        View toHide;
        public ProgressViewHolder(View itemView) {
            super(itemView);
            toHide = itemView.findViewById(R.id.progressbar);
        }
    }

}
