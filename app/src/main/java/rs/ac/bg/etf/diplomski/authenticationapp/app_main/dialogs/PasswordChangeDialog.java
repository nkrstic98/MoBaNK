package rs.ac.bg.etf.diplomski.authenticationapp.app_main.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import rs.ac.bg.etf.diplomski.authenticationapp.R;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.AccountSettingsFragment;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.MainActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.UserViewModel;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.DialogPasswordChangeBinding;
import rs.ac.bg.etf.diplomski.authenticationapp.models.OPERATION;

public class PasswordChangeDialog extends DialogFragment {

    private MainActivity mainActivity;
    private DialogPasswordChangeBinding binding;

    private AlertDialog dialog;

    private AccountSettingsFragment.OperationCallback callback;

    public PasswordChangeDialog() {
        // Required empty public constructor
    }

    public PasswordChangeDialog(AccountSettingsFragment.OperationCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) requireActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DialogPasswordChangeBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = mainActivity.getLayoutInflater();

        builder
                .setTitle("Change password")
                .setView(inflater.inflate(R.layout.dialog_password_change, null))
                .setPositiveButton("Submit", (dialog1, which) -> {

                })
                .setNegativeButton("Cancel", (dialog1, which) -> {
                    dismiss();
                });

        dialog = builder.create();

        dialog.setOnShowListener(dialog1 -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String pass = ((EditText) dialog.findViewById(R.id.password)).getText().toString();
                String passc = ((EditText) dialog.findViewById(R.id.password_confirm)).getText().toString();

                if(pass.equals("") || passc.equals("")) {
                    ((EditText) dialog.findViewById(R.id.password)).setText("");
                    ((EditText) dialog.findViewById(R.id.password_confirm)).setText("");
                    ((TextInputLayout) dialog.findViewById(R.id.password_label)).getEditText().requestFocus();
                    mainActivity.runOnUiThread(() -> {
                        Toast.makeText(mainActivity, "You must fill out the fields in order to continue", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                if(pass.equals(passc)) {
                    callback.invoke(OPERATION.SET_PASSWORD, pass, dialog);
                }
                else {
                    ((EditText) dialog.findViewById(R.id.password)).setText("");
                    ((EditText) dialog.findViewById(R.id.password_confirm)).setText("");
                    ((TextInputLayout) dialog.findViewById(R.id.password_label)).getEditText().requestFocus();
                    mainActivity.runOnUiThread(() -> {
                        Toast.makeText(mainActivity, "Password and confirm password do not match. Try again!", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });

        return dialog;
    }
}