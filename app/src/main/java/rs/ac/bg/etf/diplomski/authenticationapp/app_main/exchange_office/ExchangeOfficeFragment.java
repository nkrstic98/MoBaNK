package rs.ac.bg.etf.diplomski.authenticationapp.app_main.exchange_office;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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

public class ExchangeOfficeFragment extends Fragment {

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

    private FragmentExchangeOfficeBinding binding;

    private String currency1;
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
            executeTransaction();
        });

        return binding.getRoot();
    }

    private void executeTransaction() {
        try {
            String payer = binding.firstCurrency.getSelectedItem().toString();
            String receiver = binding.secondCurrency.getSelectedItem().toString();
            double amount = fetchNumber(binding.amountLabel).doubleValue();

            double transfer_amount = 0;
            if(operation == EXCHANGE_OPERATION.BUY) {
                transfer_amount = amount * SELLING_RATE;
                if(!accountViewModel.hasEnoughFunds(payer, transfer_amount)) {
                    Toast.makeText(mainActivity, "There is not enough funds on the payer account!", Toast.LENGTH_SHORT).show();
                    return;
                }
                accountViewModel.executeInternalTransaction(payer, receiver, transfer_amount, amount);
            }
            else {
                transfer_amount = amount * PURCHASING_RATE;
                if(!accountViewModel.hasEnoughFunds(payer, amount)) {
                    Toast.makeText(mainActivity, "There is not enough funds on the payer account!", Toast.LENGTH_SHORT).show();
                    return;
                }
                accountViewModel.executeInternalTransaction(payer, receiver, amount, transfer_amount);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setupInitialState() {
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

    private Number fetchNumber(TextInputLayout textInputLayout) throws ParseException {
        Number result = 0;
        try {
            result = NumberFormat.getInstance().parse(textInputLayout.getEditText().getText().toString());
        }
        catch (ParseException e) {
            Toast.makeText(mainActivity, "Wrong value!", Toast.LENGTH_SHORT).show();
            textInputLayout.getEditText().requestFocus();
        }

        return result;
    }
}