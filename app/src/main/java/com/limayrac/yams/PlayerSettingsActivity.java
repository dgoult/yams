package com.limayrac.yams;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class PlayerSettingsActivity extends AppCompatActivity {

    private ArrayList<Player> players = new ArrayList<>();
    private PlayerAdapter playerAdapter;
    public Button StartGameButton;
    private int iaCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_player_settings);

        RecyclerView recyclerView = findViewById(R.id.player_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        playerAdapter = new PlayerAdapter(players, this);
        recyclerView.setAdapter(playerAdapter);

        Button backButton = findViewById(R.id.back_button);
        Button addPlayerButton = findViewById(R.id.add_player_button);
        Button startGameButton = findViewById(R.id.start_game_button);
        StartGameButton = startGameButton;

        // Gestion du bouton retour
        backButton.setOnClickListener(v -> {
            // Revenir au menu principal
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            Intent intent = new Intent(PlayerSettingsActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Ferme cette activité
        });

        addPlayerButton.setOnClickListener(view -> {
            players.add(new Player("Player " + (players.size() + 1), "red"));
            playerAdapter.notifyItemInserted(players.size() - 1);
            // Debug : Voir si l'ajout a bien lieu
            Log.d("PlayerSettingsActivity", "Player added: " + players.get(players.size() - 1).getName());
            // Vérifier l'état du bouton "Lancer la partie"
            updateStartGameButtonState();
        });

        startGameButton.setOnClickListener(view -> {
            Intent intent = new Intent(PlayerSettingsActivity.this, GameActivity.class);
            intent.putParcelableArrayListExtra("players", players);
            startActivity(intent);
        });
    }

    // Méthode pour vérifier le nombre de joueurs humains et activer le bouton "Lancer la partie"
    void updateStartGameButtonState() {
        int humanPlayersCount = 0;
        int playersCount = 0;

        // Compter le nombre de joueurs humains
        for (Player player : players) {
            playersCount++;
            if (!player.isIa()) {
                humanPlayersCount++;
            }
        }
        Log.d("PlayerSettingsActivity", "updateStartGameButtonState -> humanPlayersCount : " + humanPlayersCount + ", playersCount :" + playersCount);

        // Activer le bouton "Lancer la partie" si au moins deux joueurs humains sont présents
        if (humanPlayersCount >= 1 && playersCount >= 2 ) {
            StartGameButton.setEnabled(true);
        } else {
            StartGameButton.setEnabled(false);
        }
    }

    // Méthode pour obtenir le prochain nom d'IA
    public String getNextIaName() {
        return "IA " + iaCount++;
    }
    public String decrementtIaCount() {
        return "IA " + iaCount--;
    }
}
