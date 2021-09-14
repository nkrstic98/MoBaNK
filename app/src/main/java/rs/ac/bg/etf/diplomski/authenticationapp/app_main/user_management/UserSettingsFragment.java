package rs.ac.bg.etf.diplomski.authenticationapp.app_main.user_management;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.io.ByteArrayOutputStream;

import dagger.hilt.android.scopes.ViewModelScoped;
import rs.ac.bg.etf.diplomski.authenticationapp.R;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.MainActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.dialogs.EmailChangeDialog;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.dialogs.PasswordChangeDialog;
import rs.ac.bg.etf.diplomski.authenticationapp.app_user_register.RegisterActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.FragmentUserSettingsBinding;
import rs.ac.bg.etf.diplomski.authenticationapp.models.OPERATION;
import rs.ac.bg.etf.diplomski.authenticationapp.modules.BiometricAuthenticator;
import rs.ac.bg.etf.diplomski.authenticationapp.modules.KeyboardFragmentDirections;
import rs.ac.bg.etf.diplomski.authenticationapp.view_models.UserViewModel;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

@ViewModelScoped
public class UserSettingsFragment extends Fragment {

    private static final int REQUEST_CODE_PERMISSIONS = 10;
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    private static final String[] REQUIRED_PERMISSIONS = { Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE };

    private MainActivity mainActivity;
    private UserViewModel userViewModel;
    private FragmentUserSettingsBinding binding;
    private SharedPreferences sharedPreferences;
    private NavController navController;

    private MutableLiveData<Boolean> moreOptions = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> use_biometry = new MutableLiveData<>(false);

    public UserSettingsFragment() {
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

        binding = FragmentUserSettingsBinding.inflate(inflater, container, false);

        use_biometry.setValue(sharedPreferences.getBoolean(BiometricAuthenticator.SHARED_PREFERENCES_BIOMETRY_PARAMETER, false));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        binding.user.setText(user.getDisplayName());
        binding.email.setText(user.getEmail());
        if(user.getPhotoUrl() != null) {
            binding.imageProfile.setImageURI(user.getPhotoUrl());
        }

        userViewModel.setData(mainActivity, (op, data, dialog) -> {
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

        binding.emailCard.setOnClickListener(v -> {
            new EmailChangeDialog(this::sendUpdateRequest).show(getChildFragmentManager(), "input-dialog");
        });

        binding.passwordCard.setOnClickListener(v -> {
            new PasswordChangeDialog(this::sendUpdateRequest).show(getChildFragmentManager(), "password-change-dialog");
        });

        binding.pinCard.setOnClickListener(v -> {
            navController.navigate(KeyboardFragmentDirections.actionGlobalKeyboardFragmentMain(OPERATION.SET_NEW_PIN, ""));
        });

        binding.biometryAuth.setChecked(use_biometry.getValue());
        binding.biometryAuth.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBiometry(isChecked);
        });

        binding.buttonDeleteAccount.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder
                    .setTitle("Delete account")
                    .setMessage("Are you sure you want to delete your account?")
                    .setIcon(R.drawable.outline_warning_24)
                    .setPositiveButton("Delete", (dialog, which) -> {

                    })
                    .setNegativeButton("Abort", (dialog, which) -> {
                        dialog.dismiss();
                    });

            AlertDialog dialog = builder.create();

            dialog.setOnShowListener(dialog1 -> {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.RED);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v1 -> {
                    sendUpdateRequest(OPERATION.DELETE_ACCOUNT, "", dialog);
                });
            });

            dialog.show();
        });

        binding.imageProfile.setOnClickListener(v -> {
            if(!allPermissionsGranted()) {
                ActivityCompat.requestPermissions(
                        mainActivity, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
                );
                if(allPermissionsGranted()) {
                    selectImage(mainActivity);
                }
            }
            else {
                selectImage(mainActivity);
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
    }

    public interface OperationCallback {
        void invoke(OPERATION operation, String data, AlertDialog alertDialog);
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

    private void sendUpdateRequest(OPERATION operation, String data, AlertDialog dialog) {
        if(sharedPreferences.getBoolean(BiometricAuthenticator.SHARED_PREFERENCES_BIOMETRY_PARAMETER, false)) {
            new BiometricAuthenticator(mainActivity, new BiometricAuthenticator.Callback() {
                @Override
                public void failure() {
                    dialog.dismiss();
                    navController.navigate(KeyboardFragmentDirections.actionGlobalKeyboardFragmentMain(operation, data));
                }

                @Override
                public void success() {
                    executeUpdate(operation, data);
                    dialog.dismiss();
                }
            }).authenticate();
        }
        else {
            dialog.dismiss();
            navController.navigate(KeyboardFragmentDirections.actionGlobalKeyboardFragmentMain(operation, data));
        }
    }

    private void executeUpdate(OPERATION operation, String data) {
        switch (operation)
        {
            case SET_EMAIL:
                updateEmail(data);
                break;

            case SET_PASSWORD:
                updatePassword(data);
                break;

            case DELETE_ACCOUNT:
                deleteUser();
                break;
        }
    }

    private void updateEmail(String email) {
        userViewModel.updateEmail(email, (op, data, alertDialog) -> {
            binding.email.setText(email);

            sharedPreferences
                    .edit()
                    .putString(BiometricAuthenticator.SHARED_PREFERENCES_EMAIL_PARAMETER, email)
                    .apply();
        });
    }

    private void updatePassword(String pass) {
        userViewModel.changePassword(pass);
    }

    private void deleteUser() {
        userViewModel.deleteUser((op, data, alertDialog) -> {
            sharedPreferences.edit().clear().commit();

            Intent intent = new Intent(mainActivity, RegisterActivity.class);
            mainActivity.startActivity(intent);
            mainActivity.finish();
        });
    }

    private void selectImage(Context context) {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose your profile picture");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {
                    SharedPreferences sp = mainActivity.getSharedPreferences(MainActivity.SP_PROFILE_IMAGE, Context.MODE_PRIVATE);
                    sp.edit().putBoolean(MainActivity.IMAGE_DATA, true).apply();
                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);

                } else if (options[item].equals("Choose from Gallery")) {
                    SharedPreferences sp = mainActivity.getSharedPreferences(MainActivity.SP_PROFILE_IMAGE, Context.MODE_PRIVATE);
                    sp.edit().putBoolean(MainActivity.IMAGE_DATA, true).apply();
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, 1);//one can be replaced with any action code

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != RESULT_CANCELED) {
            Uri imageUri = null;
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        imageUri = getImageUri(mainActivity, selectedImage);
                    }

                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        imageUri = data.getData();
                    }
                    break;
            }

            if(imageUri != null) {
                binding.imageProfile.setImageURI(imageUri);
                userViewModel.updateProfile(imageUri);
                ImageView imageView = (ImageView) mainActivity.findViewById(R.id.imageProfile_header);
                imageView.setImageURI(imageUri);
            }
        }
    }

    private boolean allPermissionsGranted() {
        for(int i = 0; i < REQUIRED_PERMISSIONS.length; i++) {
            if(ContextCompat.checkSelfPermission(mainActivity, REQUIRED_PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

}