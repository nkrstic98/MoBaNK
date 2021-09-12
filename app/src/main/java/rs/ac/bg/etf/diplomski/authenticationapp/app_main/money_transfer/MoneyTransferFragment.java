package rs.ac.bg.etf.diplomski.authenticationapp.app_main.money_transfer;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.List;

import rs.ac.bg.etf.diplomski.authenticationapp.app_main.MainActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.accounts_info.AccountViewModel;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.FragmentMoneyTransferBinding;

public class MoneyTransferFragment extends Fragment {

    private MainActivity mainActivity;
    private AccountViewModel accountViewModel;

    private FragmentMoneyTransferBinding binding;

    public MoneyTransferFragment() {
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
        binding = FragmentMoneyTransferBinding.inflate(inflater, container, false);

        setupInitialState();

        binding.secondAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == binding.secondAccount.getAdapter().getCount() - 1) {
                    binding.freeEnterAccountLabel.setVisibility(View.VISIBLE);
                }
                else {
                    binding.freeEnterAccountLabel.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setupInitialState() {

        binding.freeEnterAccountLabel.setVisibility(View.GONE);

        ArrayAdapter<String> from = new ArrayAdapter<>(
                mainActivity,
                android.R.layout.simple_list_item_1,
                accountViewModel.getAccounts("RSD")
        );
        binding.firstAccount.setAdapter(from);

        List<String> list = accountViewModel.getAccounts("RSD");
        list.add("Enter another account...");
        ArrayAdapter<String> to = new ArrayAdapter<>(
                mainActivity,
                android.R.layout.simple_list_item_1,
                list
        );
        binding.secondAccount.setAdapter(to);
    }
}