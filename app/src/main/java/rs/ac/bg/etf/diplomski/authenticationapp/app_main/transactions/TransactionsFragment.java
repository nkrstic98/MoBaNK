package rs.ac.bg.etf.diplomski.authenticationapp.app_main.transactions;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import rs.ac.bg.etf.diplomski.authenticationapp.app_main.MainActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.accounts_info.AccountViewModel;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.FragmentTransactionsBinding;
import rs.ac.bg.etf.diplomski.authenticationapp.modules.BiometricAuthenticator;

public class TransactionsFragment extends Fragment {

    private FragmentTransactionsBinding binding;
    private MainActivity mainActivity;
    private AccountViewModel accountViewModel;

    public TransactionsFragment() {
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

        binding = FragmentTransactionsBinding.inflate(inflater, container, false);

        TransactionAdapter adapter = new TransactionAdapter(
                TransactionsFragmentArgs.fromBundle(getArguments()).getCurrency(),
                mainActivity.getResources()
        );

        accountViewModel.getTransactions(adapter, TransactionsFragmentArgs.fromBundle(getArguments()).getAccountId());

        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(mainActivity));

        return binding.getRoot();
    }
}