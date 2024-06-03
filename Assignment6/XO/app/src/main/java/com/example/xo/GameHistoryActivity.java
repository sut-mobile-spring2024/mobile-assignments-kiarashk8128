package com.example.xo;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameHistoryActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private int userId;
    private ListView historyListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_history);

        dbHelper = new DatabaseHelper(this);
        userId = getIntent().getIntExtra("user_id", -1);

        if (userId == -1) {
            Log.e("GameHistoryActivity", "No user_id provided in intent");
            finish();
            return;
        }

        historyListView = findViewById(R.id.historyListView);
        loadGameHistory();
    }

    private void loadGameHistory() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_GAME_HISTORY, null,
                DatabaseHelper.COLUMN_USER_ID_FK + "=?",
                new String[]{String.valueOf(userId)}, null, null, DatabaseHelper.COLUMN_GAME_DATE + " DESC");

        List<HashMap<String, String>> historyList = new ArrayList<>();
        if (cursor != null) {
            int dateIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_GAME_DATE);
            int player1Index = cursor.getColumnIndex(DatabaseHelper.COLUMN_PLAYER1_SYMBOL);
            int player2Index = cursor.getColumnIndex(DatabaseHelper.COLUMN_PLAYER2_SYMBOL);
            int resultIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_RESULT);

            if (dateIndex >= 0 && player1Index >= 0 && player2Index >= 0 && resultIndex >= 0) {
                while (cursor.moveToNext()) {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("date", cursor.getString(dateIndex));
                    map.put("player1", cursor.getString(player1Index));
                    map.put("player2", cursor.getString(player2Index));
                    map.put("result", cursor.getString(resultIndex));
                    map.put("details", "Player 1: " + cursor.getString(player1Index) +
                            " vs Player 2: " + cursor.getString(player2Index) +
                            "\nResult: " + cursor.getString(resultIndex));
                    historyList.add(map);
                }
            } else {
                Log.e("GameHistoryActivity", "One or more columns not found in the cursor");
            }
            cursor.close();
        }
        db.close();

        SimpleAdapter adapter = new SimpleAdapter(this, historyList,
                android.R.layout.simple_list_item_2,
                new String[]{"date", "details"},
                new int[]{android.R.id.text1, android.R.id.text2});
        historyListView.setAdapter(adapter);
    }
}
