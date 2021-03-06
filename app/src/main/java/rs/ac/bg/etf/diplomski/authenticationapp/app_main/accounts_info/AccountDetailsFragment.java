package rs.ac.bg.etf.diplomski.authenticationapp.app_main.accounts_info;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import rs.ac.bg.etf.diplomski.authenticationapp.app_main.MainActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.FragmentAccountDetailsBinding;
import rs.ac.bg.etf.diplomski.authenticationapp.models.Account;
import rs.ac.bg.etf.diplomski.authenticationapp.view_models.AccountViewModel;

public class AccountDetailsFragment extends Fragment {

    private MainActivity mainActivity;
    private AccountViewModel accountViewModel;
    private FragmentAccountDetailsBinding binding;

    public AccountDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) requireActivity();
        accountViewModel = new ViewModelProvider(mainActivity).get(AccountViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountDetailsBinding.inflate(inflater, container, false);

        String accId = AccountDetailsFragmentArgs.fromBundle(getArguments()).getAccountId();
        Account account = accountViewModel.getAccountFromId(accId);

        binding.number.setText(account.getNumber());
        binding.type.setText(account.getCurrency().equals("RSD") ? "Basic" : "Foreign Exchange");
        binding.balance.setText((account.getBalance() % 1 == 0) ? ((int)account.getBalance() + ".00") : (String.format("%.2f", account.getBalance())) + "  " + account.getCurrency());
        binding.status.setText(account.getStatus() ? "ACTIVE" : "NOT ACTIVE");

        return binding.getRoot();
    }
}