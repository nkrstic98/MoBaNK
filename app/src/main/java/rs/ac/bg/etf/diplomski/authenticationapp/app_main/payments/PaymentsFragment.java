package rs.ac.bg.etf.diplomski.authenticationapp.app_main.payments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;

import rs.ac.bg.etf.diplomski.authenticationapp.R;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.MainActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.view_models.AccountViewModel;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.FragmentPaymentsBinding;
import rs.ac.bg.etf.diplomski.authenticationapp.di.ExecutorServiceModule;
import rs.ac.bg.etf.diplomski.authenticationapp.models.OPERATION;
import rs.ac.bg.etf.diplomski.authenticationapp.modules.BiometricAuthenticator;

import static rs.ac.bg.etf.diplomski.authenticationapp.modules.NumberOperations.fetchNumber;

public class PaymentsFragment extends Fragment {

    public static final String SHARED_PREFERENCES_PAYMENT = "shared-preferences-payment";
    public static final String RECEIVER_ACCOUNT = "payment-receiver";
    public static final String RECEIVER_INFO = "payment-receiver";
    public static final String PAYER_ACCOUNT = "payment-payer";
    public static final String PAYER_INFO = "payment-payer";
    public static final String PURPOSE = "payment-purpose";
    public static final String AMOUNT = "payment-amount";

    private MainActivity mainActivity;
    private AccountViewModel accountViewModel;
    private NavController navController;

    private FragmentPaymentsBinding binding;

    public PaymentsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) requireActivity();

        accountViewModel = new ViewModelProvider(mainActivity).get(AccountViewModel.class);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentPaymentsBinding.inflate(inflater, container, false);

        ArrayAdapter<String> from = new ArrayAdapter<>(
                mainActivity,
                android.R.layout.simple_list_item_1,
                accountViewModel.getAccounts("RSD")
        );
        binding.payerAccount.setAdapter(from);

        binding.buttonPay.setOnClickListener(v -> {
            startTransaction();
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
    }

    private void startTransaction() {
        if(!fieldValidation()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder
                .setTitle("Execute transaction?")
                .setMessage("Are you sure you want to proceed?")
                .setIcon(R.drawable.outline_payment_24)
                .setPositiveButton("Execute", (dialog, which) -> {

                })
                .setNegativeButton("Abort", (dialog, which) -> {
                    dialog.dismiss();
                });

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialog1 -> {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.BLUE);
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v1 -> {
                new BiometricAuthenticator(mainActivity, new BiometricAuthenticator.Callback() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void failure() {
                        SharedPreferences sp = mainActivity.getSharedPreferences(SHARED_PREFERENCES_PAYMENT, Context.MODE_PRIVATE);
                        sp.edit()
                                .putString(PAYER_ACCOUNT, binding.payerAccount.getSelectedItem().toString())
                                .putString(PAYER_INFO, binding.payer.getText().toString())
                                .putString(RECEIVER_ACCOUNT, getRecipientAccount())
                                .putString(RECEIVER_INFO, binding.recipient.getText().toString())
                                .putFloat(AMOUNT, fetchNumber(binding.amountLabel).floatValue())
                                .apply();

                        dialog.dismiss();
                        navController.navigate(PaymentsFragmentDirections.actionGlobalKeyboardFragmentMain(OPERATION.EXTERNAL_PAYMENT, ""));

//                        clearFields();
                    }

                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void success() {
                        dialog.dismiss();
                        executePayment();

//                        clearFields();
                    }
                }).authenticate();
            });
        });

        dialog.show();
    }

    private void executePayment() {
        String payerAccount = binding.payerAccount.getSelectedItem().toString();
        String payerInfo = binding.payer.getText().toString();
        String recipientInfo = binding.recipient.getText().toString();
        String purpose = binding.purpose.getText().toString();
        String recipientAccount = getRecipientAccount();
        double amount = fetchNumber(binding.amountLabel).doubleValue();

        if(!accountViewModel.hasEnoughFunds(payerAccount, amount)) {
            Toast.makeText(mainActivity, "There is not enough funds on the payer account!", Toast.LENGTH_SHORT).show();
            return;
        }

//        if(!payerAccount.substring(17, 19).equals(recipientAccount.substring(17, 19))) {
//            Toast.makeText(mainActivity, "Chosen account are not of same type! Enter valid accounts.", Toast.LENGTH_SHORT).show();
//            return;
//        }

        ExecutorService executorService = ExecutorServiceModule.provideExecutorService();

        accountViewModel.executeTransaction(
                payerAccount,
                payerInfo,
                recipientAccount,
                recipientInfo,
                purpose,
                purpose,
                amount,
                amount
        );
    }

    private boolean fieldValidation() {
        if(binding.payer.getText().toString().equals("")) {
            Toast.makeText(mainActivity, "Please enter payer info!", Toast.LENGTH_SHORT).show();
            binding.payerLabel.getEditText().requestFocus();
            return false;
        }

        if(binding.recipient.getText().toString().equals("")) {
            Toast.makeText(mainActivity, "Please enter recipient info!", Toast.LENGTH_SHORT).show();
            binding.recipientLabel.getEditText().requestFocus();
            return false;
        }

        if(binding.purpose.getText().toString().equals("")) {
            Toast.makeText(mainActivity, "Please enter payment purpose!", Toast.LENGTH_SHORT).show();
            binding.purposeLabel.getEditText().requestFocus();
            return false;
        }

        if(binding.recipientAccount1.getText().toString().equals("")
                || binding.recipientAccount2.getText().toString().equals("")
                || binding.recipientAccount3.getText().toString().equals("")
        ) {
            Toast.makeText(mainActivity, "Recipient account number format is wrong!", Toast.LENGTH_SHORT).show();
            binding.recipientAccount1.setText("");
            binding.recipientAccount2.setText("");
            binding.recipientAccount3.setText("");
            binding.recipientAccountLabel1.getEditText().requestFocus();
            return false;
        }

        if(binding.recipientAccount1.getText().toString().length() != 3
                || binding.recipientAccount2.getText().toString().length() != 13
                || binding.recipientAccount3.getText().toString().length() != 2
        ) {
            Toast.makeText(mainActivity, "Account number format is wrong!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(binding.amount.getText().toString().equals("")) {
            Toast.makeText(mainActivity, "Please enter payment amount!", Toast.LENGTH_SHORT).show();
            binding.amountLabel.getEditText().requestFocus();
            return false;
        }

        return true;
    }

    private String getRecipientAccount() {
        StringBuilder sb = new StringBuilder();
        sb
                .append(binding.recipientAccount1.getText().toString())
                .append("-")
                .append(binding.recipientAccount2.getText().toString())
                .append("-")
                .append(binding.recipientAccount3.getText().toString());

        return sb.toString();
    }

    private void clearFields() {
        binding.payer.setText("");
        binding.recipient.setText("");
        binding.purpose.setText("");
        binding.recipientAccount1.setText("");
        binding.recipientAccount2.setText("");
        binding.recipientAccount3.setText("");
        binding.amount.setText("");
    }
}