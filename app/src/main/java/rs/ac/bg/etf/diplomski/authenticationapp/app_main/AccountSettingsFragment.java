package rs.ac.bg.etf.diplomski.authenticationapp.app_main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import dagger.hilt.android.scopes.ViewModelScoped;
import rs.ac.bg.etf.diplomski.authenticationapp.R;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.dialogs.EmailChangeDialog;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.dialogs.PasswordChangeDialog;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.FragmentAccountSettingsBinding;
import rs.ac.bg.etf.diplomski.authenticationapp.models.OPERATION;
import rs.ac.bg.etf.diplomski.authenticationapp.modules.BiometricAuthenticator;
import rs.ac.bg.etf.diplomski.authenticationapp.modules.KeyboardFragmentDirections;

@ViewModelScoped
public class AccountSettingsFragment extends Fragment {

    private MainActivity mainActivity;
    private UserViewModel userViewModel;
    private FragmentAccountSettingsBinding binding;
    private SharedPreferences sharedPreferences;
    private NavController navController;

    private MutableLiveData<Boolean> moreOptions = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> use_biometry = new MutableLiveData<>(false);

    public AccountSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) requireActivity();
        userViewModel = new ViewModelProvider(mainActivity).get(UserViewModel.class);
        sharedPreferences = mainActivity.getSharedPreferences(BiometricAuthenticator.SHARED_PREFERENCES_ACCOUNT, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentAccountSettingsBinding.inflate(inflater, container, false);

        use_biometry.setValue(sharedPreferences.getBoolean(BiometricAuthenticator.SHARED_PREFERENCES_BIOMETRY_PARAMETER, false));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        binding.user.setText(user.getDisplayName());
        binding.email.setText(user.getEmail());

        userViewModel.setData(mainActivity, (data, dialog) -> {
            binding.phone.setText(userViewModel.getPhone());
        });

        binding.optionsMore.setVisibility(moreOptions.getValue() ? View.VISIBLE : View.GONE);
        binding.buttonMoreOptions.setImageDrawable(
                moreOptions.getValue()
                        ?
                        ContextCompat.getDrawable(mainActivity, R.drawable.outline_keyboard_arrow_up_24)
                        :
                        ContextCompat.getDrawable(mainActivity, R.drawable.outline_keyboard_arrow_down_24)
        );

        binding.buttonMoreOptions.setOnClickListener(v -> {
            moreOptions.setValue(!moreOptions.getValue());

            if(moreOptions.getValue()) {
                TransitionManager.beginDelayedTransition(binding.optionsMore, new AutoTransition());
                binding.optionsMore.setVisibility(View.VISIBLE);
                binding.buttonMoreOptions.setImageDrawable(ContextCompat.getDrawable(mainActivity, R.drawable.outline_keyboard_arrow_up_24));
                binding.security.setText("Collapse for fewer options");
            }
            else {
                TransitionManager.beginDelayedTransition(binding.optionsMore, new AutoTransition());
                binding.optionsMore.setVisibility(View.GONE);
                binding.buttonMoreOptions.setImageDrawable(ContextCompat.getDrawable(mainActivity, R.drawable.outline_keyboard_arrow_down_24));
                binding.security.setText("Expand for more options");
            }
        });

        binding.biometryAuth.setChecked(use_biometry.getValue());

        binding.biometryAuth.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBiometry(isChecked);
        });

        binding.emailCard.setOnClickListener(v -> {
            new EmailChangeDialog(this::changeEmail).show(getChildFragmentManager(), "input-dialog");
        });

        binding.passwordCard.setOnClickListener(v -> {
            new PasswordChangeDialog(this::changePassword).show(getChildFragmentManager(), "password-change-dialog");
        });

        binding.pinCard.setOnClickListener(v -> {

        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
    }

    public interface OperationCallback {
        void invoke(String data, AlertDialog alertDialog);
    }

    private void updateBiometry(boolean isChecked) {
        if(use_biometry.getValue()) {
            new BiometricAuthenticator(mainActivity, new BiometricAuthenticator.Callback() {
                @Override
                public void failure() {
                    navController.navigate(KeyboardFragmentDirections.actionGlobalKeyboardFragmentMain(OPERATION.SET_FINGERPRINT, ""));
                }

                @Override
                public void success() {
                    use_biometry.setValue(isChecked);
                    sharedPreferences
                            .edit()
                            .putBoolean(BiometricAuthenticator.SHARED_PREFERENCES_BIOMETRY_PARAMETER, isChecked)
                            .apply();
                }
            }).authenticate();
        }
        else {
            navController.navigate(KeyboardFragmentDirections.actionGlobalKeyboardFragmentMain(OPERATION.SET_FINGERPRINT, ""));
        }
    }

    private void changeEmail(String email, AlertDialog dialog) {
        if(sharedPreferences.getBoolean(BiometricAuthenticator.SHARED_PREFERENCES_BIOMETRY_PARAMETER, false)) {
            new BiometricAuthenticator(mainActivity, new BiometricAuthenticator.Callback() {
                @Override
                public void failure() {
                    dialog.dismiss();
                    navController.navigate(KeyboardFragmentDirections.actionGlobalKeyboardFragmentMain(OPERATION.SET_EMAIL, email));
                }

                @Override
                public void success() {
                    userViewModel.updateEmail(email, (data, alertDialog) -> {
                        binding.email.setText(email);
                        updateSharedPreferencesEmail(email);
                    });
                    dialog.dismiss();
                }
            }).authenticate();
        }
        else {
            dialog.dismiss();
            navController.navigate(KeyboardFragmentDirections.actionGlobalKeyboardFragmentMain(OPERATION.SET_EMAIL, email));
        }
    }

    private void updateSharedPreferencesEmail(String email) {
        sharedPreferences
                .edit()
                .putString(BiometricAuthenticator.SHARED_PREFERENCES_EMAIL_PARAMETER, email)
                .apply();
    }

    private void changePassword(String pass, AlertDialog dialog) {
        if(sharedPreferences.getBoolean(BiometricAuthenticator.SHARED_PREFERENCES_BIOMETRY_PARAMETER, false)) {
            new BiometricAuthenticator(mainActivity, new BiometricAuthenticator.Callback() {
                @Override
                public void failure() {
                    dialog.dismiss();
                    navController.navigate(KeyboardFragmentDirections.actionGlobalKeyboardFragmentMain(OPERATION.SET_PASSWORD, pass));
                }

                @Override
                public void success() {
                    userViewModel.changePassword(pass);
                    dialog.dismiss();
                }
            }).authenticate();
        }
        else {
            dialog.dismiss();
            navController.navigate(KeyboardFragmentDirections.actionGlobalKeyboardFragmentMain(OPERATION.SET_PASSWORD, pass));
        }
    }
}