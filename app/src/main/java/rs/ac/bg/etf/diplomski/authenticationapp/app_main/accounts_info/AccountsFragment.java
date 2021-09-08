package rs.ac.bg.etf.diplomski.authenticationapp.app_main.accounts_info;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import rs.ac.bg.etf.diplomski.authenticationapp.app_main.MainActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.FragmentAccountsBinding;
import rs.ac.bg.etf.diplomski.authenticationapp.modules.BiometricAuthenticator;

public class AccountsFragment extends Fragment {

    private FragmentAccountsBinding binding;
    private MainActivity mainActivity;
    private AccountViewModel accountViewModel;
    private SharedPreferences sharedPreferences;

    private NavController navController;

    public AccountsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) requireActivity();

        sharedPreferences = mainActivity.getSharedPreferences(BiometricAuthenticator.SHARED_PREFERENCES_ACCOUNT, Context.MODE_PRIVATE);

        accountViewModel = new ViewModelProvider(mainActivity).get(AccountViewModel.class);
        accountViewModel.setData(sharedPreferences.getString(BiometricAuthenticator.SHARED_PREFERENCES_USER_ID, ""));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountsBinding.inflate(inflater, container, false);

        AccountAdapter accountAdapter = new AccountAdapter(parameter -> {
            navController.navigate(AccountsFragmentDirections.actionNavAccountsToTransactionsFragment(accountViewModel.getAccountId(parameter)));
        }, parameter -> {
            navController.navigate(AccountsFragmentDirections.actionNavAccountsToAccountDetailsFragment(accountViewModel.getAccountId(parameter)));
        });

        accountViewModel.subscribeToRealtimeUpdates(accountAdapter);

        binding.recyclerView.setAdapter(accountAdapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(mainActivity));

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
    }
}