package rs.ac.bg.etf.diplomski.authenticationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.budiyev.android.codescanner.AutoFocusMode;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.ScanMode;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import dagger.hilt.android.AndroidEntryPoint;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.ActivityCodeScannerBinding;

@AndroidEntryPoint
public class CodeScannerActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 101;

    private static final String PHONE_VERIFICATION_SIS_KEY = "phone-verification-sis-key";
    private static final String PHONE_VERIFICATION_SIS_NUMBER = "phone-verification-sis-number";

    private ActivityCodeScannerBinding binding;
    private FirebaseAuth firebaseAuth;
    private CodeScanner codeScanner;

    private boolean phone_verification_in_progress = false;
    private String phone_number = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCodeScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        setupCameraPermissions();
        initCodeScanner();
    }

    private void initCodeScanner() {
        codeScanner = new CodeScanner(this, binding.scannerView);

        codeScanner.setCamera(CodeScanner.CAMERA_BACK);
        codeScanner.setFormats(CodeScanner.ALL_FORMATS);
        codeScanner.setAutoFocusMode(AutoFocusMode.SAFE);
        codeScanner.setScanMode(ScanMode.CONTINUOUS);
        codeScanner.setAutoFocusEnabled(true);
        codeScanner.setFlashEnabled(false);

        codeScanner.setDecodeCallback(result -> {
            try {
                String[] scanned_data = result.getText().split("@");

                verifyPhoneNumber(scanned_data[1]);
            }
            catch (Exception e) {
                Toast.makeText(this, "Invalid QR Code. Please try again", Toast.LENGTH_SHORT);
            }
        });

        codeScanner.setErrorCallback(error -> {
            runOnUiThread(() -> {
                Log.e("scanner-error", "Camera initialization error" + error.getMessage());
            });
        });

        binding.scannerView.setOnClickListener(v -> {
            codeScanner.startPreview();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(phone_verification_in_progress && phone_number != "") {
            //verifyPhoneNumber(phone_number);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        codeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        codeScanner.releaseResources();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(PHONE_VERIFICATION_SIS_KEY, phone_verification_in_progress);
        outState.putString(PHONE_VERIFICATION_SIS_NUMBER, phone_number);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        phone_verification_in_progress = savedInstanceState.getBoolean(PHONE_VERIFICATION_SIS_KEY);
        phone_number = savedInstanceState.getString(PHONE_VERIFICATION_SIS_NUMBER, "");
    }

    private void verifyPhoneNumber(String phoneNumber) {
        this.phone_number = phoneNumber;

        PhoneAuthOptions phoneAuthOptions =
                PhoneAuthOptions
                        .newBuilder(firebaseAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                Log.d("verification-tag", "onVerificationCompleted: " + phoneAuthCredential);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Log.e("verification-tag", "onVerificationFailed: " + e.getMessage());
                            }

                            @Override
                            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                super.onCodeSent(verificationId, forceResendingToken);

                            }

                            @Override
                            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                                super.onCodeAutoRetrievalTimeOut(s);
                                Log.d("verification-tag", "onCodeAutoRetrievalTimeOut: " + s);
                            }


                        })
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions);

        phone_verification_in_progress = true;
    }

    private void setupCameraPermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            makePermissionRequest();
        }
    }

    private void makePermissionRequest() {
        ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.CAMERA }, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "You need camera permission to be able to use this app", Toast.LENGTH_SHORT);
            }
        }
    }
}