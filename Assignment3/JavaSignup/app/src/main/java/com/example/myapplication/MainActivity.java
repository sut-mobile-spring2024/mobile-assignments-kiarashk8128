package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final String ExistingUsername = "kiarash";

    private EditText usernameEditText, emailEditText, dobEditText, passwordEditText, confirmPasswordEditText;
    private RadioGroup genderRadioGroup;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usernameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email);
        dobEditText = findViewById(R.id.dateofbirth);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirmpassword);
        genderRadioGroup = findViewById(R.id.gender);
        signupButton = findViewById(R.id.signup_btn);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String dob = dobEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String confirmPassword = confirmPasswordEditText.getText().toString().trim();
                signUp(username, email, dob, password, confirmPassword, genderRadioGroup);
            }
            });
    }

    private boolean isValidUsername(String username) {
        return !username.isEmpty();
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidDateOfBirth(String dob) {
        return dob.matches("\\d{2}/\\d{2}/\\d{4}");
    }

    private boolean isValidGender() {
        return genderRadioGroup.getCheckedRadioButtonId() != -1;
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8 && password.matches(".*[A-Z].*") && password.matches(".*[a-z].*") && password.matches(".*\\d.*");
    }

    private void signUp(String username, String email, String dob, String password, String confirmPassword, RadioGroup gender){
        if (!isValidUsername(username)) {
            Toast.makeText(MainActivity.this, "Invalid username", Toast.LENGTH_LONG).show();
            return;
        }
        if(username.equals(ExistingUsername)){
            Toast.makeText(MainActivity.this, "Username already exists!", Toast.LENGTH_LONG).show();
            return;
        }

        if (!isValidEmail(email)) {
            Toast.makeText(MainActivity.this, "Invalid email address", Toast.LENGTH_LONG).show();
            return;
        }

        if (!isValidDateOfBirth(dob)) {
            Toast.makeText(MainActivity.this, "Invalid date of birth format", Toast.LENGTH_LONG).show();
            return;
        }

        if (!isValidGender()) {
            Toast.makeText(MainActivity.this, "Please select gender!", Toast.LENGTH_LONG).show();
            return;
        }

        if (!isValidPassword(password)) {
            Toast.makeText(MainActivity.this, "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one digit!", Toast.LENGTH_LONG).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(MainActivity.this, "Passwords do not match", Toast.LENGTH_LONG).show();
            return;
        }

        String SuccessfulSignupText = "Username " + username + " successfully registered";
        Toast.makeText(MainActivity.this, SuccessfulSignupText, Toast.LENGTH_LONG).show();
    }
}