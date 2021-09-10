package rs.ac.bg.etf.diplomski.authenticationapp.app_main.payments;

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

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import rs.ac.bg.etf.diplomski.authenticationapp.R;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.MainActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.accounts_info.AccountViewModel;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.transactions.TransactionViewModel;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.FragmentPaymentsBinding;

public class PaymentsFragment extends Fragment {

    private MainActivity mainActivity;
    private AccountViewModel accountViewModel;
    private TransactionViewModel transactionViewModel;
    private FirebaseFirestore firebaseFirestore;

    private FragmentPaymentsBinding binding;

    public PaymentsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) requireActivity();

        accountViewModel = new ViewModelProvider(mainActivity).get(AccountViewModel.class);
        transactionViewModel = new ViewModelProvider(mainActivity).get(TransactionViewModel.class);

        firebaseFirestore = FirebaseFirestore.getInstance();
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

        binding.payerAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if(position == 0) {
//                    binding.payerAccount.setBackground(mainActivity.getDrawable(R.drawable.grey_outline));
//                }
//                else {
//                    binding.payerAccount.setBackground(mainActivity.getDrawable(R.drawable.blue_outline));
//                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return binding.getRoot();
    }
}