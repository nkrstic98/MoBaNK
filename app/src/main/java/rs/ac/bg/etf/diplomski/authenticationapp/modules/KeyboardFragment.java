package rs.ac.bg.etf.diplomski.authenticationapp.modules;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
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
import com.google.firebase.auth.FirebaseAuth;

import java.util.Timer;
import java.util.TimerTask;

import rs.ac.bg.etf.diplomski.authenticationapp.app_login.LoginActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.MainActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.app_second_factor_register.PinRegisterActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.app_second_factor_register.PinRegisterFragment;
import rs.ac.bg.etf.diplomski.authenticationapp.app_user_register.RegisterActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.FragmentKeyboardBinding;
import rs.ac.bg.etf.diplomski.authenticationapp.modules.BiometricAuthenticator;

public class KeyboardFragment extends Fragment {

    private static final int MAX_CHARS = 6;
    private static final int MAX_TRIES = 3;

    private FragmentActivity activity;
    private FragmentKeyboardBinding binding;

    private MutableLiveData<Integer> num_tries = new MutableLiveData<>(0);
    private MutableLiveData<Boolean> can_enter = new MutableLiveData<>(true);
    private MutableLiveData<Integer> time_to_wait = new MutableLiveData<>(30);
    private String pin_code = "";

    SharedPreferences registerSP;
    SharedPreferences sharedPreferences;

    private final Timer timer = new Timer();

    public KeyboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = requireActivity();

        registerSP = activity.getSharedPreferences(PinRegisterFragment.SHARED_PREFERENCES_REGISTER, Context.MODE_PRIVATE);
        sharedPreferences = activity.getSharedPreferences(BiometricAuthenticator.SHARED_PREFERENCES_ACCOUNT, Context.MODE_PRIVATE);
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

            String sp_pin;
            if(activity instanceof RegisterActivity) {
                sp_pin = registerSP.getString(PinRegisterFragment.SHARED_PREFERENCES_REGISTER_PIN, "");
            }
            else {
                sp_pin = sharedPreferences.getString(BiometricAuthenticator.SHARED_PREFERENCES_PIN_CODE_PARAMETER, "");
            }

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
                if(activity instanceof PinRegisterActivity) {
                   finishRegistration();

                   if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                       Intent intent = new Intent(activity, MainActivity.class);
                       intent.putExtra(MainActivity.REGISTRATION_COMPLETED, true);
                       activity.startActivity(intent);
                       activity.finish();
                   }
                   else {
                       Intent intent = new Intent(activity, LoginActivity.class);
                       activity.startActivity(intent);
                       activity.finish();
                   }
                }
                else if (activity instanceof LoginActivity) {
                    String email = sharedPreferences.getString(BiometricAuthenticator.SHARED_PREFERENCES_EMAIL_PARAMETER, "");
                    FirebaseAuth.getInstance()
                            .sendPasswordResetEmail(email)
                            .addOnSuccessListener(activity, aVoid -> {
                                Toast.makeText(activity, "Password reset email is sent to the address associated with this account!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(activity, e -> {
                                Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                    Intent intent = new Intent(activity, LoginActivity.class);
                    activity.startActivity(intent);
                    activity.finish();
                }
            }
        });

        return binding.getRoot();
    }

    private void finishRegistration() {
        String pin = registerSP.getString(PinRegisterFragment.SHARED_PREFERENCES_REGISTER_PIN, "");
        boolean biometry = registerSP.getBoolean(PinRegisterFragment.SHARED_PREFERENCES_REGISTER_BIOMETRY, false);

        sharedPreferences
                .edit()
                .putBoolean(BiometricAuthenticator.SHARED_PREFERENCES_BIOMETRY_PARAMETER, biometry)
                .putString(BiometricAuthenticator.SHARED_PREFERENCES_PIN_CODE_PARAMETER, pin)
                .commit();

        registerSP.edit().clear().apply();

        Toast.makeText(activity, "Success!", Toast.LENGTH_SHORT).show();
    }
}