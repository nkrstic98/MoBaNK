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
    public static final String SHARED_PREFERENCES_USER_ID = "shared-preferences-user-id";

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    public BiometricAuthenticator(Context context, Callback callback) {
        executor = ContextCompat.getMainExecutor(context);

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

//                Toast.makeText(context,
//                        "Authentication succeeded!", Toast.LENGTH_SHORT).show();
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
}
