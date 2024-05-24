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
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (field[i][j].equals("")) {
                    field[i][j] = computerSymbol;
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
                        return;
                    }
                    field[i][j] = "";
                }
            }
        }

        // Block player's winning move
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (field[i][j].equals("")) {
                    field[i][j] = playerSymbol;
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
                        return;
                    }
                    field[i][j] = "";
                }
            }
        }

        // Make a random move
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
}
