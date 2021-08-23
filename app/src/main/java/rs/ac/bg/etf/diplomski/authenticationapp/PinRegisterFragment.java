package rs.ac.bg.etf.diplomski.authenticationapp;

import android.content.Intent;
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

import com.davidmiguel.numberkeyboard.NumberKeyboardListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import rs.ac.bg.etf.diplomski.authenticationapp.databinding.FragmentPinRegisterBinding;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.FragmentUserRegisterBinding;

public class PinRegisterFragment extends Fragment {

    private static final int MAX_CHARS = 4;

    private RegisterActivity registerActivity;
    private FragmentPinRegisterBinding binding;
    private NavController navController;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private MutableLiveData<String> documentId = new MutableLiveData<>();

    private String pin_code = "";

    public PinRegisterFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerActivity = (RegisterActivity) requireActivity();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        documentId.setValue(PinRegisterFragmentArgs.fromBundle(requireArguments()).getDocumentId());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentPinRegisterBinding.inflate(inflater, container, false);

        binding.numberKeyboard.setListener(new NumberKeyboardListener() {
            @Override
            public void onNumberClicked(int i) {
                if(pin_code.length() < MAX_CHARS) {
                    pin_code = pin_code.concat(Integer.toString(i));
                    binding.pinCode.setText(binding.pinCode.getText().toString() + "*");
                }
            }

            @Override
            public void onLeftAuxButtonClicked() {

            }

            @Override
            public void onRightAuxButtonClicked() {
                if(pin_code.length() > 0) {
                    pin_code = pin_code.substring(0, pin_code.length() - 1);
                    binding.pinCode.setText(binding.pinCode.getText().toString().substring(0, pin_code.length()));
                }
            }
        });

        return  binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
    }
}