package com.novatech.travelmantics;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

public class UserActivity extends AppCompatActivity {

    RecyclerView recyclerView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        FirebaseUtil.getDatabase();

       /* recyclerView = findViewById(R.id.rv_deals);
        DealAdapter dealAdapter = new DealAdapter();
        recyclerView.setAdapter(dealAdapter);
        @SuppressLint("WrongConstant") LinearLayoutManager dealsLayoutManager =
                new LinearLayoutManager(this,LinearLayoutManager.VERTICAL ,false);
        recyclerView.setLayoutManager(dealsLayoutManager);*/
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_options_menu, menu);
        MenuItem insertMenu = menu.findItem(R.id.insert_menu);
        if (FirebaseUtil.isAdmin) {
            insertMenu.setVisible(true);
        } else {
            insertMenu.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.insert_menu:
                startActivity(new Intent(UserActivity.this, AdminActivity.class));
                return true;
            case R.id.logout_menu:
                FirebaseUtil.signOut();
                FirebaseUtil.detachListener();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUtil.detachListener();
        invalidateMenu();
    }



    @SuppressLint("WrongConstant")
    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUtil.openFirebaseRef("travelDeals", this);
        RecyclerView recyclerView = findViewById(R.id.rv_deals);
        final DealAdapter dealAdapter = new DealAdapter();
        recyclerView.setAdapter(dealAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        FirebaseUtil.attachListener();
        if (FirebaseUtil.mFirebaseAuth.getUid() != null) {
            FirebaseUtil.checkAdmin(FirebaseUtil.mFirebaseAuth.getUid());
            invalidateMenu();
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public void invalidateMenu() {
        invalidateOptionsMenu();
    }
}









