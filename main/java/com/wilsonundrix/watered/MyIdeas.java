package com.wilsonundrix.watered;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.google.firebase.auth.FirebaseAuth;

public class MyIdeas extends AppCompatActivity {

    private BottomNavigationView bnvMyIdeas;
    private Toolbar tbMyIdeas;
    private FrameLayout fl_fragement_content;
    private HomeFragment homeFragment;
    private AccountFragment accountFragment;
//    private NotificationFragment notificationFragment;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_ideas);

        mAuth = FirebaseAuth.getInstance();

        homeFragment = new HomeFragment();
        accountFragment = new AccountFragment();
//        notificationFragment = new NotificationFragment();

        bnvMyIdeas = findViewById(R.id.bnvMyIdeas);
        tbMyIdeas = findViewById(R.id.tbMyIdeas);
        fl_fragement_content = findViewById(R.id.fl_fragment_content);

        setSupportActionBar(tbMyIdeas);
        getSupportActionBar().setTitle("Nurtured Ideas");

        if (savedInstanceState == null) {
            replaceFragment(homeFragment);
        }

        bnvMyIdeas.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.menu_home:
                        replaceFragment(homeFragment);
                        return true;
//                    case R.id.menu_notifications:
//                        replaceFragment(notificationFragment);
//                        return true;
                    case R.id.menu_account:
                        replaceFragment(accountFragment);
                        return true;
                    default:
                        return false;
                }


            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_log_out:
                logOut();
                return false;

            case R.id.menu_acc_settings:
                Intent settingsIntent = new Intent(MyIdeas.this, AccountSettings.class);
                startActivity(settingsIntent);
                return true;

            default:
                return false;
        }
    }

    private void logOut() {
        mAuth.signOut();
        SendToLogin();
    }

    private void SendToLogin() {
        Intent settingsIntent = new Intent(MyIdeas.this, Login.class);
        startActivity(settingsIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.acc_settings_menu, menu);
        return true;
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fl_fragment_content, fragment);
        fragmentTransaction.commit();
    }
}
