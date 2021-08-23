package rs.ac.bg.etf.diplomski.authenticationapp.account_setup;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import dagger.hilt.android.AndroidEntryPoint;
import rs.ac.bg.etf.diplomski.authenticationapp.databinding.ActivityRegisterBinding;

@AndroidEntryPoint
public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
    }
}