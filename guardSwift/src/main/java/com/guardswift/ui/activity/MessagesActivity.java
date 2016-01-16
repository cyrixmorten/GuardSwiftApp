//package com.guardswift.ui.activity;
//
//import android.os.Bundle;
//import android.view.MenuItem;
//
//import com.guardswift.R;
//import com.guardswift.dagger.InjectingAppCompatActivity;
//import com.guardswift.ui.fragments.MessagesFragment;
//import com.guardswift.persistence.parse.data.message.MessagesHolder;
//
//public class MessagesActivity extends InjectingAppCompatActivity {
//
//
//    android.support.v7.app.ActionBar actionBar;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        setContentView(R.layout.activity_empty);
//
//        actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayShowHomeEnabled(true);
//            actionBar.setTitle(getString(R.string.title_messages));
//        }
//
//        MessagesHolder messagesHolder = MessagesHolder.Recent.getSelected();
//        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, MessagesFragment.newInstance(messagesHolder)).commit();
//
//    }
//
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                onActionFinish();
//                return true;
//
//
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//}
