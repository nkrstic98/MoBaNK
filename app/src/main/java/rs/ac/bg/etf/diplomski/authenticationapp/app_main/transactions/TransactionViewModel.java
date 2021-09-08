package rs.ac.bg.etf.diplomski.authenticationapp.app_main.transactions;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import rs.ac.bg.etf.diplomski.authenticationapp.models.Transaction;

public class TransactionViewModel extends ViewModel {

    private List<Transaction> transactionList;

    private FirebaseFirestore firebaseFirestore;
    private DocumentReference accountReference;
    private CollectionReference transactionsCollection;

    private TransactionAdapter transactionAdapter;

    public void setData(String userId, String accountId) {

        firebaseFirestore = FirebaseFirestore.getInstance();

        transactionsCollection = firebaseFirestore
                .collection(
                        "users/" + userId + "/accounts/" + accountId + "/transactions"
                );

        accountReference = firebaseFirestore
                .collection("users/" + userId + "/accounts/")
                .document(accountId);
    }

    public void subscribeToRealtimeUpdates(TransactionAdapter transactionAdapter) {
        this.transactionAdapter = transactionAdapter;

        transactionsCollection
                .orderBy("date")
                .addSnapshotListener((value, error) -> {
                    if(error != null) {
                        Log.e("error", error.getMessage());
                        return;
                    }

                    if(value != null) {
                        transactionList = new ArrayList<>();

                        for(DocumentSnapshot documentSnapshot : value) {
                            Transaction t = documentSnapshot.toObject(Transaction.class);
                            transactionList.add(t);
                        }

                        this.transactionAdapter.setTransactions(transactionList);
                    }
                });
    }
}
