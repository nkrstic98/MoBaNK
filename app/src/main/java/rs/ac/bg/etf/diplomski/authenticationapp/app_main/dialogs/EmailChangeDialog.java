package rs.ac.bg.etf.diplomski.authenticationapp.app_main.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;

import rs.ac.bg.etf.diplomski.authenticationapp.R;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.AccountSettingsFragment;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.MainActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.DialogEmailChangeBinding;
import rs.ac.bg.etf.diplomski.authenticationapp.models.OPERATION;

public class EmailChangeDialog extends DialogFragment {

    private MainActivity mainActivity;
    private DialogEmailChangeBinding binding;

    private AlertDialog dialog;

    private AccountSettingsFragment.OperationCallback callback;

    public EmailChangeDialog() {
        // Required empty public constructor
    }

    public EmailChangeDialog(AccountSettingsFragment.OperationCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) requireActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogEmailChangeBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = mainActivity.getLayoutInflater();

        builder
                .setTitle("Change email address")
                .setView(inflater.inflate(R.layout.dialog_email_change, null))
                .setPositiveButton("Submit", (dialog1, which) -> {

                })
                .setNegativeButton("Cancel", (dialog1, which) -> {
                   dismiss();
                });

        dialog = builder.create();

        dialog.setOnShowListener(dialog1 -> {
            ((EditText) dialog.findViewById(R.id.email)).setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String data = ((EditText) dialog.findViewById(R.id.email)).getText().toString();
                if(data.equals("")) {
                    ((EditText) dialog.findViewById(R.id.email_label)).requestFocus();
                    mainActivity.runOnUiThread(() -> {
                        Toast.makeText(mainActivity, "Enter required information!", Toast.LENGTH_SHORT).show();
                    });
                }
                else {
                    callback.invoke(OPERATION.SET_EMAIL, data, dialog);
                }
            });
        });

        return dialog;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }
}