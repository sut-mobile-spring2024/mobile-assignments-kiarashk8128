package com.example.xo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class XOActivity extends AppCompatActivity implements View.OnClickListener {

    private Button[][] buttons = new Button[3][3];
    private boolean player1Turn = true;
    private int roundCount;
    private boolean playWithComputer = false;
    private String playerSymbol = "X";
    private String computerSymbol = "O";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xo);

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

        // Get game mode and player symbol from intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String gameMode = extras.getString("gameMode");
            playerSymbol = extras.getString("playerSymbol");
            playWithComputer = "Human vs Computer".equals(gameMode);
            computerSymbol = "X".equals(playerSymbol) ? "O" : "X";
        }
    }

    @Override
    public void onClick(View v) {
        if (!((Button) v).getText().toString().equals("")) {
            return;
        }

        if (player1Turn) {
            ((Button) v).setText(playerSymbol);
            ((Button) v).setTextColor(getResources().getColor(R.color.pink));
        } else {
            ((Button) v).setText(computerSymbol);
            ((Button) v).setTextColor(getResources().getColor(R.color.yellow));
        }

        roundCount++;

        if (checkForWin()) {
            if (player1Turn) {
                player1Wins();
            } else {
                player2Wins();
            }
        } else if (roundCount == 9) {
            draw();
        } else {
            player1Turn = !player1Turn;
            if (playWithComputer && !player1Turn) {
                computerMove();
            }
        }

        // Predict probable winner
        String probableWinner = predictWinner();
        Toast.makeText(this, "Probable winner: " + probableWinner, Toast.LENGTH_SHORT).show();
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
        Toast.makeText(this, "Player " + playerSymbol + " wins!", Toast.LENGTH_LONG).show();
        clearBoard();
    }

    private void player2Wins() {
        Toast.makeText(this, "Player " + computerSymbol + " wins!", Toast.LENGTH_LONG).show();
        clearBoard();
    }

    private void draw() {
        Toast.makeText(this, "Draw!", Toast.LENGTH_LONG).show();
        clearBoard();
    }

    private void clearBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setText("");
            }
        }

        roundCount = 0;
        player1Turn = true;
    }

    private void computerMove() {
        String[][] field = new String[3][3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                field[i][j] = buttons[i][j].getText().toString();
            }
        }

        // Check if computer can win
        if (tryToWinOrBlock(field, computerSymbol)) return;

        // Block player's winning move
        if (tryToWinOrBlock(field, playerSymbol)) return;

        // Try to fill its own row, column, or diagonal
        if (tryToFillOwnLine(field)) return;

        // Make a random move
        makeRandomMove(field);
    }

    private boolean tryToWinOrBlock(String[][] field, String symbol) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (field[i][j].equals("")) {
                    field[i][j] = symbol;
                    if (checkWin(field)) {
                        buttons[i][j].setText(computerSymbol);
                        buttons[i][j].setTextColor(getResources().getColor(R.color.yellow));
                        roundCount++;
                        if (checkForWin()) {
                            player2Wins();
                        } else if (roundCount == 9) {
                            draw();
                        } else {
                            player1Turn = !player1Turn;
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
                player1Turn = !player1Turn;
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
            player1Turn = !player1Turn;
        }
    }

    private boolean checkWin(String[][] field) {
        for (int i = 0; i < 3; i++) {
            if (field[i][0].equals(field[i][1]) && field[i][0].equals(field[i][2]) && !field[i][0].equals("")) {
                return true;
            }
            if (field[0][i].equals(field[1][i]) && field[0][i].equals(field[2][i]) && !field[0][i].equals("")) {
                return true;
            }
        }

        if (field[0][0].equals(field[1][1]) && field[0][0].equals(field[2][2]) && !field[0][0].equals("")) {
            return true;
        }
        if (field[0][2].equals(field[1][1]) && field[0][2].equals(field[2][0]) && !field[0][2].equals("")) {
            return true;
        }

        return false;
    }

    // Minimax algorithm to predict probable winner
    private String predictWinner() {
        String[][] field = new String[3][3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                field[i][j] = buttons[i][j].getText().toString();
            }
        }

        int score = minimax(field, 0, true);
        if (score > 0) {
            return computerSymbol;
        } else if (score < 0) {
            return playerSymbol;
        } else {
            return "Draw";
        }
    }

    private int minimax(String[][] field, int depth, boolean isMaximizing) {
        if (checkWin(field, computerSymbol)) {
            return 10 - depth;
        }
        if (checkWin(field, playerSymbol)) {
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
                        field[i][j] = playerSymbol;
                        int score = minimax(field, depth + 1, true);
                        field[i][j] = "";
                        bestScore = Math.min(score, bestScore);
                    }
                }
            }
            return bestScore;
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
}