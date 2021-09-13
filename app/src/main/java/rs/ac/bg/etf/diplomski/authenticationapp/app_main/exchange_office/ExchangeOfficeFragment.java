package rs.ac.bg.etf.diplomski.authenticationapp.app_main.exchange_office;

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

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.text.NumberFormat;
import java.text.ParseException;

import rs.ac.bg.etf.diplomski.authenticationapp.R;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.MainActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.accounts_info.AccountViewModel;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.FragmentExchangeOfficeBinding;
import rs.ac.bg.etf.diplomski.authenticationapp.models.OPERATION;
import rs.ac.bg.etf.diplomski.authenticationapp.modules.BiometricAuthenticator;
import rs.ac.bg.etf.diplomski.authenticationapp.modules.NumberOperations;

public class ExchangeOfficeFragment extends Fragment {

    public static final String SHARED_PREFERENCES_EXCHANGE_OFFICE = "shared-preferences-exchange-office";
    public static final String EXCHANGE_OFFICE_OPERATION = "exchange-office-operation";
    public static final String EXCHANGE_OFFICE_RECEIVER = "exchange-office-receiver";
    public static final String EXCHANGE_OFFICE_PAYER = "exchange-office-payer";
    public static final String EXCHANGE_OFFICE_AMOUNT = "exchange-office-amount";

    public enum EXCHANGE_OPERATION { BUY, SELL };

    /**
     * na dan 10.09.2021.
     */
    public static final double SELLING_RATE = 117.92;
    public static final double PURCHASING_RATE = 117.21;

    public static final String EUR_CURRENCY = "EUR";
    public static final String RSD_CURRENCY = "RSD";

    private MainActivity mainActivity;
    private AccountViewModel accountViewModel;
    private NavController navController;

    private FragmentExchangeOfficeBinding binding;

    private EXCHANGE_OPERATION operation;

    public ExchangeOfficeFragment() {
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

        binding = FragmentExchangeOfficeBinding.inflate(inflater, container, false);

        operation = EXCHANGE_OPERATION.BUY;
        setupInitialState();

        binding.buttonBuy.setOnClickListener(v -> {
            operation = EXCHANGE_OPERATION.BUY;
            setupInitialState();
        });

        binding.buttonSell.setOnClickListener(v -> {
            operation = EXCHANGE_OPERATION.SELL;
            setupInitialState();
        });

        binding.amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().equals("")) {
                    binding.targetedAmount.setText("00.00 RSD");
                    return;
                }

                double value = Double.valueOf(s.toString());
                if(operation == EXCHANGE_OPERATION.BUY)
                    binding.targetedAmount.setText(value * SELLING_RATE + " RSD");
                else {
                    binding.targetedAmount.setText(value * PURCHASING_RATE + " RSD");
                }
            }
        });

        binding.buttonExchange.setOnClickListener(v -> {
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
        if(binding.amount.getText().toString().equals("")) {
            Toast.makeText(mainActivity, "Please, enter amount to proceed with transaction!", Toast.LENGTH_SHORT).show();
            binding.amountLabel.getEditText().requestFocus();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder
                .setTitle("Execute transaction?")
                .setMessage("Are you sure you want to proceed?")
                .setIcon(R.drawable.outline_euro_20)
                .setPositiveButton("Execute", (dialog, which) -> {

                })
                .setNegativeButton("Abort", (dialog, which) -> {
                    dialog.dismiss();
                });

        androidx.appcompat.app.AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialog1 -> {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.BLUE);
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v1 -> {
                new BiometricAuthenticator(mainActivity, new BiometricAuthenticator.Callback() {
                    @Override
                    public void failure() {
                        SharedPreferences sp = mainActivity.getSharedPreferences(SHARED_PREFERENCES_EXCHANGE_OFFICE, Context.MODE_PRIVATE);
                        sp.edit()
                                .putString(EXCHANGE_OFFICE_OPERATION, operation == EXCHANGE_OPERATION.BUY ? "buy" : "sell")
                                .putString(EXCHANGE_OFFICE_PAYER, binding.firstCurrency.getSelectedItem().toString())
                                .putString(EXCHANGE_OFFICE_RECEIVER, binding.secondCurrency.getSelectedItem().toString())
                                .putFloat(EXCHANGE_OFFICE_AMOUNT, NumberOperations.fetchNumber(binding.amountLabel).floatValue())
                                .apply();

                        dialog.dismiss();
                        navController.navigate(ExchangeOfficeFragmentDirections.actionGlobalKeyboardFragmentMain(OPERATION.EXCHANGE_OFFICE, ""));

                        binding.amount.setText("");
                        binding.amountLabel.clearFocus();
                        binding.targetedAmount.setText("");
                    }

                    @Override
                    public void success() {
                        dialog.dismiss();
                        executeTransaction();

                        binding.amount.setText("");
                        binding.amountLabel.clearFocus();
                        binding.targetedAmount.setText("");
                    }
                }).authenticate();
            });
        });

        dialog.show();
    }

    private void executeTransaction() {
        String payer = binding.firstCurrency.getSelectedItem().toString();
        String receiver = binding.secondCurrency.getSelectedItem().toString();
        double amount = NumberOperations.fetchNumber(binding.amountLabel).doubleValue();

        double transfer_amount = 0;

        if(operation == EXCHANGE_OPERATION.BUY) {
            transfer_amount = amount * SELLING_RATE;
            if(!accountViewModel.hasEnoughFunds(payer, transfer_amount)) {
                Toast.makeText(mainActivity, "There is not enough funds on the payer account!", Toast.LENGTH_SHORT).show();
                return;
            }
            accountViewModel.executeTransaction(
                    payer,
                    "",
                    receiver,
                    "",
                    "EUR buy to account " + receiver,
                    "EUR buy from account " + payer,
                    transfer_amount,
                    amount
            );
        }
        else {
            transfer_amount = amount * PURCHASING_RATE;
            if(!accountViewModel.hasEnoughFunds(payer, amount)) {
                Toast.makeText(mainActivity, "There is not enough funds on the payer account!", Toast.LENGTH_SHORT).show();
                return;
            }
            accountViewModel.executeTransaction(
                    payer,
                    "",
                    receiver,
                    "",
                    "EUR sell to account " + receiver,
                    "EUR sell from account " + payer,
                    amount,
                    transfer_amount
            );
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setupInitialState() {
        binding.amount.setText("");
        binding.targetedAmount.setText("00.00");

        binding.operationGroup.check(operation == EXCHANGE_OPERATION.BUY ? R.id.button_buy : R.id.button_sell);

        binding.firstCurrencyLabel.setText(operation == EXCHANGE_OPERATION.BUY ? "Basic accounts" : "Foreign Exchange accounts");
        binding.secondCurrencyLabel.setText(operation == EXCHANGE_OPERATION.SELL ?  "Basic accounts" : "Foreign Exchange accounts");

        //binding.amountLabel.setSuffixText(operation == EXCHANGE_OPERATION.BUY ? RSD_CURRENCY : EUR_CURRENCY);

        ArrayAdapter<String> firstCurrency = new ArrayAdapter<>(
                mainActivity,
                android.R.layout.simple_list_item_1,
                accountViewModel.getAccounts(operation == EXCHANGE_OPERATION.BUY ? RSD_CURRENCY : EUR_CURRENCY)
        );
        binding.firstCurrency.setAdapter(firstCurrency);

        ArrayAdapter<String> secondCurrency = new ArrayAdapter<>(
                mainActivity,
                android.R.layout.simple_list_item_1,
                accountViewModel.getAccounts(operation == EXCHANGE_OPERATION.BUY ? EUR_CURRENCY : RSD_CURRENCY)
        );
        binding.secondCurrency.setAdapter(secondCurrency);
    }
}