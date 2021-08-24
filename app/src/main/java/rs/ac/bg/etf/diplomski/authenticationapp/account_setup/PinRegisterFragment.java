package rs.ac.bg.etf.diplomski.authenticationapp.account_setup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.davidmiguel.numberkeyboard.NumberKeyboardListener;

import java.security.InvalidKeyException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import rs.ac.bg.etf.diplomski.authenticationapp.account_setup.PinRegisterFragmentArgs;
import rs.ac.bg.etf.diplomski.authenticationapp.account_setup.PinRegisterFragmentDirections;
import rs.ac.bg.etf.diplomski.authenticationapp.BiometricAuthenticator;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.FragmentPinRegisterBinding;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

public class PinRegisterFragment extends Fragment {

    private static final int MAX_CHARS = 4;

    private RegisterActivity registerActivity;
    private FragmentPinRegisterBinding binding;
    private NavController navController;

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

        documentId.setValue(PinRegisterFragmentArgs.fromBundle(requireArguments()).getDocumentId());

        biometricAuthenticator = new BiometricAuthenticator(registerActivity, new PinRegisterCallback());
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
            BiometricManager biometricManager = BiometricManager.from(registerActivity);
            switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)) {
                case BiometricManager.BIOMETRIC_SUCCESS:
                    biometry_used.setValue(isChecked);
                    if(isChecked) {
                        Toast.makeText(registerActivity, "Biometry enabled.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(registerActivity, "Biometry disabled.", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                    Toast.makeText(registerActivity, "No biometric features available on this device.", Toast.LENGTH_SHORT).show();
                    biometry_used.setValue(false);
                    break;
                case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                    Toast.makeText(registerActivity, "Biometric features are currently unavailable.", Toast.LENGTH_SHORT).show();
                    biometry_used.setValue(false);
                    binding.biometryAuth.setChecked(false);
                    break;
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

                if(biometry_used.getValue()) {
                    biometricAuthenticator.authenticate();
                }
                else {
                    PinRegisterFragmentDirections.ActionPinRegisterFragmentToKeyboardFragment action =
                            PinRegisterFragmentDirections.actionPinRegisterFragmentToKeyboardFragment(documentId.getValue());
                    action.setDocumentId(documentId.getValue());
                    navController.navigate(action);
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

    public class PinRegisterCallback implements BiometricAuthenticator.Callback {

        @Override
        public void invoke() {
            PinRegisterFragmentDirections.ActionPinRegisterFragmentToKeyboardFragment action =
                    PinRegisterFragmentDirections.actionPinRegisterFragmentToKeyboardFragment(documentId.getValue());
            action.setDocumentId(documentId.getValue());
            navController.navigate(action);
        }

        @Override
        public void encrypt() {
            biometricAuthenticator.encrypt(documentId.getValue());
        }
    }
}