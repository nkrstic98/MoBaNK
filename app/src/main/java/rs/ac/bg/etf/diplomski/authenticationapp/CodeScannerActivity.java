package rs.ac.bg.etf.diplomski.authenticationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.budiyev.android.codescanner.AutoFocusMode;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.ScanMode;

import dagger.hilt.android.AndroidEntryPoint;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.ActivityCodeScannerBinding;

@AndroidEntryPoint
public class CodeScannerActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 101;

    ActivityCodeScannerBinding binding;

    private CodeScanner codeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCodeScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
            runOnUiThread(() -> {
//                String[] scanned_data = result.getText().split("@");
//                binding.textViewKey.setText(scanned_data[0]);
//                binding.textViewPhone.setText(scanned_data[1]);
                binding.textViewKey.setText(result.getText());
            });
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
    protected void onResume() {
        super.onResume();
        codeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        codeScanner.releaseResources();
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