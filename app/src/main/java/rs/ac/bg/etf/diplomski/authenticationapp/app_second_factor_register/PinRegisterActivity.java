package rs.ac.bg.etf.diplomski.authenticationapp.app_second_factor_register;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

import rs.ac.bg.etf.diplomski.authenticationapp.app_login.LoginActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.ActivityPinRegisterBinding;

public class PinRegisterActivity extends AppCompatActivity {

    public static String REGISTRATION_INVOKED = "coming-from-user-registration";

    private ActivityPinRegisterBinding binding;
    private boolean should_logout = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPinRegisterBinding.inflate(getLayoutInflater());
        should_logout = getIntent().getBooleanExtra(REGISTRATION_INVOKED, true);

        setContentView(binding.getRoot());
    }

    @Override
    protected void onStop() {
        super.onStop();
        returnToLogin();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
    }

    private void returnToLogin() {
        if(FirebaseAuth.getInstance().getCurrentUser() != null && should_logout) {
            logout();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        should_logout = true;
    }
}