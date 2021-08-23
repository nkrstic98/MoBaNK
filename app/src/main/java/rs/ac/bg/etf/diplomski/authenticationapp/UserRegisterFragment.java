package rs.ac.bg.etf.diplomski.authenticationapp;

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

import com.google.common.base.MoreObjects;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

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

            if(email.equals("") && password.equals("")) {
                Toast.makeText(registerActivity, "Please enter email and password!", Toast.LENGTH_SHORT).show();
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
                                                UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                                        .setDisplayName(this.firstname + " " + this.lastname)
                                                        .build();

                                                currentUser.updateProfile(request)
                                                        .addOnSuccessListener(registerActivity, aVoid -> {
                                                            firebaseFirestore
                                                                    .collection("users")
                                                                    .document(documentId.getValue())
                                                                    .update(
                                                                            "email", email,
                                                                            "password", password
                                                                    )
                                                                    .addOnSuccessListener(registerActivity, aVoid1 -> {
                                                                        Toast.makeText(registerActivity, "Registration successful", Toast.LENGTH_SHORT).show();

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
                                                            Toast.makeText(registerActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
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