package rs.ac.bg.etf.diplomski.authenticationapp.modules;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.davidmiguel.numberkeyboard.NumberKeyboardListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.type.Money;

import java.util.Timer;
import java.util.TimerTask;

import rs.ac.bg.etf.diplomski.authenticationapp.R;
import rs.ac.bg.etf.diplomski.authenticationapp.app_login.LoginActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.MainActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.accounts_info.AccountViewModel;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.exchange_office.ExchangeOfficeFragment;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.money_transfer.MoneyTransferFragment;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.user_management.UserViewModel;
import rs.ac.bg.etf.diplomski.authenticationapp.app_second_factor_register.PinRegisterActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.app_second_factor_register.PinRegisterFragment;
import rs.ac.bg.etf.diplomski.authenticationapp.app_user_register.RegisterActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.FragmentKeyboardBinding;
import rs.ac.bg.etf.diplomski.authenticationapp.models.OPERATION;

import static rs.ac.bg.etf.diplomski.authenticationapp.app_main.exchange_office.ExchangeOfficeFragment.PURCHASING_RATE;
import static rs.ac.bg.etf.diplomski.authenticationapp.app_main.exchange_office.ExchangeOfficeFragment.SELLING_RATE;
import static rs.ac.bg.etf.diplomski.authenticationapp.models.OPERATION.*;

public class KeyboardFragment extends Fragment {

    private static final int MAX_CHARS = 6;
    private static final int MAX_TRIES = 3;

    private FragmentActivity activity;
    private UserViewModel userViewModel;
    private AccountViewModel accountViewModel;
    private FragmentKeyboardBinding binding;
    private NavController navController;

    private MutableLiveData<Integer> num_tries = new MutableLiveData<>(0);
    private MutableLiveData<Boolean> can_enter = new MutableLiveData<>(true);
    private MutableLiveData<Integer> time_to_wait = new MutableLiveData<>(30);
    private String pin_code = "";

    private SharedPreferences registerSP;
    private SharedPreferences sharedPreferences;

    private final Timer timer = new Timer();

    public KeyboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = requireActivity();

        userViewModel = null;
        if(activity instanceof MainActivity) {
            userViewModel = new ViewModelProvider(activity).get(UserViewModel.class);
            accountViewModel = new ViewModelProvider(activity).get(AccountViewModel.class);
        }

        registerSP = activity.getSharedPreferences(PinRegisterFragment.SHARED_PREFERENCES_REGISTER, Context.MODE_PRIVATE);
        sharedPreferences = activity.getSharedPreferences(BiometricAuthenticator.SHARED_PREFERENCES_ACCOUNT, Context.MODE_PRIVATE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentKeyboardBinding.inflate(inflater, container, false);

        if(activity instanceof MainActivity) {
            OPERATION operation = KeyboardFragmentArgs.fromBundle(getArguments()).getOperation();
            if(operation == SET_NEW_PIN) {
                binding.pinLabel.setText("Enter new PIN");
                Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar_main);
                toolbar.setTitle("Register new PIN Code");
            }
            else if(operation == CONFIRM_NEW_PIN) {
                binding.pinLabel.setText("Confirm new PIN");
                Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar_main);
                toolbar.setTitle("Register new PIN Code");
            }
        }

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
            if(newPinOperation()) return;
            validatePin();
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
    }

    private void validatePin() {
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
                pin_code = "";
                binding.pinCode.setText("");
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
            }
            else if (activity instanceof LoginActivity) {
                sendPasswordResetEmail();
            }
            else {
                doWork();
                navController.navigate(KeyboardFragmentDirections.actionKeyboardFragmentMainPop());
            }
        }
    }

    private void finishRegistration() {
        String pin = registerSP.getString(PinRegisterFragment.SHARED_PREFERENCES_REGISTER_PIN, "");
        boolean biometry = registerSP.getBoolean(PinRegisterFragment.SHARED_PREFERENCES_REGISTER_BIOMETRY, false);

        sharedPreferences
                .edit()
                .putBoolean(BiometricAuthenticator.SHARED_PREFERENCES_BIOMETRY_PARAMETER, biometry)
                .putString(BiometricAuthenticator.SHARED_PREFERENCES_PIN_CODE_PARAMETER, pin)
                .apply();

        registerSP.edit().clear().apply();

        Toast.makeText(activity, "Success!", Toast.LENGTH_SHORT).show();

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

    private void sendPasswordResetEmail() {
        String email = sharedPreferences.getString(BiometricAuthenticator.SHARED_PREFERENCES_EMAIL_PARAMETER, "");
        FirebaseAuth.getInstance()
                .sendPasswordResetEmail(email)
                .addOnSuccessListener(activity, aVoid -> {
                    Toast.makeText(activity, "Password reset email is sent to the address associated with this account!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(activity, LoginActivity.class);
                    activity.startActivity(intent);
                    activity.finish();
                })
                .addOnFailureListener(activity, e -> {
                    Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(activity, LoginActivity.class);
                    activity.startActivity(intent);
                    activity.finish();
                });
    }

    private boolean newPinOperation() {
        if(activity instanceof MainActivity) {
            OPERATION operation = KeyboardFragmentArgs.fromBundle(getArguments()).getOperation();

            if(operation == SET_NEW_PIN || operation == CONFIRM_NEW_PIN) {
                if(pin_code.length() < MAX_CHARS) {
                    Toast.makeText(activity, "Pin code must be 6 characters long", Toast.LENGTH_SHORT).show();
                    pin_code = "";
                    binding.pinCode.setText("");
                    return true;
                }
            }

            if(operation == SET_NEW_PIN) {
                navController.navigate(KeyboardFragmentDirections.actionGlobalKeyboardFragmentMain(CONFIRM_NEW_PIN, pin_code));
                return true;
            }
            else if(operation == CONFIRM_NEW_PIN) {
                String firstPin = KeyboardFragmentArgs.fromBundle(getArguments()).getData();
                if(firstPin.equals(pin_code)) {
                    changePin();
                }
                else {
                    Toast.makeText(activity, "Pin and confirm pin do not match. Try again!", Toast.LENGTH_SHORT).show();
                    navController.navigate(KeyboardFragmentDirections.actionKeyboardFragmentMainPop());
                    navController.navigate(KeyboardFragmentDirections.actionKeyboardFragmentMainPop());
                    navController.navigate(KeyboardFragmentDirections.actionGlobalKeyboardFragmentMain(SET_NEW_PIN, ""));
                }
                return true;
            }
        }

        return false;
    }

    private void doWork() {
        OPERATION operation = KeyboardFragmentArgs.fromBundle(getArguments()).getOperation();
        String data = KeyboardFragmentArgs.fromBundle(getArguments()).getData();

        switch (operation)
        {
            case SET_FINGERPRINT:
                fingerprint();
                break;

            case SET_EMAIL:
                changeEmail(data);
                break;

            case SET_PASSWORD:
                changePassword(data);
                break;

            case REGISTER_NEW_PIN:
                updatePin(data);
                break;

            case DELETE_ACCOUNT:
                deleteUser();
                break;

            case EXCHANGE_OFFICE:
                exchangeOffice();

            case INTERNAL_TRANSFER:
                internalTransfer();

            default:
                break;
        }
    }

    private void fingerprint() {
        sharedPreferences
                .edit()
                .putBoolean(
                        BiometricAuthenticator.SHARED_PREFERENCES_BIOMETRY_PARAMETER, !sharedPreferences.getBoolean(BiometricAuthenticator.SHARED_PREFERENCES_BIOMETRY_PARAMETER, false)
                )
                .apply();
    }

    private void changeEmail(String email) {
        userViewModel.updateEmail(email, (op, data, alertDialog) -> {
            sharedPreferences
                    .edit()
                    .putString(BiometricAuthenticator.SHARED_PREFERENCES_EMAIL_PARAMETER, email)
                    .apply();
        });
    }

    private void changePassword(String pass) {
        userViewModel.changePassword(pass);
    }

    private void changePin() {
        if(sharedPreferences.getBoolean(BiometricAuthenticator.SHARED_PREFERENCES_BIOMETRY_PARAMETER, false)) {
            new BiometricAuthenticator(activity, new BiometricAuthenticator.Callback() {
                @Override
                public void failure() {
                    navController.navigate(KeyboardFragmentDirections.actionGlobalKeyboardFragmentMain(REGISTER_NEW_PIN, pin_code));
                }

                @Override
                public void success() {
                    updatePin(pin_code);
                }
            }).authenticate();
        }
        else {
            navController.navigate(KeyboardFragmentDirections.actionGlobalKeyboardFragmentMain(REGISTER_NEW_PIN, pin_code));
        }
    }

    private void updatePin(String data) {
        sharedPreferences
                .edit()
                .putString(BiometricAuthenticator.SHARED_PREFERENCES_PIN_CODE_PARAMETER, data)
                .apply();

        Toast.makeText(activity, "New pin successfully set", Toast.LENGTH_SHORT).show();

        navController.navigate(KeyboardFragmentDirections.actionKeyboardFragmentMainPop());
        navController.navigate(KeyboardFragmentDirections.actionKeyboardFragmentMainPop());
    }

    private void deleteUser() {
        userViewModel.deleteUser((op, data, alertDialog) -> {
            sharedPreferences.edit().clear().commit();

            Intent intent = new Intent(activity, RegisterActivity.class);
            activity.startActivity(intent);
            activity.finish();
        });
    }

    private void exchangeOffice() {
        SharedPreferences sp = activity.getSharedPreferences(ExchangeOfficeFragment.SHARED_PREFERENCES_EXCHANGE_OFFICE, Context.MODE_PRIVATE);

        String payer = sp.getString(ExchangeOfficeFragment.EXCHANGE_OFFICE_PAYER, "");
        String receiver = sp.getString(ExchangeOfficeFragment.EXCHANGE_OFFICE_RECEIVER, "");
        double amount = sp.getFloat(ExchangeOfficeFragment.EXCHANGE_OFFICE_AMOUNT, 0);

        double transfer_amount = 0;
        if(sp.getString(ExchangeOfficeFragment.EXCHANGE_OFFICE_OPERATION, "").equals("buy")) {
            transfer_amount = amount * SELLING_RATE;
            if(!accountViewModel.hasEnoughFunds(payer, transfer_amount)) {
                Toast.makeText(activity, "There is not enough funds on the payer account!", Toast.LENGTH_SHORT).show();
                return;
            }
            accountViewModel.executeInternalTransaction(payer, receiver, transfer_amount, amount);
        }
        else {
            transfer_amount = amount * PURCHASING_RATE;
            if(!accountViewModel.hasEnoughFunds(payer, amount)) {
                Toast.makeText(activity, "There is not enough funds on the payer account!", Toast.LENGTH_SHORT).show();
                return;
            }
            accountViewModel.executeInternalTransaction(payer, receiver, amount, transfer_amount);
        }

        sp.edit().clear().apply();
    }

    private void internalTransfer() {
        SharedPreferences sp = activity.getSharedPreferences(MoneyTransferFragment.SHARED_PREFERENCES_TRANSFER, Context.MODE_PRIVATE);

        String payer = sp.getString(MoneyTransferFragment.TRANSFER_PAYER, "");
        String receiver = sp.getString(MoneyTransferFragment.TRANSFER_RECEIVER, "");
        double amount = sp.getFloat(MoneyTransferFragment.TRANSFER_AMOUNT, 0);

        if(!accountViewModel.hasEnoughFunds(payer, amount)) {
            Toast.makeText(activity, "There is not enough funds on the payer account!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!payer.substring(17, 19).equals(receiver.substring(17, 19))) {
            Toast.makeText(activity, "Chosen account are not of same type! Enter valid accounts.", Toast.LENGTH_SHORT).show();
            return;
        }

        accountViewModel.executeInternalTransaction(payer, receiver, amount, amount);
    }
}