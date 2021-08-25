package rs.ac.bg.etf.diplomski.authenticationapp.app_login;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import rs.ac.bg.etf.diplomski.authenticationapp.BiometricAuthenticator;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        getSharedPreferences(BiometricAuthenticator.SHARED_PREFERENCES_ACCOUNT, MODE_PRIVATE)
//                .edit()
//                .remove(BiometricAuthenticator.SHARED_PREFERENCES_PIN_CODE_PARAMETER)
//                .remove(BiometricAuthenticator.SHARED_PREFERENCES_BIOMETRY_PARAMETER)
//                .commit();
    }
}