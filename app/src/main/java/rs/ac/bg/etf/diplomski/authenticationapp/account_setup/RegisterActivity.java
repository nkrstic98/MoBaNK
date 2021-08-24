package rs.ac.bg.etf.diplomski.authenticationapp.account_setup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import dagger.hilt.android.AndroidEntryPoint;
import rs.ac.bg.etf.diplomski.authenticationapp.BiometricAuthenticator;
import rs.ac.bg.etf.diplomski.authenticationapp.LoginActivity;
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

        sharedPreferences = this.getSharedPreferences(BiometricAuthenticator.SHARED_PREFERENCES_BIOMETRY, MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.clear();
//        editor.commit();

        long date = sharedPreferences.getLong(BiometricAuthenticator.SHARED_PREFERENCES_KEY_PARAMETER, -1);

        if(date != -1) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra(LoginActivity.SECRET_KEY_PARAMETER, date);
            startActivity(intent);
            this.finish();
        }
    }
}