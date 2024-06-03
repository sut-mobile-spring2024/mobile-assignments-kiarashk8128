package com.example.xo;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private DatabaseHelper dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginbtn);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredUsername = usernameEditText.getText().toString().trim();
                String enteredPassword = passwordEditText.getText().toString().trim();

                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null,
                        DatabaseHelper.COLUMN_USERNAME + "=? AND " + DatabaseHelper.COLUMN_PASSWORD + "=?",
                        new String[]{enteredUsername, enteredPassword}, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    userId = -1;
                    int userIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID);
                    if (userIdIndex!=-1){
                        userId = cursor.getInt(userIdIndex);

                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(LoginActivity.this, MainPageActivity.class);
                            intent.putExtra("user_id", userId);
                            startActivity(intent);
                            finish(); // Finish the login activity so that the user cannot return to it with the back button
                        }
                    }, 2000);
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed. Please check your credentials", Toast.LENGTH_LONG).show();
                }

                if (cursor != null) {
                    cursor.close();
                }
                db.close();
            }
        });

    }
}
