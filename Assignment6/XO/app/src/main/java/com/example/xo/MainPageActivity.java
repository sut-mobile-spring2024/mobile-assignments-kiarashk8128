package com.example.xo;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainPageActivity extends AppCompatActivity {

    private Button exitButton;
    private Button viewGameHistoryButton;
    private Button startGameButton;
    private RadioGroup gameModeGroup;
    private RadioGroup playerSymbolGroup;
    private DatabaseHelper dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        dbHelper = new DatabaseHelper(this);
        userId = getIntent().getIntExtra("user_id", -1);
        Log.d("MainPageActivity", "Received user_id: " + userId); // Add logging here
        exitButton = findViewById(R.id.exitButton);
        startGameButton = findViewById(R.id.startGameButton);
        viewGameHistoryButton = findViewById(R.id.viewGameHistoryButton);
        gameModeGroup = findViewById(R.id.gameModeGroup);
        playerSymbolGroup = findViewById(R.id.playerSymbolGroup);

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainPageActivity.this, SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Check for unfinished game when the activity is created
        if (checkForUnfinishedGame()) {
            new AlertDialog.Builder(this)
                    .setTitle("Unfinished Game")
                    .setMessage("You have an unfinished game. Do you want to continue?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            continueGame();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            deleteUnfinishedGame();
                            startNewGame();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        }

        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewGame();
            }
        });

        viewGameHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                debugGameHistory(); // Debug the game history
                Intent intent = new Intent(MainPageActivity.this, GameHistoryActivity.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });
    }

    private void deleteUnfinishedGame() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deletedRows = db.delete(DatabaseHelper.TABLE_GAME_STATE,
                DatabaseHelper.COLUMN_USER_ID_FK + "=? AND " + DatabaseHelper.COLUMN_IS_FINISHED + "=?",
                new String[]{String.valueOf(userId), "0"});

        if (deletedRows > 0) {
            Toast.makeText(this, "Unfinished game deleted successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No unfinished game to delete", Toast.LENGTH_SHORT).show();
        }

        db.close();
    }

    private void startNewGame() {
        int selectedGameModeId = gameModeGroup.getCheckedRadioButtonId();
        int selectedPlayerSymbolId = playerSymbolGroup.getCheckedRadioButtonId();

        if (selectedGameModeId == -1 || selectedPlayerSymbolId == -1) {
            Toast.makeText(MainPageActivity.this, "Please select game mode and symbol", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedGameModeButton = findViewById(selectedGameModeId);
        RadioButton selectedPlayerSymbolButton = findViewById(selectedPlayerSymbolId);

        String gameMode = selectedGameModeButton.getText().toString();
        String playerSymbol = selectedPlayerSymbolButton.getText().toString();

        Intent intent = new Intent(MainPageActivity.this, XOActivity.class);
        intent.putExtra("gameMode", gameMode);
        intent.putExtra("playerSymbol", playerSymbol);
        intent.putExtra("user_id", userId);
        startActivity(intent);
        finish(); // Finish the main page activity
    }

    private boolean checkForUnfinishedGame() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_GAME_STATE, null,
                DatabaseHelper.COLUMN_USER_ID_FK + "=? AND " + DatabaseHelper.COLUMN_IS_FINISHED + "=?",
                new String[]{String.valueOf(userId), "0"}, null, null, null);

        boolean hasUnfinishedGame = (cursor != null && cursor.moveToFirst());
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return hasUnfinishedGame;
    }

    private void continueGame() {
        Intent intent = new Intent(MainPageActivity.this, XOActivity.class);
        intent.putExtra("user_id", userId);
        intent.putExtra("continue_game", true);
        startActivity(intent);
        finish(); // Finish the main page activity
    }

    private void debugGameHistory() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_GAME_HISTORY, null,
                DatabaseHelper.COLUMN_USER_ID_FK + "=?",
                new String[]{String.valueOf(userId)}, null, null, DatabaseHelper.COLUMN_GAME_DATE + " DESC");

        if (cursor != null) {
            int dateIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_GAME_DATE);
            int player1Index = cursor.getColumnIndex(DatabaseHelper.COLUMN_PLAYER1_SYMBOL);
            int player2Index = cursor.getColumnIndex(DatabaseHelper.COLUMN_PLAYER2_SYMBOL);
            int resultIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_RESULT);

            if (dateIndex >= 0 && player1Index >= 0 && player2Index >= 0 && resultIndex >= 0) {
                while (cursor.moveToNext()) {
                    String date = cursor.getString(dateIndex);
                    String player1 = cursor.getString(player1Index);
                    String player2 = cursor.getString(player2Index);
                    String result = cursor.getString(resultIndex);

                    Log.d("GameHistoryDebug", "Date: " + date + ", Player1: " + player1 + ", Player2: " + player2 + ", Result: " + result);
                }
            } else {
                Log.e("GameHistoryDebug", "One or more columns not found in the cursor");
            }
            cursor.close();
        } else {
            Log.e("GameHistoryDebug", "Cursor is null");
        }
        db.close();
    }
}
