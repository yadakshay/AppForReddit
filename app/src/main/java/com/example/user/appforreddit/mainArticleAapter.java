package com.example.user.appforreddit;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by user on 04-01-2018.
 */

public class mainArticleAapter extends RecyclerView.Adapter<mainArticleAapter.mainArticleViewHolder> {
    //interface for click listener
    public interface dissmissItemClickListener {
        void onListItemClick(String clickedItemId, String ShoworHide);
    }
    @Override
    public mainArticleAapter.mainArticleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(mainArticleAapter.mainArticleViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class mainArticleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public mainArticleViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
