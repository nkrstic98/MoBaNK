package rs.ac.bg.etf.diplomski.authenticationapp.view_models;

import android.app.AlertDialog;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
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
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import rs.ac.bg.etf.diplomski.authenticationapp.R;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.MainActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.accounts_info.AccountAdapter;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.transactions.TransactionAdapter;
import rs.ac.bg.etf.diplomski.authenticationapp.di.ExecutorServiceModule;
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

    private String uid;

    private AlertDialog dialog;
    private ExecutorService executorService;

    public void setData(String userId, MainActivity mainActivity) {
        this.mainActivity = mainActivity;

        uid = userId;

        firebaseFirestore = FirebaseFirestore.getInstance();

        accountCollection = firebaseFirestore
                .collection("users")
                .document(userId)
                .collection("accounts");

        accounts = new ArrayList<>();
        accountIds = new ArrayList<>();

        executorService = ExecutorServiceModule.provideExecutorService();
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

    public void executeTransaction(String payerAccount, String payerInfo, String recipientAccount, String recipientInfo,
                                   String payer_purpose, String receiver_purpose, double payer_amount, double receiver_amount) {

        executorService.submit(() -> {
            mainActivity.runOnUiThread(() -> {
                dialog = buildDialog();
                dialog.show();
            });

            final String payer = payerAccount + (payerInfo.equals("") ? "" : (" (" + payerInfo + ")"));
            final String recipient = recipientAccount + (recipientInfo.equals("") ? "" : (" (" + recipientInfo + ")"));

            Date transactionTime = new Date();

            Account payerA = getAccount(payerAccount);
            if(payerA.getTransactions() == null) {
                payerA.setTransactions(new ArrayList<>());
            }
            payerA.getTransactions().add(
                    new Transaction(
                            transactionTime,
                            payer_amount,
                            payer,
                            recipient,
                            TRANSACTION_TYPE.OUTFLOW,
                            payer_purpose
                    )
            );

            AtomicReference<Account> receiverA = new AtomicReference<>(getAccount(recipientAccount));

            if(receiverA.get() == null) {
                firebaseFirestore
                        .collection("users")
                        .get()
                        .addOnSuccessListener(mainActivity, queryDocumentSnapshots -> {

                            DocumentSnapshot ds;
                            int i = -1;
                            AtomicBoolean next = new AtomicBoolean(true);
                            AtomicBoolean shouldBreak = new AtomicBoolean(false);

                            while(true) {
                                if(shouldBreak.get()) break;
                                if(!next.get()) {
//                                    try {
//                                        Thread.sleep(1000);
//                                    } catch (InterruptedException e) {
//                                        e.printStackTrace();
//                                    }
                                    continue;
                                }

                                next.set(false);
                                i++;

                                if(i == queryDocumentSnapshots.getDocuments().size()) break;

                                ds = queryDocumentSnapshots.getDocuments().get(i);

                                DocumentSnapshot finalDs = ds;
                                firebaseFirestore
                                        .collection("users")
                                        .document(ds.getId())
                                        .collection("accounts")
                                        .whereEqualTo("number", recipientAccount)
                                        .get()
                                        .addOnSuccessListener(executorService, snapshots -> {
                                            if(snapshots.getDocuments().size() == 1) {
                                                shouldBreak.set(true);

                                                receiverA.set(snapshots.getDocuments().get(0).toObject(Account.class));

                                                if(receiverA.get().getTransactions() == null) {
                                                    receiverA.get().setTransactions(new ArrayList<>());
                                                }
                                                receiverA.get().getTransactions().add(
                                                        new Transaction(
                                                                transactionTime,
                                                                receiver_amount,
                                                                payer,
                                                                recipient,
                                                                TRANSACTION_TYPE.INFLOW,
                                                                receiver_purpose
                                                        )
                                                );

                                                executeInternalTransaction(finalDs.getId(), snapshots.getDocuments().get(0).getId(),
                                                        payerA, receiverA.get(), payer_amount, receiver_amount);
                                            }

                                            next.set(true);
                                        })
                                        .addOnFailureListener(executorService, e -> {
                                            Log.e("error-log", e.getMessage());
                                            next.set(true);
                                        });

//                                try {
//                                    Thread.sleep(1000);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
                            }

                            if(!shouldBreak.get()) {
                                executeExternalTransaction(
                                        payerA,
                                        payer_amount,
                                        new Transaction(
                                                transactionTime,
                                                receiver_amount,
                                                payer,
                                                recipient,
                                                TRANSACTION_TYPE.INFLOW,
                                                receiver_purpose
                                        )
                                );
                            }
                        })
                        .addOnFailureListener(mainActivity, e -> {
                            mainActivity.runOnUiThread(() -> {
                                Toast.makeText(mainActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        });
            }
            else {
                if(receiverA.get().getTransactions() == null) {
                    receiverA.get().setTransactions(new ArrayList<>());
                }
                receiverA.get().getTransactions().add(
                        new Transaction(
                                transactionTime,
                                receiver_amount,
                                payer,
                                recipient,
                                TRANSACTION_TYPE.INFLOW,
                                receiver_purpose
                        )
                );

                executeInternalTransaction(uid, "", payerA, receiverA.get(), payer_amount, receiver_amount);
            }
        });
    }

    private void executeInternalTransaction(String uid2, String accId, Account payerA, Account receiverA, double payer_amount, double receiver_amount) {
        WriteBatch batch = firebaseFirestore.batch();

        DocumentReference payerRef = accountCollection.document(getId(payerA.getNumber()));
        batch.update(payerRef, "balance", payerA.getBalance() - payer_amount, "transactions", payerA.getTransactions());

        DocumentReference recRef;
        if(uid.equals(uid2)) {
            recRef = accountCollection.document(getId(receiverA.getNumber()));
        }
        else {
            recRef = firebaseFirestore.collection("users").document(uid2).collection("accounts").document(accId);
        }
        batch.update(recRef, "balance", receiverA.getBalance() + receiver_amount, "transactions", receiverA.getTransactions());

        batch.commit()
                .addOnSuccessListener(command -> {
                    mainActivity.runOnUiThread(() -> {
                        Toast.makeText(mainActivity, "Transaction executed successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                })
                .addOnFailureListener(e -> {
                    mainActivity.runOnUiThread(() -> {
                        Toast.makeText(mainActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                });
    }

    private void executeExternalTransaction(Account payerA, double payer_amount, Transaction t) {
        WriteBatch batch = firebaseFirestore.batch();

        DocumentReference payerRef = accountCollection.document(getId(payerA.getNumber()));
        batch.update(payerRef, "balance", payerA.getBalance() - payer_amount, "transactions", payerA.getTransactions());

        DocumentReference transRef = firebaseFirestore.collection("external_transactions").document();
        batch.set(transRef, t);

        batch.commit()
                .addOnSuccessListener(command -> {
                    mainActivity.runOnUiThread(() -> {
                        Toast.makeText(mainActivity, "Payment executed successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                })
                .addOnFailureListener(e -> {
                    mainActivity.runOnUiThread(() -> {
                        Toast.makeText(mainActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                });
    }

    private AlertDialog buildDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        LayoutInflater inflater = mainActivity.getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_loading, null));

        return builder.create();
    }
}