package rs.ac.bg.etf.diplomski.authenticationapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.biometric.BiometricManager;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.davidmiguel.numberkeyboard.NumberKeyboardListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;

import rs.ac.bg.etf.diplomski.authenticationapp.databinding.FragmentPinRegisterBinding;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.FragmentUserRegisterBinding;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

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

        documentId.setValue(PinRegisterFragmentArgs.fromBundle(requireArguments()).getDocumentId());

        biometricAuthenticator = new BiometricAuthenticator(registerActivity, documentId.getValue());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    biometricAuthenticator.generateSecretKey(biometry_used.getValue());
                    Cipher cipher = biometricAuthenticator.getCypher();
                    SecretKey secretKey = biometricAuthenticator.getSecretKey();

                    try {
                        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

                        if(biometry_used.getValue()) {
                            biometricAuthenticator.authenticate(cipher);
                        }
                        else {
                            Date date = new Date();
                            long time = date.getTime();

                            editor.putLong(BiometricAuthenticator.SHARED_PREFERENCES_KEY_PARAMETER, time);
                            editor.commit();

                            String key = documentId.getValue() + "_" + time;

                            byte[] crypto = cipher.doFinal(
                                    key.getBytes(Charset.defaultCharset())
                            );

                            firebaseFirestore
                                    .collection("users")
                                    .document(documentId.getValue())
                                    .update("secret_key", crypto.toString())
                                    .addOnSuccessListener(registerActivity, aVoid1 -> {
                                        Toast.makeText(registerActivity, "Success!", Toast.LENGTH_SHORT).show();

                                        //prebaci na logovanje
                                        //ugasi aktivnost
                                    })
                                    .addOnFailureListener(registerActivity, e -> {
                                        Toast.makeText(registerActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (BadPaddingException e) {
                        e.printStackTrace();
                    } catch (IllegalBlockSizeException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        return  binding.getRoot();
    }
}