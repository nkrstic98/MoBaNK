package rs.ac.bg.etf.diplomski.authenticationapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import rs.ac.bg.etf.diplomski.authenticationapp.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

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
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                        finish();

//                        FirebaseUser user = firebaseAuth.getCurrentUser();
//
//                        firebaseFirestore.collection("users")
//                                .whereEqualTo("email", user.getEmail())
//                                .get()
//                                .addOnSuccessListener(this, queryDocumentSnapshots -> {
//                                    if(queryDocumentSnapshots.isEmpty()) {
//                                        Toast.makeText(this, "There is no user with provided credentials!", Toast.LENGTH_SHORT).show();
//                                        return;
//                                    }
//
//                                    String security_message = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class).getSecurity_message();
//
//                                    if(verifyAccount(security_message)) {
//                                        Intent intent = new Intent(this, MainActivity.class);
//                                        startActivity(intent);
//                                        finish();
//                                    }
//                                    else {
//                                        Toast.makeText(this, "Something went wrong. Please try again!", Toast.LENGTH_SHORT).show();
//                                    }
//                                })
//                                .addOnFailureListener(this, e -> {
//                                    firebaseAuth.signOut();
//                                    Toast.makeText(this, "Something went wrong. Please try again!", Toast.LENGTH_SHORT).show();
//                                    binding.password.setText("");
//                                });
                    }
                    else {
                        binding.password.setText("");
                        binding.passwordLabel.getEditText().requestFocus();
                        Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

//    private boolean verifyAccount(String security_message) {
//        SharedPreferences sharedPreferences = getSharedPreferences(BiometricAuthenticator.SHARED_PREFERENCES_BIOMETRY, MODE_PRIVATE);
//
//
//       Encryption encryption = (new Gson()).fromJson(sharedPreferences.getString(BiometricAuthenticator.SHARED_PREFERENCES_ENCRYPTION_OBJECT, ""), Encryption.class);
//
//        String decrypted = null;
//
//        decrypted = encryption.decryptOrNull(security_message);
//
//        if(decrypted != null && decrypted.equals(sharedPreferences.getString(BiometricAuthenticator.SHARED_PREFERENCES_SECRET, ""))) {
//            return true;
//        }
//
//        return false;
//    }

//    public byte[] getBytes(String arrayString) {
//        String[] strings = arrayString.replace("[", "").replace("]", "").split(", ");
//        byte[] result = new byte[strings.length];
//        for(int i = 0; i < strings.length; i++) {
//            result[i] = Byte.parseByte(strings[i]);
//        }
//
//        return result;
//    }
}