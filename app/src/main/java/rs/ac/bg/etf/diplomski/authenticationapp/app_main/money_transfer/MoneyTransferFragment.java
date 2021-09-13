package rs.ac.bg.etf.diplomski.authenticationapp.app_main.money_transfer;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.List;

import rs.ac.bg.etf.diplomski.authenticationapp.R;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.MainActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.accounts_info.AccountViewModel;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.FragmentMoneyTransferBinding;
import rs.ac.bg.etf.diplomski.authenticationapp.models.OPERATION;
import rs.ac.bg.etf.diplomski.authenticationapp.modules.BiometricAuthenticator;

import static rs.ac.bg.etf.diplomski.authenticationapp.modules.NumberOperations.fetchNumber;

public class MoneyTransferFragment extends Fragment {

    public static final String SHARED_PREFERENCES_TRANSFER = "shared-preferences-transfer";
    public static final String TRANSFER_RECEIVER = "transfer-receiver";
    public static final String TRANSFER_PAYER = "transfer-payer";
    public static final String TRANSFER_AMOUNT = "transfer-amount";

    private MainActivity mainActivity;
    private AccountViewModel accountViewModel;
    private NavController navController;

    private FragmentMoneyTransferBinding binding;

//    private boolean first_dash = false, second_dash = false;

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

        binding.firstAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                List<String> list = accountViewModel.getAccounts("RSD");
                list.add("Enter another account...");
                list.removeIf(s -> s.equals(binding.firstAccount.getSelectedItem()));
                ArrayAdapter<String> to = new ArrayAdapter<>(
                        mainActivity,
                        android.R.layout.simple_list_item_1,
                        list
                );
                binding.secondAccount.setAdapter(to);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.secondAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == binding.secondAccount.getAdapter().getCount() - 1) {
                    binding.freeAccountLayout.setVisibility(View.VISIBLE);
                }
                else {
                    binding.freeAccountLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

//        binding.freeEnterAccount.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
////                if(s.length() < 3) {
////                    first_dash = false;
////                }
////
////                if(s.length() < 16) {
////                    second_dash = false;
////                }
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if(s.length() < 3) {
//                    first_dash = false;
//                }
//
//                if(s.length() < 16) {
//                    second_dash = false;
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if(s.toString().length() == 3 && !first_dash) {
//                    binding.freeEnterAccount.setText(s.toString() + "-");
//                    binding.freeEnterAccount.setSelection(s.length() + 1);
//                    first_dash = true;
//                }
//
//                if(s.toString().length() == 16 && !second_dash) {
//                    binding.freeEnterAccount.setText(s.toString() + "-");
//                    binding.freeEnterAccount.setSelection(s.length() + 1);
//                    second_dash = true;
//                }
//            }
//        });

        binding.buttonTransfer.setOnClickListener(v -> {
            startTransaction();
        });

        return binding.getRoot();
    }

    private void startTransaction() {
        if(binding.secondAccount.getSelectedItem().toString().equals("Enter another account...") &&
                (binding.recipientAccount1.getText().toString().length() != 3
                        || binding.recipientAccount2.getText().toString().length() != 13
                        || binding.recipientAccount3.getText().toString().length() != 2
                )) {
            Toast.makeText(mainActivity, "Account number format is wrong!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(binding.amount.getText().toString().equals("")) {
            Toast.makeText(mainActivity, "Please, enter amount to proceed with transaction!", Toast.LENGTH_SHORT).show();
            binding.amountLabel.getEditText().requestFocus();
            return;
        }

        if(binding.secondAccount.getSelectedItem().equals("Enter another account...")) {
            if(binding.firstAccount.getSelectedItem().equals(getFreeEnterAccount())) {
                Toast.makeText(mainActivity, "Invalid transaction parameters!", Toast.LENGTH_SHORT).show();
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder
                .setTitle("Execute transaction?")
                .setMessage("Are you sure you want to proceed?")
                .setIcon(R.drawable.outline_swap_horiz_24)
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
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void failure() {
                        SharedPreferences sp = mainActivity.getSharedPreferences(SHARED_PREFERENCES_TRANSFER, Context.MODE_PRIVATE);
                        sp.edit()
                                .putString(TRANSFER_PAYER, binding.firstAccount.getSelectedItem().toString())
                                .putString(
                                        TRANSFER_RECEIVER,
                                        binding.secondAccount.getSelectedItem().equals("Enter another account...")
                                                ?
                                                getFreeEnterAccount()
                                                :
//                                                binding.freeEnterAccount.getText().toString()
                                                binding.secondAccount.getSelectedItem().toString()

                                )
                                .putFloat(TRANSFER_AMOUNT, fetchNumber(binding.amountLabel).floatValue())
                                .apply();

                        dialog.dismiss();
                        navController.navigate(MoneyTransferFragmentDirections.actionGlobalKeyboardFragmentMain(OPERATION.INTERNAL_TRANSFER, ""));

//                        setupInitialState();
                    }

                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void success() {
                        dialog.dismiss();
                        executeTransaction();

//                        setupInitialState();
                    }
                }).authenticate();
            });
        });

        dialog.show();
    }

    private void executeTransaction() {
        String payer = binding.firstAccount.getSelectedItem().toString();
        String receiver = "";
        if(!binding.secondAccount.getSelectedItem().toString().equals("Enter another account...")) {
            receiver = binding.secondAccount.getSelectedItem().toString();
        }
        else {
//            receiver = binding.freeEnterAccount.getText().toString();
            receiver = getFreeEnterAccount();
        }
        double amount = fetchNumber(binding.amountLabel).doubleValue();

        if(!accountViewModel.hasEnoughFunds(payer, amount)) {
            Toast.makeText(mainActivity, "There is not enough funds on the payer account!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!payer.substring(17, 19).equals(receiver.substring(17, 19))) {
            Toast.makeText(mainActivity, "Chosen account are not of same type! Enter valid accounts.", Toast.LENGTH_SHORT).show();
            return;
        }

        accountViewModel.executeTransaction(
                payer,
                "",
                receiver,
                "",
                "Internal transfer: " + receiver,
                "Internal transfer: " + payer,
                amount,
                amount
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setupInitialState() {
        binding.amount.setText("");
        binding.amountLabel.clearFocus();

//        binding.freeEnterAccountLabel.setVisibility(View.GONE);
        binding.freeAccountLayout.setVisibility(View.GONE);

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

    private String getFreeEnterAccount() {
        StringBuilder sb = new StringBuilder();
        sb
                .append(binding.recipientAccount1.getText().toString())
                .append("-")
                .append(binding.recipientAccount2.getText().toString())
                .append("-")
                .append(binding.recipientAccount3.getText().toString());

        return sb.toString();
    }
}