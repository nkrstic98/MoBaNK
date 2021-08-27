package rs.ac.bg.etf.diplomski.authenticationapp.app_main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import rs.ac.bg.etf.diplomski.authenticationapp.R;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.FragmentAccountSettingsBinding;
import rs.ac.bg.etf.diplomski.authenticationapp.models.OPERATION;
import rs.ac.bg.etf.diplomski.authenticationapp.models.User;
import rs.ac.bg.etf.diplomski.authenticationapp.modules.BiometricAuthenticator;
import rs.ac.bg.etf.diplomski.authenticationapp.modules.KeyboardFragment;
import rs.ac.bg.etf.diplomski.authenticationapp.modules.KeyboardFragmentDirections;

public class AccountSettingsFragment extends Fragment {

    private MainActivity mainActivity;
    private FragmentAccountSettingsBinding binding;
    private SharedPreferences sharedPreferences;
    private NavController navController;

    private MutableLiveData<Boolean> moreOptions = new MutableLiveData<>(false);
    private MutableLiveData<String> userPhone = new MutableLiveData<>("");
    private MutableLiveData<Boolean> use_biometry = new MutableLiveData<>(false);

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseUser user;

    public AccountSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) requireActivity();
        sharedPreferences = mainActivity.getSharedPreferences(BiometricAuthenticator.SHARED_PREFERENCES_ACCOUNT, Context.MODE_PRIVATE);

        firebaseAuth=  FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        user = firebaseAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentAccountSettingsBinding.inflate(inflater, container, false);

        use_biometry.setValue(sharedPreferences.getBoolean(BiometricAuthenticator.SHARED_PREFERENCES_BIOMETRY_PARAMETER, false));

        firebaseFirestore.collection("users")
                .whereEqualTo("email", firebaseAuth.getCurrentUser().getEmail())
                .get()
                .addOnCompleteListener(mainActivity, task -> {
                    if(task.isSuccessful()) {
                        List<DocumentSnapshot> documents =  task.getResult().getDocuments();
                        if(documents.size() != 0) {
                            User myUser = documents.get(0).toObject(User.class);
                            userPhone.setValue(myUser.getPhone());
                            binding.phone.setText(userPhone.getValue());
                        }
                    }
                });

        binding.user.setText(user.getDisplayName());
        binding.email.setText(user.getEmail());

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
            if(use_biometry.getValue()) {
                new BiometricAuthenticator(mainActivity, new BiometricAuthenticator.Callback() {
                    @Override
                    public void failure() {
                        navController.navigate(KeyboardFragmentDirections.actionGlobalKeyboardFragmentMain(OPERATION.SET_FINGERPRINT));
                    }

                    @Override
                    public void success() {
                        use_biometry.setValue(isChecked);
                        sharedPreferences
                                .edit()
                                .putBoolean(BiometricAuthenticator.SHARED_PREFERENCES_BIOMETRY_PARAMETER, isChecked)
                                .commit();
                    }
                }).authenticate();
            }
            else {
                navController.navigate(KeyboardFragmentDirections.actionGlobalKeyboardFragmentMain(OPERATION.SET_FINGERPRINT));
            }
        });

        binding.emailCard.setOnClickListener(v -> {

        });

        binding.passwordCard.setOnClickListener(v -> {

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
}