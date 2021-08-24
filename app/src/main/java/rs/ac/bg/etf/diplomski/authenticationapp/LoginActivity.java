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

    @RequiresApi(api = Build.VERSION_CODES.M)
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

                                    String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();

                                    if(verifyAccount(secret_key, documentId)) {
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean verifyAccount(String secret_key, String documentId) {
        long date = getIntent().getLongExtra(SECRET_KEY_PARAMETER, -1);

        if(date == -1) {
            return false;
        }

        BiometricAuthenticator biometricAuthenticator = new BiometricAuthenticator(this, null);

        biometricAuthenticator.generateSecretKey(false);
        Cipher cipher = biometricAuthenticator.getCypher();
        SecretKey secretKey = biometricAuthenticator.getSecretKey();

        secretKey.getEncoded();

        SharedPreferences sharedPreferences = getSharedPreferences(BiometricAuthenticator.SHARED_PREFERENCES_BIOMETRY, MODE_PRIVATE);

        try {
            String iv = sharedPreferences.getString(BiometricAuthenticator.SHARED_PREFERENCES_IV, "");
            byte[] iv_bytes = getBytes(iv);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] decrypted_data = cipher.doFinal((documentId + "_" + date).getBytes(Charset.defaultCharset()));

            String decrypted_string = decrypted_data.toString();
            Toast.makeText(this, decrypted_string, Toast.LENGTH_SHORT).show();

        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

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