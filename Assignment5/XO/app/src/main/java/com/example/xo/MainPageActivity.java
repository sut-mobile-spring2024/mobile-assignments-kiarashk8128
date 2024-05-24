package com.example.xo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainPageActivity extends AppCompatActivity {

    private Button exitButton;
    private Button startGameButton;
    private RadioGroup gameModeGroup;
    private RadioGroup playerSymbolGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        exitButton = findViewById(R.id.exitButton);
        startGameButton = findViewById(R.id.startGameButton);
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

        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                startActivity(intent);
            }
        });
    }
}
