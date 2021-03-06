package rs.ac.bg.etf.diplomski.authenticationapp.app_user_register;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.budiyev.android.codescanner.AutoFocusMode;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.ScanMode;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

import rs.ac.bg.etf.diplomski.authenticationapp.app_user_register.CodeScannerFragmentDirections;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.FragmentCodeScannerBinding;

public class CodeScannerFragment extends Fragment {

    private static final int CAMERA_REQUEST_CODE = 101;

    private FragmentCodeScannerBinding binding;
    private RegisterActivity registerActivity;
    private NavController navController;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private CodeScanner codeScanner;

    public CodeScannerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerActivity = (RegisterActivity) requireActivity();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentCodeScannerBinding.inflate(inflater, container, false);

        setupCameraPermissions();
        initCodeScanner();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
    }

    @Override
    public void onPause() {
        super.onPause();
        codeScanner.releaseResources();
    }

    @Override
    public void onResume() {
        super.onResume();
        codeScanner.startPreview();
    }

    private void initCodeScanner() {
        codeScanner = new CodeScanner(registerActivity, binding.scannerView);

        codeScanner.setCamera(CodeScanner.CAMERA_BACK);
        codeScanner.setFormats(CodeScanner.ALL_FORMATS);
        codeScanner.setAutoFocusMode(AutoFocusMode.SAFE);
        codeScanner.setScanMode(ScanMode.SINGLE);
        codeScanner.setAutoFocusEnabled(true);
        codeScanner.setFlashEnabled(false);

        codeScanner.setDecodeCallback(result -> {
            try {
                String[] scanned_data = result.getText().split("@");

                verifyUserKey(scanned_data[0], scanned_data[1]);
            }
            catch (Exception e) {
                registerActivity.runOnUiThread(() -> {
                    Toast.makeText(registerActivity, "Invalid QR Code. Please try again", Toast.LENGTH_SHORT).show();
                });
            }
        });

        codeScanner.setErrorCallback(error -> {
            registerActivity.runOnUiThread(() -> {
                Log.e("scanner-error", "Camera initialization error" + error.getMessage());
            });
        });

        binding.scannerView.setOnClickListener(v -> {
            codeScanner.startPreview();
        });
    }

    private void verifyUserKey(String key, String phoneNumber) {
        firebaseFirestore
                .collection("users")
                .document(key)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot != null && documentSnapshot.getId().equals(key)) {
                        Toast.makeText(registerActivity, "QR Code successfully read! Wait for SMS code to proceed.", Toast.LENGTH_SHORT).show();
                        verifyPhoneNumber(phoneNumber, key);
                    }
                    else {
                        Toast.makeText(registerActivity, "Invalid QR code! Use different code, or contact QR Code provider.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("firebase-tag", e.getMessage());
                    Toast.makeText(registerActivity, "Something went wrong. Please try again later!", Toast.LENGTH_SHORT).show();
                });
    }

    private void verifyPhoneNumber(String phoneNumber, String documentId) {

        PhoneAuthOptions phoneAuthOptions =
                PhoneAuthOptions
                        .newBuilder(firebaseAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(registerActivity)
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
                                CodeScannerFragmentDirections.ActionCodeScannerFragmentToCodeVerifierFragment action =
                                        CodeScannerFragmentDirections.actionCodeScannerFragmentToCodeVerifierFragment(verificationId, documentId);
                                action.setVerificationId(verificationId);
                                action.setDocumentId(documentId);
                                navController.navigate(action);
                            }

                            @Override
                            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                                super.onCodeAutoRetrievalTimeOut(s);
                                Log.d("verification-tag", "onCodeAutoRetrievalTimeOut: " + s);
                            }
                        })
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions);

    }

    private void setupCameraPermissions() {
        if(ContextCompat.checkSelfPermission(registerActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            makePermissionRequest();
        }
    }

    private void makePermissionRequest() {
        ActivityCompat.requestPermissions(registerActivity, new String[]{ Manifest.permission.CAMERA }, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(registerActivity, "You need camera permission to be able to use this app", Toast.LENGTH_SHORT).show();
            }
        }
    }
}