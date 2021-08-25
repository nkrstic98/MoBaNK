package rs.ac.bg.etf.diplomski.authenticationapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

import rs.ac.bg.etf.diplomski.authenticationapp.account_login.LoginActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        returnToLogin();
    }

    @Override
    protected void onPause() {
        super.onPause();
        logout();
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
        FirebaseAuth.getInstance().signOut();
    }

    private void returnToLogin() {
        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}