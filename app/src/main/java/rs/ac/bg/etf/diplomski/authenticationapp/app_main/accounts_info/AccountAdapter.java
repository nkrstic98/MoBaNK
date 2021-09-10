package rs.ac.bg.etf.diplomski.authenticationapp.app_main.accounts_info;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import rs.ac.bg.etf.diplomski.authenticationapp.databinding.ViewHolderAccountBinding;
import rs.ac.bg.etf.diplomski.authenticationapp.models.Account;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {

    public interface Callback<T, B> {
        void invoke(T param1, B param2);
    }

    List<Account> accountList;
    private final Callback<Integer, String> callbackTransactions;
    private final Callback<Integer, Integer> callbackDetails;

    public AccountAdapter(Callback<Integer, String> callbackTransactions, Callback<Integer, Integer> callbackDetails) {
        this.callbackTransactions = callbackTransactions;
        this.callbackDetails = callbackDetails;

        accountList = new ArrayList<>();
    }

    public void setAccountList(List<Account> list) {
        accountList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewHolderAccountBinding binding = ViewHolderAccountBinding.inflate(inflater, parent, false);
        return new AccountViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        holder.bind(accountList.get(position));
    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }

    public class AccountViewHolder extends RecyclerView.ViewHolder {

        private ViewHolderAccountBinding binding;

        public AccountViewHolder(ViewHolderAccountBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Account account) {
            binding.accountNumber.setText(account.getNumber());
            binding.accountBalance.setText(((account.getBalance() % 1 == 0) ? ((int)account.getBalance() + ".00") : (account.getBalance() + "")));
            binding.accountType.setText(account.getCurrency());
            binding.buttonTransactions.setOnClickListener(v -> {
                callbackTransactions.invoke(getAdapterPosition(), account.getCurrency());
            });
            binding.buttonDetails.setOnClickListener(v -> {
                callbackDetails.invoke(getAdapterPosition(), null);
            });
        }
    }
}
