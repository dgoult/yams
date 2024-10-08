package com.limayrac.yams;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private ArrayList<Player> players;
    private int currentPlayerIndex = 0;
    private int[] diceValues = new int[5];
    private ImageView[] diceImages = new ImageView[5];
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        players = getIntent().getParcelableArrayListExtra("players");

        diceImages[0] = findViewById(R.id.dice1);
        diceImages[1] = findViewById(R.id.dice2);
        diceImages[2] = findViewById(R.id.dice3);
        diceImages[3] = findViewById(R.id.dice4);
        diceImages[4] = findViewById(R.id.dice5);

        Button rollButton = findViewById(R.id.roll_button);
        rollButton.setOnClickListener(view -> rollDice());

        updateUI();
    }

    private void rollDice() {
        for (int i = 0; i < 5; i++) {
            diceValues[i] = random.nextInt(6) + 1;
            updateDiceImage(i);
        }
        // Logic to allow re-rolling and score marking
    }

    private void updateDiceImage(int index) {
        int drawableResource = getResources().getIdentifier("dice_" + diceValues[index], "drawable", getPackageName());
        diceImages[index].setImageResource(drawableResource);
    }

    private void updateUI() {
        // Update UI to show whose turn it is, remaining rolls, etc.
    }
}