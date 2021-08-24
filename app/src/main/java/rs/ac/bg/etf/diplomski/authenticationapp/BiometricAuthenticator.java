package rs.ac.bg.etf.diplomski.authenticationapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.Executor;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import rs.ac.bg.etf.diplomski.authenticationapp.account_setup.RegisterActivity;

public class BiometricAuthenticator {

    public interface  Callback {
        void invoke();
        void encrypt();
    }

    public static final String SHARED_PREFERENCES_BIOMETRY = "shared-preferences-authentication-data";

    public static final String SHARED_PREFERENCES_BIOMETRY_PARAMETER = "shared-preferences-biometry-parameter";
    public static final String SHARED_PREFERENCES_PIN_CODE_PARAMETER = "shared-preferences-pin-code-parameter";

    public static final String SHARED_PREFERENCES_KEY = "shared-preferences-key-parameter";
    public static final String SHARED_PREFERENCES_IV = "shared-preferences-iv";
    public static final String SHARED_PREFERENCES_SALT = "shared-preferences-secret-key";

    protected static final String KEY_NAME = "android-device-info-key";

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
                callback.invoke();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                callback.encrypt();

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

    public void encrypt(String data) {

    }
}
