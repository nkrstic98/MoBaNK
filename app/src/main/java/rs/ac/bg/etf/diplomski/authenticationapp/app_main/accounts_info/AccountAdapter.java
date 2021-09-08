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

    public interface Callback<T> {
        void invoke(T parameter);
    }

    List<Account> accountList;
    private final Callback<Integer> callbackTransactions;
    private final Callback<Integer> callbackDetails;

    public AccountAdapter(Callback<Integer> callbackTransactions, Callback<Integer> callbackDetails) {
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
            binding.accountBalance.setText(account.getBalance() + "");
            binding.accountType.setText(account.getType());
            binding.buttonTransactions.setOnClickListener(v -> {
                callbackTransactions.invoke(getAdapterPosition());
            });
            binding.buttonDetails.setOnClickListener(v -> {
                callbackDetails.invoke(getAdapterPosition());
            });
        }
    }
}
