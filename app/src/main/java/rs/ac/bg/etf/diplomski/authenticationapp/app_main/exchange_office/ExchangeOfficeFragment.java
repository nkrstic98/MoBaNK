package rs.ac.bg.etf.diplomski.authenticationapp.app_main.exchange_office;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import rs.ac.bg.etf.diplomski.authenticationapp.R;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.MainActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.accounts_info.AccountViewModel;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.transactions.TransactionViewModel;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.FragmentExchangeOfficeBinding;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.FragmentTransactionsBinding;

public class ExchangeOfficeFragment extends Fragment {

    public static final String EUR_CURRENCY = "EUR";
    public static final String RSD_CURRENCY = "RSD";

    private MainActivity mainActivity;
    private AccountViewModel accountViewModel;
    private TransactionViewModel transactionViewModel;

    private FragmentExchangeOfficeBinding binding;

    private String currency1;

    public ExchangeOfficeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) requireActivity();

        accountViewModel = new ViewModelProvider(mainActivity).get(AccountViewModel.class);
        transactionViewModel = new ViewModelProvider(mainActivity).get(TransactionViewModel.class);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentExchangeOfficeBinding.inflate(inflater, container, false);

        currency1 = RSD_CURRENCY;
        setupInitialState();

        binding.buttonBuy.setOnClickListener(v -> {
            currency1 = RSD_CURRENCY;

            setupInitialState();
        });

        binding.buttonSell.setOnClickListener(v -> {
            currency1 = EUR_CURRENCY;

            setupInitialState();
        });

        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setupInitialState() {
        binding.operationGroup.check(currency1.equals(RSD_CURRENCY) ? R.id.button_buy : R.id.button_sell);

        binding.firstCurrencyLabel.setText(currency1.equals(RSD_CURRENCY) ? "Basic accounts" : "Foreign Exchange accounts");
        binding.secondCurrencyLabel.setText(currency1.equals(EUR_CURRENCY) ?  "Basic accounts" : "Foreign Exchange accounts");

        binding.amountLabel.setSuffixText(currency1);

        ArrayAdapter<String> firstCurrency = new ArrayAdapter<>(
                mainActivity,
                android.R.layout.simple_list_item_1,
                accountViewModel.getAccounts(currency1.equals(RSD_CURRENCY) ? RSD_CURRENCY : EUR_CURRENCY)
        );
        binding.firstCurrency.setAdapter(firstCurrency);

        ArrayAdapter<String> secondCurrency = new ArrayAdapter<>(
                mainActivity,
                android.R.layout.simple_list_item_1,
                accountViewModel.getAccounts(currency1.equals(RSD_CURRENCY) ? RSD_CURRENCY : EUR_CURRENCY)
        );
        binding.secondCurrency.setAdapter(secondCurrency);
    }
}