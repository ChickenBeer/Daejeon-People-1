package com.daejeonpeople.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.Toast;

import com.daejeonpeople.R;
import com.daejeonpeople.activities.base.BaseActivity;
import com.daejeonpeople.adapter.CustomAdapter;
import com.daejeonpeople.adapter.CustomsAdapter;
import com.daejeonpeople.support.database.DBHelper;

//동규

public class Main extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);


        //툴바 생성 액션바 대신 툴바 사용합니다.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //Bottom tabwidget & tabhost Code
        TabHost tabHost = (TabHost) findViewById(R.id.tabHost1);
        tabHost.setup();

        //메인으로 위젯 생성
        TabHost.TabSpec ts1 = tabHost.newTabSpec("Tab1");
        ts1.setIndicator("메인으로", ContextCompat.getDrawable( getApplicationContext() ,R.drawable.ic_home));
        ts1.setContent(R.id.tab1);
        tabHost.addTab(ts1);

        //카테고리 위젯 생성
        TabHost.TabSpec ts2 = tabHost.newTabSpec("Tab2");
        ts2.setIndicator("카테고리", ContextCompat.getDrawable( getApplicationContext() ,R.drawable.ic_category));
        ts2.setContent(R.id.tab2);
        tabHost.addTab(ts2);
        //tabwidget에 사진 넣기가 너무 어렵네요.ㅠㅠ 죄송합니다.
        tabHost.setCurrentTab(0);


        //Advertising ViewPager
        pager = (ViewPager) findViewById(R.id.pager1);
        CustomAdapter adapter = new CustomAdapter(getLayoutInflater());
        pager.setAdapter(adapter);

        //이달의 인기만점 ViewPager
        pager = (ViewPager) findViewById(R.id.pager2);
        CustomsAdapter adapter2 = new CustomsAdapter(getLayoutInflater());
        pager.setAdapter(adapter2);

        //이달이 가볼만한 곳 ViewPager
        pager = (ViewPager) findViewById(R.id.pager3);
        CustomsAdapter adapter3 = new CustomsAdapter(getLayoutInflater());
        pager.setAdapter(adapter3);

        DBHelper dbHelper = DBHelper.getInstance(getApplicationContext(), "CHECK.db", null, 1);

        //로그인이 되어 있는 경우
        if(dbHelper.getCookie() != null) {
            //사이드메뉴 생성
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
        } //로그인이 되어 있지 않은 경우
          else {
            //사이드메뉴 생성
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
        }
    }

    //Navigation onBackPressed
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // 네비게이션 뷰 조작
        int id = item.getItemId();
        if (id == R.id.navigation_item01) {
            //내 정보
            //Myinfo로 이동 (XML 구현 미완료)
            Toast.makeText(this, "내 정보", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.navigation_item02) {
            //설정
            //어디로 가는지 알 수 가 없음
            Toast.makeText(this, "설정", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_sub_menu_item01) {
            //위시리스트
            //WishList로 이동 (XML 구현 미완료)
            Toast.makeText(this, "위시리스트", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_sub_menu_item02) {
            //친구 목록
            //AddressBook으로 이동
            Intent intent = new Intent(Main.this, AddressBook.class);
            startActivity(intent);
        } else if (id == R.id.nav_sub_menu_item03) {
            //최근여행지
            //recent_destinations로 이동 (XML 구현 미완료)
            Toast.makeText(this, "최근 여행지", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_sub_menu_item04) {
            //활성화된 여행
            //Chat List로 이동 (오류 발생)
            Toast.makeText(this, "활성화된 여행", Toast.LENGTH_SHORT).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}