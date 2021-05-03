package com.example.mavtrade;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.mavtrade.fragments.DetailsFragment;
import com.example.mavtrade.fragments.FollowingFragment;
import com.example.mavtrade.fragments.HomeFragment;
import com.example.mavtrade.fragments.InboxFragment;
import com.example.mavtrade.fragments.ProfileFragment;
import com.example.mavtrade.fragments.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    final FragmentManager fragmentManager = getSupportFragmentManager();
    private Toolbar toolbar;
    private BottomNavigationView bottomNavigationView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            Fragment fragment;
            switch (menuItem.getItemId()) {
                case R.id.action_settings:
                    fragment = new SettingsFragment();
                    break;

                case R.id.action_profile:
                    fragment = new ProfileFragment();
                    break;

                case R.id.action_home:
                    fragment = new HomeFragment();
                    break;

                case R.id.action_following:
                    fragment = new FollowingFragment();
                    break;

                case R.id.action_direct:
                default:
                    fragment = new InboxFragment();
                    break;
            }
            fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
            return true;
        });
        //Set default selection
        bottomNavigationView.setSelectedItemId(R.id.action_home);


    }
}