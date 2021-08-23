package rs.ac.bg.etf.diplomski.authenticationapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.davidmiguel.numberkeyboard.NumberKeyboardListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;

import rs.ac.bg.etf.diplomski.authenticationapp.databinding.FragmentKeyboardBinding;

public class KeyboardFragment extends Fragment {

    private static final int MAX_CHARS = 4;
    private static final int MAX_TRIES = 3;

    private FragmentActivity activity;
    private FragmentKeyboardBinding binding;
    private SharedPreferences sharedPreferences;

    private FirebaseFirestore firebaseFirestore;

    private MutableLiveData<String> documentId = new MutableLiveData<>();

    private MutableLiveData<Integer> num_tries = new MutableLiveData<>(0);
    private MutableLiveData<Boolean> can_enter = new MutableLiveData<>(true);
    private MutableLiveData<Integer> time_to_wait = new MutableLiveData<>(30);
    private String pin_code = "";

    private final Timer timer = new Timer();

    public KeyboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = requireActivity();

        sharedPreferences = activity.getSharedPreferences(BiometricAuthenticator.SHARED_PREFERENCES_BIOMETRY, Context.MODE_PRIVATE);

        firebaseFirestore = FirebaseFirestore.getInstance();

        documentId.setValue(KeyboardFragmentArgs.fromBundle(requireArguments()).getDocumentId());
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentKeyboardBinding.inflate(inflater, container, false);

        binding.numberKeyboard.setListener(new NumberKeyboardListener() {
            @Override
            public void onNumberClicked(int i) {
                if(can_enter.getValue()) {
                    if (pin_code.length() < MAX_CHARS) {
                        pin_code = pin_code.concat(Integer.toString(i));
                        binding.pinCode.setText(binding.pinCode.getText().toString() + "*");
                    }
                }
                else {
                    Toast.makeText(activity, "Wrong PIN Code entered multiple times: wait for " + time_to_wait.getValue() + " seconds and try again.", Toast.LENGTH_SHORT).show();
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

        binding.buttonSubmit.setOnClickListener(v -> {
            String sp_pin = sharedPreferences.getString(BiometricAuthenticator.SHARED_PREFERENCES_PIN_CODE_PARAMETER, "");

            if(!pin_code.equals(sp_pin)) {
                if(num_tries.getValue() < MAX_TRIES) {
                    Toast.makeText(activity, "Invalid PIN Code. Try again!", Toast.LENGTH_SHORT).show();
                    num_tries.setValue(num_tries.getValue() + 1);
                }
                else {
                    if(!can_enter.getValue()) {
                        Toast.makeText(activity, "Wrong PIN Code entered multiple times: wait for " + time_to_wait.getValue() + " seconds and try again.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Toast.makeText(activity, "Wrong PIN code was inserted 3 times. Wait 30 seconds and try again.", Toast.LENGTH_SHORT).show();
                    can_enter.setValue(false);

                    Handler handler = new Handler(Looper.getMainLooper());

                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            handler.post(() -> {
                                time_to_wait.setValue(time_to_wait.getValue() - 1);
                                Log.d("timer-task", "Remaining time: " + time_to_wait.getValue());

                                if(time_to_wait.getValue() == 0) {
                                    time_to_wait.setValue(30);
                                    can_enter.setValue(true);
                                    num_tries.setValue(0);
                                    timer.cancel();
                                }
                            });
                        }
                    }, 0, 1000);
                }
            }
            else {
                if(activity instanceof RegisterActivity) {
                    BiometricAuthenticator biometricAuthenticator = new BiometricAuthenticator(activity, null);

                    biometricAuthenticator.generateSecretKey(false);
                    Cipher cipher = biometricAuthenticator.getCypher();
                    SecretKey secretKey = biometricAuthenticator.getSecretKey();

                    try {
                        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

                        biometricAuthenticator.encrypt(cipher, documentId.getValue());
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    }
                }
                else {

                }
            }
        });

        return binding.getRoot();
    }
}