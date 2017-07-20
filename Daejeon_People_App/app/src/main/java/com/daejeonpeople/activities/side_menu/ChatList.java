package com.daejeonpeople.activities.side_menu;

import android.app.ActionBar;
import android.os.Bundle;

import com.daejeonpeople.R;
import com.daejeonpeople.activities.base.BaseActivity;
//민지

public class ChatList extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_list_listview);

        final ActionBar chatting = getActionBar();
        chatting.setCustomView(R.layout.custom_chat_list);
        chatting.setDisplayShowTitleEnabled(false);
        chatting.setDisplayShowCustomEnabled(true);
        chatting.setDisplayShowHomeEnabled(false);
    }
}
