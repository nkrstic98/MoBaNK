package rs.ac.bg.etf.diplomski.authenticationapp.app_main.user_management;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import rs.ac.bg.etf.diplomski.authenticationapp.app_main.MainActivity;
import rs.ac.bg.etf.diplomski.authenticationapp.app_main.user_management.AccountSettingsFragment;
import rs.ac.bg.etf.diplomski.authenticationapp.models.OPERATION;
import rs.ac.bg.etf.diplomski.authenticationapp.models.User;
import rs.ac.bg.etf.diplomski.authenticationapp.modules.BiometricAuthenticator;

public class UserViewModel extends ViewModel {
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;
    private DocumentReference userReference;

    private MainActivity mainActivity;
    private SharedPreferences sharedPreferences;

    private MutableLiveData<User> user = new MutableLiveData<>(null);

    public void setData(MainActivity mainActivity, AccountSettingsFragment.OperationCallback callback) {
        sharedPreferences = mainActivity.getSharedPreferences(BiometricAuthenticator.SHARED_PREFERENCES_ACCOUNT, Context.MODE_PRIVATE);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore
                .collection("users")
                .whereEqualTo("email", firebaseUser.getEmail())
                .get()
                .addOnSuccessListener(command -> {
                    List<DocumentSnapshot> docs = command.getDocuments();
                    if(docs.size() > 0) {
                        User u = docs.get(0).toObject(User.class);
                        this.user.setValue(u);

                        userReference = firebaseFirestore
                                .collection("users")
                                .document(docs.get(0).getId());

                        callback.invoke(OPERATION.DO_NOTHING, "", null);
                    }
                })
                .addOnFailureListener(command -> {
                    Log.e("error-msg", command.getMessage());
                });

        this.mainActivity = mainActivity;
    }

    public String getDisplayName() {
        return firebaseUser.getDisplayName();
    }

    public String getEmail() {
        return firebaseUser.getEmail();
    }

    public String getPhone() {
        return user.getValue().getPhone();
    }

    public void updateEmail(String data, AccountSettingsFragment.OperationCallback callback) {
        firebaseUser
                .updateEmail(data)
                .addOnSuccessListener(mainActivity, aVoid -> {
                    userReference
                            .update("email", data)
                            .addOnSuccessListener(mainActivity, aVoid1 -> {
                                sharedPreferences
                                        .edit()
                                        .putString(BiometricAuthenticator.SHARED_PREFERENCES_EMAIL_PARAMETER, data)
                                        .apply();

                                showMessage("Email successfully changed!");

                                callback.invoke(OPERATION.DO_NOTHING, null, null);
                            })
                            .addOnFailureListener(mainActivity, e -> {
                                showMessage(e.getMessage());
                            });
                })
                .addOnFailureListener(mainActivity, e -> {
                    Toast.makeText(mainActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void changePassword(String pass) {
        firebaseUser
                .updatePassword(pass)
                .addOnSuccessListener(mainActivity, aVoid -> {
                    showMessage("Password successfully updated!");
                })
                .addOnFailureListener(mainActivity, e -> {
                    showMessage(e.getMessage());
                });
    }

    public void deleteUser(AccountSettingsFragment.OperationCallback callback) {
        firebaseUser.delete()
                .addOnSuccessListener(mainActivity, aVoid -> {
                    userReference
                            .update("email", null)
                            .addOnSuccessListener(mainActivity, aVoid1 -> {
                                showMessage("Account deleted!!!");
                                callback.invoke(OPERATION.DO_NOTHING, null, null);
                            })
                            .addOnFailureListener(mainActivity, e -> {
                                showMessage(e.getMessage());
                            });
                })
                .addOnFailureListener(mainActivity, e -> {
                    showMessage(e.getMessage());
                });
    }

    private void showMessage(String msg) {
        mainActivity.runOnUiThread(() -> {
            Toast.makeText(mainActivity, msg, Toast.LENGTH_SHORT).show();
        });
    }
}
