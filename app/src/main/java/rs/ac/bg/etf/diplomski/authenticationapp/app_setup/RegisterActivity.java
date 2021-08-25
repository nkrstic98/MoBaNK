package rs.ac.bg.etf.diplomski.authenticationapp.app_setup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import dagger.hilt.android.AndroidEntryPoint;
import rs.ac.bg.etf.diplomski.authenticationapp.BiometricAuthenticator;
import rs.ac.bg.etf.diplomski.authenticationapp.app_login.LoginActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.ActivityRegisterBinding;

@AndroidEntryPoint
public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        sharedPreferences = this.getSharedPreferences(BiometricAuthenticator.SHARED_PREFERENCES_ACCOUNT, MODE_PRIVATE);

        if(sharedPreferences.contains(BiometricAuthenticator.SHARED_PREFERENCES_EMAIL_PARAMETER)) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            this.finish();
        }
    }
}