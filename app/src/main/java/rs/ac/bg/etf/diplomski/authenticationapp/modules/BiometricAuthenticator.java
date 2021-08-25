package rs.ac.bg.etf.diplomski.authenticationapp.modules;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import java.util.concurrent.Executor;

public class BiometricAuthenticator {

    public interface  Callback {
        void failure();
        void success();
    }

    public static final String SHARED_PREFERENCES_ACCOUNT = "shared-preferences-authentication-data";

    public static final String SHARED_PREFERENCES_EMAIL_PARAMETER = "shared-preferences-email-parameter";
    public static final String SHARED_PREFERENCES_BIOMETRY_PARAMETER = "shared-preferences-biometry-parameter";
    public static final String SHARED_PREFERENCES_PIN_CODE_PARAMETER = "shared-preferences-pin-code-parameter";

    public static final String SHARED_PREFERENCES_USER_INFO = "shared-preferences-user-info";

    private Context context;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    public BiometricAuthenticator(Context context, Callback callback) {
        executor = ContextCompat.getMainExecutor(context);

        this.context = context;

        biometricPrompt = new BiometricPrompt((FragmentActivity) context, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                callback.failure();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                callback.success();

                Toast.makeText(context,
                        "Authentication succeeded!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(context, "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Fingerprint scanner")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .setNegativeButtonText("Use PIN code instead")
                .build();
    }

    public void authenticate() {
        biometricPrompt.authenticate(promptInfo);
    }

//    public void encrypt() {
//        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
//
//        //SECRET
//        String message = "This is a secret created by Niky for aditional security of users and their data";
//        String secret = shuffle(message);
//        //SALT
//        String salt = shuffle(secret);
//        //KEY
//        String key = shuffle(data + (new Date()).getTime());
//        //IV
//        Random rd = new Random();
//        byte[] iv = new byte[16];
//        rd.nextBytes(iv);
//
//        Encryption encryption = Encryption.getDefault(key, salt, iv);
//
//        String encrypted = encryption.encryptOrNull(secret);
//
//        Log.d("my-log", encrypted);
//        //Log.d("my-log", decrypted);
//
//        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_BIOMETRY, Context.MODE_PRIVATE);
//        sharedPreferences.edit()
//                .putString(SHARED_PREFERENCES_SECRET, secret)
//                .apply();
//
//        firebaseFirestore
//                .collection("users")
//                .document(data)
//                .update("security_message", encrypted)
//                .addOnSuccessListener((Activity) context, aVoid -> {
//                    Intent intent = new Intent(context, LoginActivity.class);
//                    context.startActivity(intent);
//                    ((Activity) context).finish();
//                })
//                .addOnFailureListener((Activity) context, e -> {
//                    Toast.makeText(context, "Something went wrong. Please try again!", Toast.LENGTH_SHORT).show();
//                });
//    }

//    private String shuffle(String input){
//        List<Character> characters = new ArrayList<Character>();
//        for(char c:input.toCharArray()){
//            characters.add(c);
//        }
//        StringBuilder output = new StringBuilder(input.length());
//        while(characters.size()!=0){
//            int randPicker = (int)(Math.random()*characters.size());
//            output.append(characters.remove(randPicker));
//        }
//
//        return output.toString();
//    }
}
