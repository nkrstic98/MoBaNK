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
        void encrypt(Cipher cipher);
    }

    public static final String SHARED_PREFERENCES_BIOMETRY = "shared-preferences-authentication-data";
    public static final String SHARED_PREFERENCES_BIOMETRY_PARAMETER = "shared-preferences-biometry-parameter";
    public static final String SHARED_PREFERENCES_PIN_CODE_PARAMETER = "shared-preferences-pin-code-parameter";
    public static final String SHARED_PREFERENCES_KEY_PARAMETER = "shared-preferences-key-parameter";
    public static final String SHARED_PREFERENCES_IV = "shared-preferences-iv";
    public static final String SHARED_PREFERENCES_SECRET_KEY = "shared-preferences-secret-key";

    protected static final String KEY_NAME = "android-device-info-key";

    private Context context;
    private NavController navController;

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

                if(context instanceof RegisterActivity) {
                    callback.invoke();
                }
                else {

                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                if(context instanceof RegisterActivity) {
                    callback.encrypt(result.getCryptoObject().getCipher());
                }
                else {

                }

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

    public void registerNavController(NavController navController) {
        this.navController = navController;
    }

    public void authenticate(Cipher cipher) {
        biometricPrompt.authenticate(promptInfo, new BiometricPrompt.CryptoObject(cipher));
    }

    public void encrypt(Cipher cipher, String data) {
        try {
            Date date = new Date();
            long time = date.getTime();

            SharedPreferences sharedPreferences = context.getSharedPreferences(BiometricAuthenticator.SHARED_PREFERENCES_BIOMETRY, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(BiometricAuthenticator.SHARED_PREFERENCES_KEY_PARAMETER, time);
            editor.commit();

            String key = data + "_" + time;

            byte[] crypto = cipher.doFinal(
                    key.getBytes(Charset.defaultCharset())
            );

            IvParameterSpec ivParams = cipher.getParameters().getParameterSpec(IvParameterSpec.class);
            byte[] iv = ivParams.getIV();
            editor.putString(SHARED_PREFERENCES_IV, Arrays.toString(iv));
            editor.commit();

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore
                    .collection("users")
                    .document(data)
                    .update("secret_key", Arrays.toString(crypto))
                    .addOnSuccessListener((FragmentActivity) context, aVoid1 -> {
                        Toast.makeText(context, "Authentication succeeded!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(context, LoginActivity.class);
                        intent.putExtra(LoginActivity.SECRET_KEY_PARAMETER, time);
                        context.startActivity(intent);
                        ((FragmentActivity) context).finish();
                    })
                    .addOnFailureListener((FragmentActivity) context, e -> {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidParameterSpecException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void generateSecretKey(boolean require_authentication) {
        try {
            KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                        BiometricAuthenticator.KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
                    )
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .setUserAuthenticationRequired(require_authentication)
                    .build();

            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keyGenerator.init(keyGenParameterSpec);
            keyGenerator.generateKey();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    public SecretKey getSecretKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            return ((SecretKey) keyStore.getKey(KEY_NAME, null));

        } catch (KeyStoreException e) {
            e.printStackTrace();
            return null;
        } catch (CertificateException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cipher getCypher() {
        try {
            return Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7
            );
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
