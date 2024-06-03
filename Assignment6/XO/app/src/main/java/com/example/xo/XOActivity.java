package com.example.xo;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class XOActivity extends AppCompatActivity implements View.OnClickListener {

    private Button[][] buttons = new Button[3][3];
    private boolean player1Turn = true;
    private int roundCount;
    private boolean havePlayer2 = false;
    private boolean playWithComputer = false;
    private String player1Symbol = "X";
    private String player2Symbol = "O";
    private String computerSymbol = "O";
    private Handler handler = new Handler();
    private DatabaseHelper dbHelper;
    private int userId;
    private boolean continueGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xo);

        dbHelper = new DatabaseHelper(this);
        userId = getIntent().getIntExtra("user_id", -1);
        continueGame = getIntent().getBooleanExtra("continue_game", false);
        Log.d("XOActivity", "Received user_id: " + userId); // Add logging here
        // Debug print all users and their states
        debugPrintAllUsers();

        // Initialize buttons
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                String buttonID = "button" + (i * 3 + j + 1);
                int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
                buttons[i][j] = findViewById(resID);
                buttons[i][j].setOnClickListener(this);
            }
        }

        Button buttonClear = findViewById(R.id.clearButton);
        buttonClear.setOnClickListener(v -> clearBoard());

        Button buttonExit = findViewById(R.id.exitButton);
        buttonExit.setOnClickListener(v -> {
            debugGameHistory(); // Add this line to debug game history before exiting
            exitToLogin();
        });
        if (continueGame) {
            loadGameState();
        } else {
            // Get game mode and player symbol from intent
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                String gameMode = extras.getString("gameMode");
                player1Symbol = extras.getString("playerSymbol");
                playWithComputer = "Human vs Computer".equals(gameMode);
                if (playWithComputer) {
                    computerSymbol = "X".equals(player1Symbol) ? "O" : "X";
                    player2Symbol = "";
                } else {
                    player2Symbol = "X".equals(player1Symbol) ? "O" : "X";
                    computerSymbol = "";
                    havePlayer2 = true;
                }
            }
        }
    }

    private void exitToLogin() {
        debugPrintGameStateForUser(userId);
        debugPrintGameState(); // Add this line to debug before exiting
        Intent intent = new Intent(XOActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Finish the XO activity
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
    @Override
    public void onClick(View v) {
        if (!((Button) v).getText().toString().equals("")) {
            return;
        }

        if (player1Turn) {
            ((Button) v).setText(player1Symbol);
            ((Button) v).setTextColor(getResources().getColor(R.color.pink));
        } else {
            if (playWithComputer) {
                ((Button) v).setText(computerSymbol);
                ((Button) v).setTextColor(getResources().getColor(R.color.yellow));
            } else {
                ((Button) v).setText(player2Symbol);
                ((Button) v).setTextColor(getResources().getColor(R.color.yellow));
            }
        }

        roundCount++;

        if (checkForWin()) {
            if (player1Turn) {
                player1Wins();
            } else {
                player2Wins();
            }
            saveGameState(true); // Save the game state after a win
        } else if (roundCount == 9) {
            draw();
            saveGameState(true); // Save the game state after a draw
        } else {
            if (havePlayer2) {
                player1Turn = !player1Turn;
            } else {
                player1Turn = !player1Turn;
                predictAndToastWinner(false); // Predict for CPU move
                handler.postDelayed(this::computerMove, 4000); // Delay for CPU move
            }
            saveGameState(false); // Save the game state after each move
        }

        if (playWithComputer && player1Turn) {
            predictAndToastWinner(true); // Predict for human move
        }
    }

    private boolean checkForWin() {
        String[][] field = new String[3][3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                field[i][j] = buttons[i][j].getText().toString();
            }
        }

        for (int i = 0; i < 3; i++) {
            if (field[i][0].equals(field[i][1])
                    && field[i][0].equals(field[i][2])
                    && !field[i][0].equals("")) {
                return true;
            }
            if (field[0][i].equals(field[1][i])
                    && field[0][i].equals(field[2][i])
                    && !field[0][i].equals("")) {
                return true;
            }
        }

        if (field[0][0].equals(field[1][1])
                && field[0][0].equals(field[2][2])
                && !field[0][0].equals("")) {
            return true;
        }
        if (field[0][2].equals(field[1][1])
                && field[0][2].equals(field[2][0])
                && !field[0][2].equals("")) {
            return true;
        }

        return false;
    }

    private void player1Wins() {
        Toast.makeText(this, "Player " + player1Symbol + " wins!", Toast.LENGTH_LONG).show();
        saveGameHistory(player1Symbol + " wins");
        clearBoard();
    }

    private void player2Wins() {
        if (playWithComputer) {
            Toast.makeText(this, "Player " + computerSymbol + " wins!", Toast.LENGTH_LONG).show();
            saveGameHistory(computerSymbol + " wins");
        } else {
            Toast.makeText(this, "Player " + player2Symbol + " wins!", Toast.LENGTH_LONG).show();
            saveGameHistory(player2Symbol + " wins");
        }
        clearBoard();
    }

    private void draw() {
        Toast.makeText(this, "Draw!", Toast.LENGTH_LONG).show();
        saveGameHistory("Draw");
        clearBoard();
    }

    private void saveGameHistory(String result) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_USER_ID_FK, userId);
        values.put(DatabaseHelper.COLUMN_GAME_DATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        values.put(DatabaseHelper.COLUMN_PLAYER1_SYMBOL, player1Symbol);
        values.put(DatabaseHelper.COLUMN_PLAYER2_SYMBOL, player2Symbol);
        values.put(DatabaseHelper.COLUMN_RESULT, result);

        long rowId = db.insert(DatabaseHelper.TABLE_GAME_HISTORY, null, values);
        Log.d("XOActivity", "Game history saved. Row ID: " + rowId);

        db.close();
    }

    private void clearBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setText("");
            }
        }

        roundCount = 0;
        player1Turn = true;
        saveGameState(true); // Clear the saved game state
    }

    private void computerMove() {
        String[][] field = new String[3][3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                field[i][j] = buttons[i][j].getText().toString();
            }
        }

        // Check if computer can win
        if (tryToWinOrBlock(field, computerSymbol)) {
            predictAndToastWinner(true);
            saveGameState(false); // Save the game state after each move
            return;
        }

        // Block player's winning move
        if (tryToWinOrBlock(field, player1Symbol)) {
            predictAndToastWinner(true);
            saveGameState(false); // Save the game state after each move
            return;
        }

        // Try to fill its own row, column, or diagonal
        if (tryToFillOwnLine(field)) {
            predictAndToastWinner(true);
            saveGameState(false); // Save the game state after each move
            return;
        }

        // Make a random move
        makeRandomMove(field);
        predictAndToastWinner(true);
        saveGameState(false); // Save the game state after each move
    }

    private boolean tryToWinOrBlock(String[][] field, String symbol) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (field[i][j].equals("")) {
                    field[i][j] = symbol;
                    if (checkWin(field, symbol)) {
                        buttons[i][j].setText(computerSymbol);
                        buttons[i][j].setTextColor(getResources().getColor(R.color.yellow));
                        roundCount++;
                        if (checkForWin()) {
                            player2Wins();
                        } else if (roundCount == 9) {
                            draw();
                        } else {
                            player1Turn = true;
                        }
                        return true;
                    }
                    field[i][j] = "";
                }
            }
        }
        return false;
    }

    private boolean tryToFillOwnLine(String[][] field) {
        // Check rows
        for (int i = 0; i < 3; i++) {
            if (checkAndFillLine(field, i, 0, 0, 1)) {
                return true;
            }
        }
        // Check columns
        for (int i = 0; i < 3; i++) {
            if (checkAndFillLine(field, 0, i, 1, 0)) {
                return true;
            }
        }
        // Check diagonals
        if (checkAndFillLine(field, 0, 0, 1, 1)) {
            return true;
        }
        if (checkAndFillLine(field, 0, 2, 1, -1)) {
            return true;
        }
        return false;
    }

    private boolean checkAndFillLine(String[][] field, int startX, int startY, int dx, int dy) {
        int count = 0;
        int emptyX = -1, emptyY = -1;
        for (int i = 0; i < 3; i++) {
            int x = startX + i * dx;
            int y = startY + i * dy;
            if (field[x][y].equals(computerSymbol)) {
                count++;
            } else if (field[x][y].equals("")) {
                emptyX = x;
                emptyY = y;
            } else {
                return false;
            }
        }
        if (count == 1 && emptyX != -1) {
            buttons[emptyX][emptyY].setText(computerSymbol);
            buttons[emptyX][emptyY].setTextColor(getResources().getColor(R.color.yellow));
            roundCount++;
            if (checkForWin()) {
                player2Wins();
            } else if (roundCount == 9) {
                draw();
            } else {
                player1Turn = true;
            }
            return true;
        }
        return false;
    }

    private void makeRandomMove(String[][] field) {
        List<int[]> emptyCells = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (field[i][j].equals("")) {
                    emptyCells.add(new int[]{i, j});
                }
            }
        }

        Random rand = new Random();
        int[] move = emptyCells.get(rand.nextInt(emptyCells.size()));
        buttons[move[0]][move[1]].setText(computerSymbol);
        buttons[move[0]][move[1]].setTextColor(getResources().getColor(R.color.yellow));
        roundCount++;
        if (checkForWin()) {
            player2Wins();
        } else if (roundCount == 9) {
            draw();
        } else {
            player1Turn = true;
        }
    }

    private boolean checkWin(String[][] field, String symbol) {
        for (int i = 0; i < 3; i++) {
            if (field[i][0].equals(symbol) && field[i][1].equals(symbol) && field[i][2].equals(symbol)) {
                return true;
            }
            if (field[0][i].equals(symbol) && field[1][i].equals(symbol) && field[2][i].equals(symbol)) {
                return true;
            }
        }

        if (field[0][0].equals(symbol) && field[1][1].equals(symbol) && field[2][2].equals(symbol)) {
            return true;
        }
        if (field[0][2].equals(symbol) && field[1][1].equals(symbol) && field[2][0].equals(symbol)) {
            return true;
        }

        return false;
    }

    private boolean isFull(String[][] field) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (field[i][j].equals("")) {
                    return false;
                }
            }
        }
        return true;
    }

    // Minimax algorithm to predict probable winner
    private String predictWinner() {
        String[][] field = new String[3][3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                field[i][j] = buttons[i][j].getText().toString();
            }
        }

        int score = minimax(field, 0, !player1Turn); // Use current turn for prediction
        if (score > 0) {
            return computerSymbol;
        } else if (score < 0) {
            return player1Symbol;
        } else {
            return "Draw";
        }
    }

    private int minimax(String[][] field, int depth, boolean isMaximizing) {
        if (checkWin(field, computerSymbol)) {
            return 10 - depth;
        }
        if (checkWin(field, player1Symbol)) {
            return depth - 10;
        }
        if (isFull(field)) {
            return 0;
        }

        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (field[i][j].equals("")) {
                        field[i][j] = computerSymbol;
                        int score = minimax(field, depth + 1, false);
                        field[i][j] = "";
                        bestScore = Math.max(score, bestScore);
                    }
                }
            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (field[i][j].equals("")) {
                        field[i][j] = player1Symbol;
                        int score = minimax(field, depth + 1, true);
                        field[i][j] = "";
                        bestScore = Math.min(score, bestScore);
                    }
                }
            }
            return bestScore;
        }
    }

    private void predictAndToastWinner(boolean isPlayerTurn) {
        String probableWinner = predictWinner();
        Toast.makeText(this, "Probable winner: " + probableWinner, Toast.LENGTH_SHORT).show();
    }

    private void saveGameState(boolean isGameOver) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Concatenate the board state into a single string, using spaces for empty cells
        StringBuilder boardState = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                String cell = buttons[i][j].getText().toString();
                boardState.append(cell.isEmpty() ? " " : cell);
            }
        }

        // Put the game state values
        values.put(DatabaseHelper.COLUMN_USER_ID_FK, userId);
        values.put(DatabaseHelper.COLUMN_BOARD_STATE, boardState.toString());
        values.put(DatabaseHelper.COLUMN_PLAYER_TURN, player1Turn ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_ROUND_COUNT, roundCount);
        values.put(DatabaseHelper.COLUMN_PLAYER1_SYMBOL, player1Symbol);
        values.put(DatabaseHelper.COLUMN_PLAYER2_SYMBOL, player2Symbol);
        values.put(DatabaseHelper.COLUMN_COMPUTER_SYMBOL, computerSymbol);
        values.put(DatabaseHelper.COLUMN_IS_FINISHED, isGameOver ? 1 : 0);

        // Replace the old state with the new one
        long rowId = db.insertWithOnConflict(DatabaseHelper.TABLE_GAME_STATE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        Log.d("XOActivity", "Game state saved. Row ID: " + rowId + ", Board State: " + boardState.toString());

        db.close();
    }

    private void loadGameState() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_GAME_STATE, null,
                DatabaseHelper.COLUMN_USER_ID_FK + "=? AND " + DatabaseHelper.COLUMN_IS_FINISHED + "=0",
                new String[]{String.valueOf(userId)}, null, null, DatabaseHelper.COLUMN_GAME_ID + " DESC", "1");

        if (cursor != null && cursor.moveToFirst()) {
            int boardStateIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_BOARD_STATE);
            int playerTurnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PLAYER_TURN);
            int roundCountIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ROUND_COUNT);
            int player1SymbolIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PLAYER1_SYMBOL);
            int player2SymbolIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PLAYER2_SYMBOL);
            int computerSymbolIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_COMPUTER_SYMBOL);

            if (boardStateIndex != -1) {
                String boardState = cursor.getString(boardStateIndex);
                Log.d("XOActivity", "Loading game state. Board State: " + boardState);
                if (boardState.length() == 9) {
                    for (int i = 0; i < 3; i++) {
                        for (int j = 0; j < 3; j++) {
                            char symbol = boardState.charAt(i * 3 + j);
                            buttons[i][j].setText(symbol == ' ' ? "" : String.valueOf(symbol));
                        }
                    }
                } else {
                    Log.e("XOActivity", "Invalid board state length: " + boardState.length());
                    // Handle invalid board state length, possibly reset the board or show an error message
                }
            } else {
                Log.e("XOActivity", "Board state column index not found");
            }

            if (playerTurnIndex != -1) {
                player1Turn = cursor.getInt(playerTurnIndex) == 1;
            }

            if (roundCountIndex != -1) {
                roundCount = cursor.getInt(roundCountIndex);
            }

            if (player1SymbolIndex != -1) {
                player1Symbol = cursor.getString(player1SymbolIndex);
            }

            if (player2SymbolIndex != -1) {
                player2Symbol = cursor.getString(player2SymbolIndex);
            }

            if (computerSymbolIndex != -1) {
                computerSymbol = cursor.getString(computerSymbolIndex);
            }

            cursor.close();
        } else {
            Log.e("XOActivity", "Failed to load game state from database for user ID: " + userId);
        }

        db.close();
    }



    private void debugPrintAllUsers() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null, null, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int userIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID);
                int usernameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USERNAME);

                if (userIdIndex != -1 && usernameIndex != -1) {
                    int userId = cursor.getInt(userIdIndex);
                    String username = cursor.getString(usernameIndex);
                    Log.d("DatabaseDebug", "UserID: " + userId + ", Username: " + username);
                } else {
                    Log.e("DatabaseDebug", "UserID or Username column not found");
                }
            }
            cursor.close();
        }
        db.close();
    }

    private void debugPrintGameState() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_GAME_STATE, null, null, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int boardStateIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_BOARD_STATE);
                int userIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID_FK);
                int isFinishedIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_IS_FINISHED);

                if (boardStateIndex != -1 && userIdIndex != -1 && isFinishedIndex != -1) {
                    String boardState = cursor.getString(boardStateIndex);
                    int userId = cursor.getInt(userIdIndex);
                    int isFinished = cursor.getInt(isFinishedIndex);

                    Log.d("DatabaseDebug", "UserID: " + userId + ", BoardState: " + boardState + ", Finished: " + isFinished);
                } else {
                    Log.e("DatabaseDebug", "One or more columns not found");
                }
            }
            cursor.close();
        }
        db.close();
    }

    private void debugPrintGameStateForUser(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_GAME_STATE, null,
                DatabaseHelper.COLUMN_USER_ID_FK + "=?",
                new String[]{String.valueOf(userId)}, null, null, DatabaseHelper.COLUMN_GAME_ID + " DESC", null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int boardStateIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_BOARD_STATE);
                int isFinishedIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_IS_FINISHED);

                if (boardStateIndex != -1 && isFinishedIndex != -1) {
                    String boardState = cursor.getString(boardStateIndex);
                    int isFinished = cursor.getInt(isFinishedIndex);

                    Log.d("DatabaseDebug", "UserID: " + userId + ", BoardState: " + boardState + ", Finished: " + isFinished);
                } else {
                    Log.e("DatabaseDebug", "One or more columns not found for user ID: " + userId);
                }
            }
            cursor.close();
        }
        db.close();
    }

}
