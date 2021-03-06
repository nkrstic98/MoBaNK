package rs.ac.bg.etf.diplomski.authenticationapp.app_main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import rs.ac.bg.etf.diplomski.authenticationapp.R;
import rs.ac.bg.etf.diplomski.authenticationapp.modules.BiometricAuthenticator;
import rs.ac.bg.etf.diplomski.authenticationapp.app_login.LoginActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.app_second_factor_register.PinRegisterActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    public static final String REGISTRATION_COMPLETED = "registration-completed";

    public static final String SP_PROFILE_IMAGE = "sp-profile-image";
    public static final String IMAGE_DATA = "image-data";

    private ActivityMainBinding binding;
    private AppBarConfiguration appBarConfiguration;

    private boolean finish_registration = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(binding.getRoot());

        boolean registered_fully = getSharedPreferences(BiometricAuthenticator.SHARED_PREFERENCES_ACCOUNT, MODE_PRIVATE)
                .contains(BiometricAuthenticator.SHARED_PREFERENCES_PIN_CODE_PARAMETER);

        if(!registered_fully) {
            finish_registration = true;
            Intent intent = new Intent(this, PinRegisterActivity.class);
            intent.putExtra(PinRegisterActivity.REGISTRATION_INVOKED, false);
            startActivity(intent);
            finish();
            return;
        }
//        else {
            finish_registration = getIntent().getBooleanExtra(REGISTRATION_COMPLETED, false);
//        }

        setSupportActionBar(findViewById(R.id.toolbar_main));

        DrawerLayout drawerLayout = binding.drawerLayout;

        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_accounts,
                R.id.nav_payments,
                R.id.nav_money_transfer,
                R.id.nav_exchange_office,
                R.id.nav_user_settings
        )
        .setDrawerLayout(drawerLayout)
        .build();

        binding.navView.getMenu().findItem(R.id.menu_logout).setOnMenuItemClickListener(item -> {
            logout();
            returnToLogin();

            return true;
        });

        NavigationView navigationView = binding.navView;
        NavController navController = Navigation.findNavController(this, R.id.main_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        setUserData();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.main_host_fragment);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onResume() {
        super.onResume();
        returnToLogin();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        logout();
    }

    @Override
    protected void onStop() {
        super.onStop();
        logout();
    }

    private void logout() {
        SharedPreferences sp = getSharedPreferences(SP_PROFILE_IMAGE, MODE_PRIVATE);
        if(sp.getBoolean(IMAGE_DATA, false)) {
            sp.edit().clear().apply();
            return;
        }

        if(!finish_registration) {
            FirebaseAuth.getInstance().signOut();
        }
    }

    private void returnToLogin() {
        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void setUserData() {
        SharedPreferences sharedPreferences = getSharedPreferences(BiometricAuthenticator.SHARED_PREFERENCES_ACCOUNT, MODE_PRIVATE);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        View headerView = binding.navView.getHeaderView(0);

        TextView name = (TextView) headerView.findViewById(R.id.user_name);
        name.setText(user.getDisplayName());
        TextView email = (TextView) headerView.findViewById(R.id.user_email);
        email.setText(user.getEmail());
        ImageView profile = (ImageView) headerView.findViewById(R.id.imageProfile_header);

        if(user.getPhotoUrl() != null) {
            profile.setImageURI(user.getPhotoUrl());
        }
        else {
            profile.setImageDrawable(getDrawable(R.drawable.no_profile_pic));
        }
    }
}