package rs.ac.bg.etf.diplomski.authenticationapp.app_main.accounts_info;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import rs.ac.bg.etf.diplomski.authenticationapp.models.Account;

public class AccountViewModel extends ViewModel {

    private List<Account> accounts;
    private List<String> accountIds;

    private FirebaseFirestore firebaseFirestore;
    private CollectionReference accountCollection;

    private AccountAdapter accountAdapter;

    public void setData(String userId) {

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

    public String getAccountId(int index) {
        return accountIds.get(index);
    }
}
