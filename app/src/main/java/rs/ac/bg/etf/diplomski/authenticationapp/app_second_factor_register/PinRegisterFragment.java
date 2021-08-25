package rs.ac.bg.etf.diplomski.authenticationapp.app_second_factor_register;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.auth.FirebaseAuth;

import rs.ac.bg.etf.diplomski.authenticationapp.app_login.LoginActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.modules.BiometricAuthenticator;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.HomeFragmentDirections;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.MainActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.FragmentPinRegisterBinding;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

public class PinRegisterFragment extends Fragment {

    public static final String SHARED_PREFERENCES_REGISTER = "shared-preferences-register";
    public static final String SHARED_PREFERENCES_REGISTER_PIN = "shared-preferences-register-pin";
    public static final String SHARED_PREFERENCES_REGISTER_BIOMETRY = "shared-preferences-register-biometry";

    private static final int MAX_CHARS = 6;

    private PinRegisterActivity pinRegisterActivity;
    private FragmentPinRegisterBinding binding;
    private NavController navController;

    private BiometricAuthenticator biometricAuthenticator;
    private MutableLiveData<Boolean> biometry_used = new MutableLiveData<>(false);
    private String pin_code = "";

    public PinRegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pinRegisterActivity = (PinRegisterActivity) requireActivity();

        biometricAuthenticator = new BiometricAuthenticator(pinRegisterActivity, new PinRegisterCallback());
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
            BiometricManager biometricManager = BiometricManager.from(pinRegisterActivity);
            switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)) {
                case BiometricManager.BIOMETRIC_SUCCESS:
                    biometry_used.setValue(isChecked);
                    if(isChecked) {
                        Toast.makeText(pinRegisterActivity, "Biometry enabled.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(pinRegisterActivity, "Biometry disabled.", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                    Toast.makeText(pinRegisterActivity, "No biometric features available on this device.", Toast.LENGTH_SHORT).show();
                    biometry_used.setValue(false);
                    break;
                case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                    Toast.makeText(pinRegisterActivity, "Biometric features are currently unavailable.", Toast.LENGTH_SHORT).show();
                    biometry_used.setValue(false);
                    binding.biometryAuth.setChecked(false);
                    break;
            }
        });

        binding.buttonSubmit.setOnClickListener(v -> {
            if(pin_code.equals("") || pin_code.length() < MAX_CHARS) {
                Toast.makeText(pinRegisterActivity, "You must enter " + MAX_CHARS + " digit PIN code in order to continue", Toast.LENGTH_SHORT).show();
                pin_code = "";
                binding.pinCode.setText("");
            }
            else {
                if(biometry_used.getValue()) {
                    biometricAuthenticator.authenticate();
                }
                else {
                    pinRegisterActivity
                            .getSharedPreferences(PinRegisterFragment.SHARED_PREFERENCES_REGISTER, Context.MODE_PRIVATE)
                            .edit()
                            .putString(PinRegisterFragment.SHARED_PREFERENCES_REGISTER_PIN, pin_code)
                            .putBoolean(PinRegisterFragment.SHARED_PREFERENCES_REGISTER_BIOMETRY, biometry_used.getValue())
                            .commit();

                    navController.navigate(
                            PinRegisterFragmentDirections.actionPinRegisterFragmentToNavGraphPin()
                    );
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
        public void failure() {
            pinRegisterActivity
                    .getSharedPreferences(PinRegisterFragment.SHARED_PREFERENCES_REGISTER, Context.MODE_PRIVATE)
                    .edit()
                    .putString(PinRegisterFragment.SHARED_PREFERENCES_REGISTER_PIN, pin_code)
                    .putBoolean(PinRegisterFragment.SHARED_PREFERENCES_REGISTER_BIOMETRY, biometry_used.getValue())
                    .commit();

            navController.navigate(
                    PinRegisterFragmentDirections.actionPinRegisterFragmentToNavGraphPin()
            );
        }

        @Override
        public void success() {
            SharedPreferences sharedPreferences = pinRegisterActivity.getSharedPreferences(BiometricAuthenticator.SHARED_PREFERENCES_ACCOUNT, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(BiometricAuthenticator.SHARED_PREFERENCES_BIOMETRY_PARAMETER, biometry_used.getValue());
            editor.putString(BiometricAuthenticator.SHARED_PREFERENCES_PIN_CODE_PARAMETER, pin_code);
            editor.apply();

            if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                Intent intent = new Intent(pinRegisterActivity, MainActivity.class);
                intent.putExtra(MainActivity.REGISTRATION_COMPLETED, true);
                pinRegisterActivity.startActivity(intent);
                pinRegisterActivity.finish();
            }
            else {
                Intent intent = new Intent(pinRegisterActivity, LoginActivity.class);
                pinRegisterActivity.startActivity(intent);
                pinRegisterActivity.finish();
            }
        }
    }
}