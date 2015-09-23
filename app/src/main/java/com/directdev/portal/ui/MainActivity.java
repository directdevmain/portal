package com.directdev.portal.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;

import com.directdev.portal.R;
import com.directdev.portal.tools.database.CourseDB;
import com.directdev.portal.tools.event.GradesResponseEvent;
import com.directdev.portal.tools.event.TermResponseEvent;
import com.directdev.portal.tools.fetcher.FetchScore;
import com.directdev.portal.tools.uihelper.MainViewPagerAdapter;
import com.directdev.portal.ui.access.LoginActivity;
import com.directdev.portal.ui.access.LoginAuthorization;
import com.directdev.portal.ui.journal.JournalFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import de.greenrobot.event.EventBus;


public class MainActivity  extends AppCompatActivity {
    private SharedPreferences sPref;
    private FetchScore fetch;
    private CourseDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Portal");
        Toolbar toolbar = (Toolbar) findViewById(R.id.tabanim_toolbar);
        setSupportActionBar(toolbar);

        ViewPager viewPager = (ViewPager) findViewById(R.id.tabanim_viewpager);
        setupViewPager(viewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabanim_tabs);
        tabLayout.setupWithViewPager(viewPager);
        EventBus.getDefault().register(this);

        db = new CourseDB(this);
        fetch = new FetchScore(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLogin();
    }

    private void setupViewPager(ViewPager viewPager) {
        MainViewPagerAdapter adapter = new MainViewPagerAdapter(getSupportFragmentManager());
        new JournalFragment();
        new ResourceFragment();
        new AccountFragment();
        adapter.addFrag(JournalFragment.newInstance(null, null), "SCHEDULE");
        adapter.addFrag(ResourceFragment.newInstance(null, null), "RESOURCES");
        adapter.addFrag(AccountFragment.newInstance(null, null), "ACCOUNT");
        viewPager.setAdapter(adapter);
    }

    public void checkLogin(){
        sPref = this.getSharedPreferences(getString(R.string.shared_preferences), MODE_PRIVATE);
        if(sPref.getInt(getResources().getString(R.string.login_data_given_pref),0) !=1||sPref.getInt(getResources().getString(R.string.login_condition_pref),0) !=1) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    public void onEvent(TermResponseEvent event){
        List<String> terms;
        try{
            JSONArray jArray = new JSONArray(sPref.getString(getString(R.string.resource_terms),""));
            db.deleteData();
            db.addTerms(jArray);

            terms = db.queryTerm();
            for (int i = 0 ; i < terms.size(); i++){
                fetch.requestScores(terms.get(i));
            }
        }catch (JSONException e){

        }
    }

    public void onEvent(GradesResponseEvent event){
        try {
            JSONObject data= new JSONObject(sPref.getString(getString(R.string.resource_scores) + "_" + event.term, ""));
            db.addGrades(data,event.term);
        }catch (JSONException e){

        }
    }
}

