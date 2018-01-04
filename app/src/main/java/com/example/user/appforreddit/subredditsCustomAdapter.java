package com.example.user.appforreddit;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
        void onListItemClick(String clickedItemId, String ShoworHide);
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

        public subredditsViewHolder(View itemView) {
            super(itemView);
            subredditName = (TextView) itemView.findViewById(R.id.subredditName);
            show = (Button) itemView.findViewById(R.id.show);
            hide = (Button) itemView.findViewById(R.id.hide);
            //flt = itemView.findViewById(R.id.containingFrame);
            show.setOnClickListener(this);
            hide.setOnClickListener(this);
        }

        void bind(int listIndex) {
            subredditsCursor.moveToPosition(listIndex);
            subredditName.setText(subredditsCursor.getString(subredditsCursor.getColumnIndex(subredditsContract.subredditEntry.COLUMN_SUBREDDIT_DISPLAYNAME_PREFIXED)));
            String displayPref = subredditsCursor.getString(subredditsCursor.getColumnIndex(subredditsContract.subredditEntry.COLUMN_DISPLAY_SUBREDDIT));
            if(displayPref.matches("show")){
                hide.setVisibility(View.GONE);
                show.setVisibility(View.VISIBLE);
            }else{
                show.setVisibility(View.GONE);
                hide.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onClick(View v) {
            String SorH, id;
            if((Button) v == show){
                SorH= "show";
            }else{
                SorH="hide";
            }
            int clickedPosition = getAdapterPosition();
            subredditsCursor.moveToPosition(clickedPosition);
            id = subredditsCursor.getString(subredditsCursor.getColumnIndex(subredditsContract.subredditEntry.COLUMN_SUBREDDIT_ID));
            showHideClickListener.onListItemClick(id, SorH);
        }
    }
    public void swapCursor(Cursor newCursor) {
        // Always close the previous mCursor first
        if (subredditsCursor != null) subredditsCursor.close();
        subredditsCursor = newCursor;
        if (newCursor != null) {
            // Force the RecyclerView to refresh
            this.notifyDataSetChanged();
        }
    }
}
