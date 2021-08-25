package rs.ac.bg.etf.diplomski.authenticationapp.app_main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

import rs.ac.bg.etf.diplomski.authenticationapp.modules.BiometricAuthenticator;
import rs.ac.bg.etf.diplomski.authenticationapp.app_login.LoginActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.app_second_factor_register.PinRegisterActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    public static final String REGISTRATION_COMPLETED = "registration-completed";

    private ActivityMainBinding binding;

    private boolean finish_registration = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        boolean registered_fully = getSharedPreferences(BiometricAuthenticator.SHARED_PREFERENCES_ACCOUNT, MODE_PRIVATE)
                .contains(BiometricAuthenticator.SHARED_PREFERENCES_PIN_CODE_PARAMETER);

        if(!registered_fully) {
            finish_registration = true;
            Intent intent = new Intent(this, PinRegisterActivity.class);
            intent.putExtra(PinRegisterActivity.REGISTRATION_INVOKED, false);
            startActivity(intent);
            finish();
        }
        else {
            finish_registration = getIntent().getBooleanExtra(REGISTRATION_COMPLETED, false);
        }
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
}