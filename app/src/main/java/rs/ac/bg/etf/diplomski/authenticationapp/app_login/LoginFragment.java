package rs.ac.bg.etf.diplomski.authenticationapp.app_login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import rs.ac.bg.etf.diplomski.authenticationapp.modules.BiometricAuthenticator;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.MainActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.FragmentLoginBinding;
import rs.ac.bg.etf.diplomski.authenticationapp.modules.KeyboardFragment;
import rs.ac.bg.etf.diplomski.authenticationapp.modules.KeyboardFragmentDirections;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private LoginActivity loginActivity;
    private SharedPreferences sharedPreferences;
    private NavController navController;

    private FirebaseAuth firebaseAuth;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loginActivity = (LoginActivity) requireActivity();
        sharedPreferences = loginActivity.getSharedPreferences(BiometricAuthenticator.SHARED_PREFERENCES_ACCOUNT, Context.MODE_PRIVATE);

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);

        Uri uri = null;
        if(sharedPreferences.getString(BiometricAuthenticator.SHARED_PREFERENCES_USER_PHOTO, "").equals("")) {
            uri = null;
        }
        else {
            uri = Uri.parse(sharedPreferences.getString(BiometricAuthenticator.SHARED_PREFERENCES_USER_PHOTO, ""));

        }
        if(uri != null) {
            binding.imageProfileLogin.setImageURI(uri);
        }

        binding.welcomeMessage.setText("Welcome, " + sharedPreferences.getString(BiometricAuthenticator.SHARED_PREFERENCES_USER_INFO, ""));

        binding.buttonLogin.setOnClickListener(v -> {
            login();
        });

        binding.forgotPassword.setOnClickListener(v -> {
            new BiometricAuthenticator(loginActivity, new BiometricAuthenticator.Callback() {
                @Override
                public void failure() {
                    navController.navigate(LoginFragmentDirections.actionLoginFragmentToNavGraphPin());
                }

                @Override
                public void success() {
                    sendForgotPasswordEmail();
                }
            }).authenticate();
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
    }

    private void login() {
        String email = binding.email.getText().toString();
        String password = binding.password.getText().toString();

        if(email.equals("") && password.equals("")) {
            binding.emailLabel.getEditText().requestFocus();
            Toast.makeText(loginActivity, "Email and password are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(email.equals("")) {
            binding.emailLabel.getEditText().requestFocus();
            Toast.makeText(loginActivity, "Email is required!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(password.equals("")) {
            binding.passwordLabel.getEditText().requestFocus();
            Toast.makeText(loginActivity, "Password is required!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!email.equals(sharedPreferences.getString(BiometricAuthenticator.SHARED_PREFERENCES_EMAIL_PARAMETER, ""))) {
            Toast.makeText(loginActivity, "Entered email is not associated with this account!", Toast.LENGTH_SHORT).show();
            binding.email.setText("");
            binding.password.setText("");
            binding.emailLabel.getEditText().requestFocus();
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(loginActivity, task -> {
                    if(task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();

                        if(user.isEmailVerified()) {
                            Intent intent = new Intent(loginActivity, MainActivity.class);
                            startActivity(intent);
                            loginActivity.finish();
                        }
                        else {
                            Toast.makeText(loginActivity, "Verify email address to activate account!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        binding.password.setText("");
                        binding.passwordLabel.getEditText().requestFocus();
                        Toast.makeText(loginActivity, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendForgotPasswordEmail() {
        String email = sharedPreferences.getString(BiometricAuthenticator.SHARED_PREFERENCES_EMAIL_PARAMETER, "");
        firebaseAuth
                .sendPasswordResetEmail(email)
                .addOnSuccessListener(loginActivity, aVoid -> {
                    Toast.makeText(loginActivity, "Password reset email is sent to the address associated with this account!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(loginActivity, e -> {
                    Toast.makeText(loginActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}