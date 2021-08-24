package rs.ac.bg.etf.diplomski.authenticationapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;

import rs.ac.bg.etf.diplomski.authenticationapp.databinding.ActivityLoginBinding;
import rs.ac.bg.etf.diplomski.authenticationapp.models.User;

public class LoginActivity extends AppCompatActivity {

    public static final String SECRET_KEY_PARAMETER = "secret-key-parameter";

    private ActivityLoginBinding binding;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        binding.buttonLogin.setOnClickListener(v -> {
            login();
        });
    }

    private void login() {
        String email = binding.email.getText().toString();
        String password = binding.password.getText().toString();

        if(email.equals("") && password.equals("")) {
            binding.emailLabel.getEditText().requestFocus();
            Toast.makeText(this, "Email and password are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(email.equals("")) {
            binding.emailLabel.getEditText().requestFocus();
            Toast.makeText(this, "Email is required!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(password.equals("")) {
            binding.passwordLabel.getEditText().requestFocus();
            Toast.makeText(this, "Password is required!", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if(task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();

                        firebaseFirestore.collection("users")
                                .whereEqualTo("email", user.getEmail())
                                .get()
                                .addOnSuccessListener(this, queryDocumentSnapshots -> {
                                    if(queryDocumentSnapshots.isEmpty()) {
                                        Toast.makeText(this, "There is no user with provided credentials!", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    String secret_key = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class).getSecret_key();

                                    if(verifyAccount(secret_key)) {
                                        Toast.makeText(this, "Something went wrong. Please try again!", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        Intent intent = new Intent(this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .addOnFailureListener(this, e -> {
                                    firebaseAuth.signOut();
                                    Toast.makeText(this, "Something went wrong. Please try again!", Toast.LENGTH_SHORT).show();
                                    binding.password.setText("");
                                });
                    }
                    else {
                        binding.password.setText("");
                        binding.passwordLabel.getEditText().requestFocus();
                        Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean verifyAccount(String secret_key) {

        return false;
    }

    public byte[] getBytes(String arrayString) {
        String[] strings = arrayString.replace("[", "").replace("]", "").split(", ");
        byte[] result = new byte[strings.length];
        for(int i = 0; i < strings.length; i++) {
            result[i] = Byte.parseByte(strings[i]);
        }

        return result;
    }
}