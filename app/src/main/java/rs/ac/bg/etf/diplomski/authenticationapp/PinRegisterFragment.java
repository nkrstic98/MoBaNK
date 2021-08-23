package rs.ac.bg.etf.diplomski.authenticationapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.davidmiguel.numberkeyboard.NumberKeyboardListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import rs.ac.bg.etf.diplomski.authenticationapp.databinding.FragmentPinRegisterBinding;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.FragmentUserRegisterBinding;

public class PinRegisterFragment extends Fragment {

    private static final int MAX_CHARS = 4;

    private RegisterActivity registerActivity;
    private FragmentPinRegisterBinding binding;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private BiometricAuthenticator biometricAuthenticator;

    private MutableLiveData<String> documentId = new MutableLiveData<>();
    private MutableLiveData<Boolean> biometry_used = new MutableLiveData<>(false);
    private String pin_code = "";

    public PinRegisterFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerActivity = (RegisterActivity) requireActivity();

        biometricAuthenticator = new BiometricAuthenticator(registerActivity);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        documentId.setValue(PinRegisterFragmentArgs.fromBundle(requireArguments()).getDocumentId());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentPinRegisterBinding.inflate(inflater, container, false);

        binding.biometryAuth.setChecked(biometry_used.getValue());

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

        binding.biometryAuth.setOnCheckedChangeListener((buttonView, isChecked) -> {
            biometry_used.setValue(isChecked);

            if(isChecked) {
                biometricAuthenticator.authenticate();
            }
        });

        binding.buttonSubmit.setOnClickListener(v -> {
            if(pin_code.equals("") || pin_code.length() < MAX_CHARS) {
                Toast.makeText(registerActivity, "You must enter 4 digit PIN code in order to continue", Toast.LENGTH_SHORT).show();
                pin_code = "";
                binding.pinCode.setText("");
            }
            else {
                SharedPreferences sharedPreferences = registerActivity.getSharedPreferences(BiometricAuthenticator.SHARED_PREFERENCES_BIOMETRY, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(BiometricAuthenticator.SHARED_PREFERENCES_BIOMETRY_PARAMETER, biometry_used.getValue());
                editor.putString(BiometricAuthenticator.SHARED_PREFERENCES_PIN_CODE_PARAMETER, pin_code);
                editor.commit();
            }
        });

        return  binding.getRoot();
    }
}