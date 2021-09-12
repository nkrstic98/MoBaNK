package rs.ac.bg.etf.diplomski.authenticationapp.app_main.transactions;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import rs.ac.bg.etf.diplomski.authenticationapp.R;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.ViewHolderTransactionBinding;
import rs.ac.bg.etf.diplomski.authenticationapp.models.TRANSACTION_TYPE;
import rs.ac.bg.etf.diplomski.authenticationapp.models.Transaction;
import rs.ac.bg.etf.diplomski.authenticationapp.modules.DateTimeUtil;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private final String currency;
    private Resources resources;

    private List<Transaction> transactions;

    public TransactionAdapter(String currency, Resources resources) {
        this.currency = currency;
        this.resources = resources;

        transactions = new ArrayList<>();
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewHolderTransactionBinding binding = ViewHolderTransactionBinding.inflate(inflater, parent, false);
        return new TransactionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        holder.bind(transactions.get(position));
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }


    public class TransactionViewHolder extends RecyclerView.ViewHolder {
        private ViewHolderTransactionBinding binding;

        public TransactionViewHolder(ViewHolderTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Transaction t) {
            binding.date.setText(DateTimeUtil.getSimpleDateTimeFormat().format(t.getDate()));
            binding.currency.setText(currency);
            if(t.getType() == TRANSACTION_TYPE.INFLOW) {
                binding.amount.setText("+ " + ((t.getAmount() % 1 == 0) ? ((int)t.getAmount() + ".00") : t.getAmount()));
                binding.executor.setText("Payer: " + t.getPayer());
                binding.card.setCardBackgroundColor(resources.getColor(R.color.color_inflow));
            }
            else {
                binding.amount.setText("- " + ((t.getAmount() % 1 == 0) ? ((int)t.getAmount() + ".00") : t.getAmount()));
                binding.executor.setText("Recipient: " + t.getRecipient());
                binding.card.setCardBackgroundColor(resources.getColor(R.color.color_outflow));
            }
        }
    }
}
