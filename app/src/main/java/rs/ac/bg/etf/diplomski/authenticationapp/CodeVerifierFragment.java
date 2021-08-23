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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import rs.ac.bg.etf.diplomski.authenticationapp.databinding.FragmentCodeVerifierBinding;

public class CodeVerifierFragment extends Fragment {

    private RegisterActivity registerActivity;
    private FragmentCodeVerifierBinding binding;
    private NavController navController;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private MutableLiveData<String> verificationId = new MutableLiveData<>();
    private MutableLiveData<String> documentId = new MutableLiveData<>();

    public CodeVerifierFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerActivity = (RegisterActivity) requireActivity();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        verificationId.setValue(CodeVerifierFragmentArgs.fromBundle(requireArguments()).getVerificationId());
        documentId.setValue(CodeVerifierFragmentArgs.fromBundle(requireArguments()).getDocumentId());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentCodeVerifierBinding.inflate(inflater, container, false);

        binding.buttonSubmit.setOnClickListener(v -> {
            String smsCode = binding.smsVerifier.getText().toString();
            if(smsCode.equals("")) {
                Toast.makeText(registerActivity, "You must enter SMS code!", Toast.LENGTH_SHORT).show();
                binding.smsVerifierLabel.getEditText().requestFocus();
            }
            else {
                registerUser(smsCode);
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
    }

    private void registerUser(String smsCode) {
        firebaseAuth.signInWithCredential(PhoneAuthProvider.getCredential(verificationId.getValue(), smsCode))
                .addOnCompleteListener(registerActivity, task -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(registerActivity, "Valid SMS code!", Toast.LENGTH_SHORT).show();

                        firebaseAuth.getCurrentUser().delete()
                                .addOnCompleteListener(registerActivity, task1 -> {
                                    CodeVerifierFragmentDirections.ActionCodeVerifierFragmentToUserRegisterFragment action =
                                            CodeVerifierFragmentDirections.actionCodeVerifierFragmentToUserRegisterFragment(documentId.getValue());
                                    action.setDocumentId(documentId.getValue());
                                    navController.navigate(action);
                                });
                    }
                    else {
                        Toast.makeText(registerActivity, "Wrong SMS verification code!", Toast.LENGTH_SHORT).show();
                        binding.smsVerifier.setText("");
                        binding.smsVerifierLabel.getEditText().requestFocus();
                    }
                });
    }
}