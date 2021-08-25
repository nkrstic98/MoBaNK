package rs.ac.bg.etf.diplomski.authenticationapp.account_setup;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rs.ac.bg.etf.diplomski.authenticationapp.BiometricAuthenticator;
import rs.ac.bg.etf.diplomski.authenticationapp.account_setup.UserRegisterFragmentArgs;
import rs.ac.bg.etf.diplomski.authenticationapp.account_setup.UserRegisterFragmentDirections;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.FragmentUserRegisterBinding;
import rs.ac.bg.etf.diplomski.authenticationapp.models.User;

public class UserRegisterFragment extends Fragment {

    private RegisterActivity registerActivity;
    private FragmentUserRegisterBinding binding;
    private NavController navController;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private MutableLiveData<String> documentId = new MutableLiveData<>();

    private String firstname;
    private String lastname;

    public UserRegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerActivity = (RegisterActivity) requireActivity();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        documentId.setValue(UserRegisterFragmentArgs.fromBundle(requireArguments()).getDocumentId());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentUserRegisterBinding.inflate(inflater, container, false);

        firebaseFirestore
                .collection("users")
                .document(documentId.getValue())
                .get()
                .addOnSuccessListener(registerActivity, documentSnapshot -> {
                    if(documentSnapshot != null) {
                        User user = documentSnapshot.toObject(User.class);

                        this.firstname = user.getFirstname();
                        this.lastname = user.getLastname();

                        StringBuilder sb = new StringBuilder("Welcome, ");
                        sb.append(user.getFirstname());
                        sb.append(" ");
                        sb.append(user.getLastname());
                        binding.welcomeText.setText(sb.toString());
                    }
                    else {
                        Toast.makeText(registerActivity, "Something went wrong, please try again later!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(registerActivity, e -> {
                    Toast.makeText(registerActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        binding.buttonRegister.setOnClickListener(v -> {
            String email = binding.email.getText().toString();
            String password = binding.password.getText().toString();
            String confirm_password = binding.passwordConfirm.getText().toString();

            if(email.equals("") && password.equals("") && confirm_password.equals("")) {
                Toast.makeText(registerActivity, "Please enter required data", Toast.LENGTH_SHORT).show();
                binding.emailLabel.getEditText().requestFocus();
                return;
            }

            if(email.equals("")) {
                Toast.makeText(registerActivity, "Email is required!", Toast.LENGTH_SHORT).show();
                binding.emailLabel.getEditText().requestFocus();
                return;
            }

            if(password.equals("")) {
                Toast.makeText(registerActivity, "Password is required!", Toast.LENGTH_SHORT).show();
                binding.passwordLabel.getEditText().requestFocus();
                return;
            }

            if(confirm_password.equals("")) {
                Toast.makeText(registerActivity, "Confirm password is required!", Toast.LENGTH_SHORT).show();
                binding.passwordConfirmLabel.getEditText().requestFocus();
                return;
            }

            if(!password.equals(confirm_password)) {
                Toast.makeText(registerActivity, "Password and confirm password do not match!", Toast.LENGTH_SHORT).show();
                binding.password.setText("");
                binding.passwordConfirm.setText("");
                binding.passwordLabel.getEditText().requestFocus();
                return;
            }

            Pattern pattern = Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=\\S+$)" +           //no white spaces
                    ".{3,}" +               //at least 4 characters
                    "$");
            Matcher matcher = pattern.matcher(password);

            if(!matcher.matches()) {
                Toast.makeText(registerActivity,
                        "Password must contain at least 1 lowercase, 1 uppercase letter, and 1 digit, must not contain whitespaces, and must be at least 6 characters long",
                        Toast.LENGTH_LONG
                ).show();

                binding.password.setText("");
                binding.passwordConfirm.setText("");
                binding.passwordLabel.getEditText().requestFocus();
                return;
            }

            firebaseFirestore
                    .collection("users")
                    .document(documentId.getValue())
                    .get()
                    .addOnCompleteListener(registerActivity, task -> {
                        if(task.isSuccessful()) {
                            if(task.getResult().get("email") == null) {
                                firebaseAuth.createUserWithEmailAndPassword(email, password)
                                        .addOnSuccessListener(registerActivity, authResult -> {
                                            FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                                            if(currentUser != null) {

                                                currentUser.sendEmailVerification()
                                                        .addOnSuccessListener(registerActivity, aVoid -> {
                                                            Toast.makeText(registerActivity, "Verification email sent!", Toast.LENGTH_SHORT).show();

                                                            UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                                                    .setDisplayName(this.firstname + " " + this.lastname)
                                                                    .build();

                                                            currentUser.updateProfile(request)
                                                                    .addOnSuccessListener(registerActivity, aVoid1 -> {
                                                                        firebaseFirestore
                                                                                .collection("users")
                                                                                .document(documentId.getValue())
                                                                                .update(
                                                                                        "email", email
                                                                                )
                                                                                .addOnSuccessListener(registerActivity, aVoid2 -> {
                                                                                    registerActivity
                                                                                            .getSharedPreferences(BiometricAuthenticator.SHARED_PREFERENCES_ACCOUNT, Context.MODE_PRIVATE)
                                                                                            .edit()
                                                                                            .putString(BiometricAuthenticator.SHARED_PREFERENCES_EMAIL_PARAMETER, email)
                                                                                            .apply();

                                                                                    UserRegisterFragmentDirections.ActionUserRegisterFragmentToPinRegisterFragment action =
                                                                                            UserRegisterFragmentDirections.actionUserRegisterFragmentToPinRegisterFragment(documentId.getValue());
                                                                                    action.setDocumentId(documentId.getValue());
                                                                                    navController.navigate(action);
                                                                                })
                                                                                .addOnFailureListener(registerActivity, e -> {
                                                                                    Toast.makeText(registerActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                });
                                                                    })
                                                                    .addOnFailureListener(registerActivity, e -> {
                                                                        Toast.makeText(registerActivity, "Registration failed. Try again!", Toast.LENGTH_SHORT).show();
                                                                        currentUser.delete();
                                                                    });
                                                        })
                                                        .addOnFailureListener(registerActivity, e -> {
                                                            Toast.makeText(registerActivity, "Registration failed. Try again!", Toast.LENGTH_SHORT).show();
                                                            currentUser.delete();
                                                        });

                                            }
                                        })
                                        .addOnFailureListener(registerActivity, e -> {
                                            Toast.makeText(registerActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                            else {
                                Toast.makeText(registerActivity, "Error: This user is already registered!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
    }
}