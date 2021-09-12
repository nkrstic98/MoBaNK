package rs.ac.bg.etf.diplomski.authenticationapp.app_main.accounts_info;

import android.os.Build;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rs.ac.bg.etf.diplomski.authenticationapp.app_main.MainActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.transactions.TransactionAdapter;
import rs.ac.bg.etf.diplomski.authenticationapp.models.Account;
import rs.ac.bg.etf.diplomski.authenticationapp.models.TRANSACTION_TYPE;
import rs.ac.bg.etf.diplomski.authenticationapp.models.Transaction;

public class AccountViewModel extends ViewModel {

    private List<Account> accounts;
    private List<String> accountIds;

    private FirebaseFirestore firebaseFirestore;
    private CollectionReference accountCollection;

    private AccountAdapter accountAdapter;

    private MainActivity mainActivity;

    public void setData(String userId, MainActivity mainActivity) {
        this.mainActivity = mainActivity;

        firebaseFirestore = FirebaseFirestore.getInstance();

        accountCollection = firebaseFirestore
                .collection("users")
                .document(userId)
                .collection("accounts");

        accounts = new ArrayList<>();
        accountIds = new ArrayList<>();
    }

    public void subscribeToRealtimeUpdates(AccountAdapter accountAdapter) {

        this.accountAdapter = accountAdapter;

        accountCollection
                .orderBy("number", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if(error != null) {
                        Log.e("error", error.getMessage());
                        return;
                    }

                    if(value != null) {
                        accounts = new ArrayList<>();
                        accountIds = new ArrayList<>();
                        for(DocumentSnapshot documentSnapshot : value) {
                            Account account = documentSnapshot.toObject(Account.class);
                            accounts.add(account);
                            accountIds.add(documentSnapshot.getId());
                        }

                        this.accountAdapter.setAccountList(accounts);
                    }
                });
    }

    public void getTransactions(TransactionAdapter transactionAdapter, String accountId) {
        accountCollection
                .orderBy("number", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if(error != null) {
                        Log.e("error", error.getMessage());
                        return;
                    }

                    if(value != null) {
                        for(DocumentSnapshot documentSnapshot : value) {
                            Account account = documentSnapshot.toObject(Account.class);
                            if(documentSnapshot.getId().equals(accountId)) {
                                transactionAdapter.setTransactions(account.getTransactions());
                            }
                        }
                    }
                });
    }

    public Account getAccount(String id) {
        for (Account a :
                accounts) {
            if(a.getNumber().equals(id)) return a;
        }

        return null;
    }

    public String getAccountId(int index) {
        return accountIds.get(index);
    }

    public String getId(String number) {
        for(int i = 0; i < accounts.size(); i++) {
            if(accounts.get(i).getNumber().equals(number)) {
                return getAccountId(i);
            }
        }

        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public List<String> getAccounts(String currency) {
        List<String> list = new ArrayList<>();
        accounts.forEach(account -> {
            if(account.getCurrency().equals(currency) && account.getStatus()) {
                list.add(account.getNumber());
            }
        });

        return list;
    }

    public boolean hasEnoughFunds(String account, double amount) {
        for (Account acc :
                accounts) {
            if (acc.getNumber().equals(account)) {
                return acc.getBalance() >= amount;
            }
        }

        return false;
    }

    public void executeInternalTransaction(String payer, String receiver, double payer_amount, double receiver_amount) {
        Date transactionTime = new Date();

        Account payerA = getAccount(payer);
        payerA.getTransactions().add(
                new Transaction(
                        transactionTime,
                        payer_amount,
                        payer,
                        receiver,
                        TRANSACTION_TYPE.OUTFLOW
                )
        );

        Account receiverA = getAccount(receiver);
        receiverA.getTransactions().add(
                new Transaction(
                        transactionTime,
                        receiver_amount,
                        payer,
                        receiver,
                        TRANSACTION_TYPE.INFLOW
                )
        );

        WriteBatch batch = firebaseFirestore.batch();

        DocumentReference payerRef = accountCollection.document(getId(payer));
        batch.update(payerRef, "balance", payerA.getBalance() - payer_amount, "transactions", payerA.getTransactions());

        DocumentReference recRef = accountCollection.document(getId(receiver));
        batch.update(recRef, "balance", receiverA.getBalance() + receiver_amount, "transactions", receiverA.getTransactions());

        batch.commit()
                .addOnSuccessListener(command -> {
                    Toast.makeText(mainActivity, "Transaction executed successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(mainActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}