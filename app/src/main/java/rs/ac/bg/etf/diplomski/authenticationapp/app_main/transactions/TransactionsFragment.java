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
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.FragmentTransactionsBinding;
import rs.ac.bg.etf.diplomski.authenticationapp.modules.BiometricAuthenticator;

public class TransactionsFragment extends Fragment {

    private FragmentTransactionsBinding binding;
    private MainActivity mainActivity;
    private TransactionViewModel transactionViewModel;
    private SharedPreferences sharedPreferences;

    public TransactionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) requireActivity();

        sharedPreferences = mainActivity.getSharedPreferences(BiometricAuthenticator.SHARED_PREFERENCES_ACCOUNT, Context.MODE_PRIVATE);

        transactionViewModel = new ViewModelProvider(mainActivity).get(TransactionViewModel.class);
        transactionViewModel.setData(
                sharedPreferences.getString(BiometricAuthenticator.SHARED_PREFERENCES_USER_ID, ""),
                TransactionsFragmentArgs.fromBundle(getArguments()).getAccountId()
                );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentTransactionsBinding.inflate(inflater, container, false);

        TransactionAdapter adapter = new TransactionAdapter(
                TransactionsFragmentArgs.fromBundle(getArguments()).getCurrency(),
                mainActivity.getResources()
        );

        transactionViewModel.subscribeToRealtimeUpdates(adapter);

        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(mainActivity));

        return binding.getRoot();
    }
}