package com.example.user.appforreddit;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.user.appforreddit.Database.articleContract;

/**
 * Created by user on 06-01-2018.
 */

public class stackWidgetService extends RemoteViewsService {

    public stackWidgetService() {
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }
    class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
        private Context mContext;
        Cursor c;
        public StackRemoteViewsFactory(Context applicationContext, Intent intent) {
            mContext = applicationContext;
        }
        @Override
        public void onCreate() {

        }

        @Override
        public void onDataSetChanged() {
            Uri queryUri = articleContract.articleEntry.CONTENT_URI;
           c = getApplicationContext().getContentResolver().query(queryUri, null, null, null, null);
        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            return c.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_stack_item);
            if (position <= getCount()) {
                c.moveToPosition(position);
                String subUrl = c.getString(c.getColumnIndex(articleContract.articleEntry.COLUMN_SUBREDDIT_URL));
                String title = c.getString(c.getColumnIndex(articleContract.articleEntry.COLUM_ARTICLE_TITLE));
                rv.setTextViewText(R.id.stackWidgetItemTitle, subUrl);
                rv.setTextViewText(R.id.stackWidgetItemBody, title);
            }
            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}
