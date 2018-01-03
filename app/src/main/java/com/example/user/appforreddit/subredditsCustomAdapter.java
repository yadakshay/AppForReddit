package com.example.user.appforreddit;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.user.appforreddit.Database.subredditsContract;

/**
 * Created by Akshay on 04-01-2018.
 */

public class subredditsCustomAdapter extends RecyclerView.Adapter<subredditsCustomAdapter.subredditsViewHolder>{
    private Cursor subredditsCursor;
    private showhideItemClickListener showHideClickListener;
    //interface for click listener
    public interface showhideItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }
    //constructor
    public subredditsCustomAdapter(Cursor c, showhideItemClickListener listener){
        subredditsCursor = c;
        showHideClickListener = listener;
    }
    @Override
    public subredditsCustomAdapter.subredditsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.subreddit_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        subredditsViewHolder viewHolder = new subredditsViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(subredditsCustomAdapter.subredditsViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return subredditsCursor.getCount();
    }

    public class subredditsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView subredditName;
        Button show, hide;
        FrameLayout flt;
        public subredditsViewHolder(View itemView) {
            super(itemView);
            subredditName = (TextView) itemView.findViewById(R.id.subredditName);
            show = (Button) itemView.findViewById(R.id.show);
            hide = (Button) itemView.findViewById(R.id.hide);
            flt = (FrameLayout) itemView.findViewById(R.id.containingFrame);
            flt.setOnClickListener(this);
        }

        void bind(int listIndex) {
            subredditsCursor.moveToPosition(listIndex);
            subredditName.setText(subredditsCursor.getString(subredditsCursor.getColumnIndex(subredditsContract.subredditEntry.COLUMN_SUBREDDIT_DISPLAYNAME_PREFIXED)));
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            showHideClickListener.onListItemClick(clickedPosition);
        }
    }
}
