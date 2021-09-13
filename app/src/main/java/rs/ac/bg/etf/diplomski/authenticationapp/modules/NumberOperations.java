package rs.ac.bg.etf.diplomski.authenticationapp.modules;

import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.text.NumberFormat;
import java.text.ParseException;

public class NumberOperations {
    public static Number fetchNumber(TextInputLayout textInputLayout) {
        Number result = 0;
        try {
            result = NumberFormat.getInstance().parse(textInputLayout.getEditText().getText().toString());
        }
        catch (ParseException e) {
            textInputLayout.getEditText().requestFocus();
        }

        return result;
    }
}
