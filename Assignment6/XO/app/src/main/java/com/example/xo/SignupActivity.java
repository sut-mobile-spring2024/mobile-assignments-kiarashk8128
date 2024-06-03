package com.example.xo;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    private EditText usernameEditText, emailEditText, dobEditText, passwordEditText, confirmPasswordEditText;
    private RadioGroup genderRadioGroup;
    private Button signupButton;
    private TextView signInLink;

    private String deviceUUID;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        deviceUUID = getDeviceUUID(this);
        dbHelper = new DatabaseHelper(this);

        usernameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email);
        dobEditText = findViewById(R.id.dateofbirth);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirmpassword);
        genderRadioGroup = findViewById(R.id.gender);
        signupButton = findViewById(R.id.signup_btn);
        signInLink = findViewById(R.id.signInLink);

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

        signInLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignInClicked(v);
            }
        });
    }



    public void onSignInClicked(View view) {
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void signUp(String username, String email, String dob, String password, String confirmPassword, RadioGroup gender){
        if (!isValidUsername(username)) {
            Toast.makeText(SignupActivity.this, "Invalid username", Toast.LENGTH_LONG).show();
            return;
        }
        if (usernameExists(username)) {
            Toast.makeText(SignupActivity.this, "Username already exists!", Toast.LENGTH_LONG).show();
            return;
        }

        if (!isValidEmail(email)) {
            Toast.makeText(SignupActivity.this, "Invalid email address", Toast.LENGTH_LONG).show();
            return;
        }

        if (!isValidDateOfBirth(dob)) {
            Toast.makeText(SignupActivity.this, "Invalid date of birth format", Toast.LENGTH_LONG).show();
            return;
        }

        if (!isValidGender()) {
            Toast.makeText(SignupActivity.this, "Please select gender!", Toast.LENGTH_LONG).show();
            return;
        }

        if (!isValidPassword(password)) {
            Toast.makeText(SignupActivity.this, "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one digit!", Toast.LENGTH_LONG).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(SignupActivity.this, "Passwords do not match", Toast.LENGTH_LONG).show();
            return;
        }

        // Save user to database
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USERNAME, username);
        values.put(DatabaseHelper.COLUMN_EMAIL, email);
        values.put(DatabaseHelper.COLUMN_DATE_OF_BIRTH, dob);
        values.put(DatabaseHelper.COLUMN_GENDER, gender.getCheckedRadioButtonId() == R.id.male ? "Male" : "Female");
        values.put(DatabaseHelper.COLUMN_PASSWORD, password);
        values.put(DatabaseHelper.COLUMN_DEVICE_UUID, deviceUUID);
        db.insert(DatabaseHelper.TABLE_USERS, null, values);
        db.close();

        String successfulSignupText = "Username " + username + " successfully registered with UUID: " + deviceUUID;
        // Ensure the toast message is displayed before navigating away
        Toast.makeText(SignupActivity.this, successfulSignupText, Toast.LENGTH_LONG).show();

        // Delay the intent transition slightly to ensure the toast is visible
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Finish the signup activity so that the user cannot return to it with the back button
            }
        }, 2000);

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

    private String getDeviceUUID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private boolean usernameExists(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null, DatabaseHelper.COLUMN_USERNAME + "=?",
                new String[]{username}, null, null, null);

        boolean exists = (cursor != null && cursor.moveToFirst());
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return exists;
    }
}
