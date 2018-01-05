package com.example.user.appforreddit;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.user.appforreddit.Database.articleContract;
import com.squareup.picasso.Picasso;

/**
 * Created by user on 04-01-2018.
 */

public class mainArticleAapter extends RecyclerView.Adapter<mainArticleAapter.mainArticleViewHolder> {
    private Cursor articlesCursor;
    private dismissItemClickListener mItemClickListener;
    private Context context;
    //interface for click listener
    public interface dismissItemClickListener {
        void onListItemClick(String clickedArticleId, String ShoworHide);
    }

    //constructor
    public mainArticleAapter(Cursor c, dismissItemClickListener clkListener, Context contxt){
        articlesCursor = c;
        mItemClickListener = clkListener;
        context = contxt;
    }

    @Override
    public mainArticleAapter.mainArticleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.article_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        mainArticleViewHolder viewHolder = new mainArticleViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(mainArticleAapter.mainArticleViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return articlesCursor.getCount();
    }

    public class mainArticleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView articleTitleHolder, subredditNameHolder, articleExtraHolder;
        ImageView thumbNailView;
        Button crossButton;
        ProgressBar spinner;
        LinearLayout articleContainer;
        FrameLayout articleFrame;

        public mainArticleViewHolder(View itemView) {
            super(itemView);
            subredditNameHolder = (TextView) itemView.findViewById(R.id.subredditNameHolder);
            articleTitleHolder = (TextView) itemView.findViewById(R.id.articleTitleHolder);
            crossButton = (Button) itemView.findViewById(R.id.xButton);
            thumbNailView = (ImageView) itemView.findViewById(R.id.articleThumbnail);
            spinner = (ProgressBar) itemView.findViewById(R.id.loading_spinner);
            articleContainer = (LinearLayout) itemView.findViewById(R.id.articleHolder);
            articleFrame = (FrameLayout) itemView.findViewById(R.id.articleFrameLayout);
        }

        void bind(int listIndex) {
            articlesCursor.moveToPosition(listIndex);
            spinner.setVisibility(View.GONE);
            articleFrame.setVisibility(View.VISIBLE);
            subredditNameHolder.setText(articlesCursor.getString(articlesCursor.getColumnIndex(articleContract.articleEntry.COLUMN_SUBREDDIT_URL)));
            String imagePath = articlesCursor.getString(articlesCursor.getColumnIndex(articleContract.articleEntry.COLUMN_IMAGE_THUMB));
            if(imagePath != null && !imagePath.matches("")) {
                Picasso.with(context).load(imagePath).into(thumbNailView);
            }else{
                thumbNailView.setVisibility(View.INVISIBLE);
            }
            articleTitleHolder.setText(articlesCursor.getString(articlesCursor.getColumnIndex(articleContract.articleEntry.COLUM_ARTICLE_TITLE)));
            crossButton.setOnClickListener(this);
            articleContainer.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            articlesCursor.moveToPosition(clickedPosition);
            if(v == crossButton) {
                spinner.setVisibility(View.VISIBLE);
                articleFrame.setVisibility(View.GONE);
                String id = articlesCursor.getString(articlesCursor.getColumnIndex(articleContract.articleEntry.COLUMN_ARTICLE_ID));
                String subUrl = articlesCursor.getString(articlesCursor.getColumnIndex(articleContract.articleEntry.COLUMN_SUBREDDIT_URL));
                mItemClickListener.onListItemClick(id, subUrl);
            }else if(v == articleContainer){
                mItemClickListener.onListItemClick(null,
                        articlesCursor.getString(articlesCursor.getColumnIndex(articleContract.articleEntry.COLUMN_ARTICLE_URL)));
            }
        }
    }
    public void swapArticleCursor(Cursor newCursor) {
        // Always close the previous mCursor first
        if (articlesCursor != null) articlesCursor.close();
        articlesCursor = newCursor;
        if (newCursor != null) {
            // Force the RecyclerView to refresh
            this.notifyDataSetChanged();
        }
    }
}
