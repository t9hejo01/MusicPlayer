package music.player;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

public class Utilities extends AppCompatActivity {

    boolean checkUsernameField(Context context, TextInputLayout textLayout, TextView textField, Button registerButton) {
        String textFieldText = textField.getText().toString();
        if (textFieldText.isEmpty()) {
            setErrorToLayout(context.getString(R.string.usernameEmpty), textLayout);
            registerButton.setEnabled(false);
        }
        textLayout.setErrorEnabled(false);
        return true;
    }

    boolean checkEmailField(Context context, TextInputLayout textLayout, TextView textField, Button registerButton) {
        String textFieldText = textField.getText().toString();
        if (textFieldText.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(textFieldText).matches()) {
            setErrorToLayout(context.getString(R.string.emailError), textLayout);
            registerButton.setEnabled(true);
            return false;
        }
        textLayout.setErrorEnabled(false);
        return true;
    }

    void setErrorToLayout(String error, TextInputLayout textLayout) {
        // Set error to layout
        if (!error.isEmpty() && textLayout != null) {
            textLayout.setError(error);
            textLayout.setErrorEnabled(true);
        }
    }

    boolean checkPasswords(Context context, TextInputLayout passwordLayout, TextView passwordField, TextInputLayout passwordAgainLayout, TextView passwordAgainField) {
        // Check that passwords match and aren't empty
        if (!passwordField.getText().toString().isEmpty()) {
            passwordLayout.setErrorEnabled(false);

            String password = passwordField.getText().toString();
            String passwordAgain = passwordAgainField.getText().toString();
            if (password.length() < 8) {
                setErrorToLayout(context.getString(R.string.passwordLength), passwordLayout);
                return false;
            }

            if (password.equals(passwordAgain)) {
                passwordAgainLayout.setErrorEnabled(false);
                return true; // Password is good!
            } // Passwords don't match
            setErrorToLayout(context.getString(R.string.passwordNotMatch), passwordAgainLayout);
        } // Password field is empty
            setErrorToLayout(context.getString(R.string.passwordEmpty), passwordLayout);
            return false; // Password not ok!
    }
}