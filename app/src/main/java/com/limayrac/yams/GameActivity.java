package com.limayrac.yams;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Function;

public class GameActivity extends AppCompatActivity {

    private ArrayList<Player> players;
    private int currentPlayerIndex = 0; // Index du joueur actuel
    private TextView currentPlayerText; // TextView pour afficher le nom du joueur actuel
    private int[] diceValues = new int[5]; // Valeurs des dés après chaque lancer
    private boolean[] diceLocked = new boolean[5]; // Garde trace des dés à garder
    private ImageView[] diceImages = new ImageView[5]; // Références aux ImageView pour chaque dé
    private Random random = new Random(); // Générateur de nombres aléatoires
    private AnimationDrawable[] diceAnimations = new AnimationDrawable[5]; // Animations pour chaque dé
    private int rollsRemaining = 3; // Nombre de lancers restants (max 3)
    private TextView rollsRemainingTextView; // TextView pour afficher les lancers restants
    private LinearLayout lockedDiceLayout; // Layout pour les dés gardés
    private ScrollView scoreTableLayout; // Layout pour le tableau des scores
    private HashMap<Player, Score> playerScores = new HashMap<>(); // Scores des joueurs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);

        players = getIntent().getParcelableArrayListExtra("players");

        // Initialisation du bouton pour afficher le tableau des scores
        Button showScoreTableButton = findViewById(R.id.show_score_table_button);
        showScoreTableButton.setOnClickListener(v -> toggleScoreTableVisibility());

        // Initialiser le TextView du joueur à qui est le tour
        currentPlayerText = findViewById(R.id.current_player_text);

        // Initialisation des ImageView pour chaque dé
        diceImages[0] = findViewById(R.id.dice1);
        diceImages[1] = findViewById(R.id.dice2);
        diceImages[2] = findViewById(R.id.dice3);
        diceImages[3] = findViewById(R.id.dice4);
        diceImages[4] = findViewById(R.id.dice5);

        // Initialisation du TextView pour les lancers restants
        rollsRemainingTextView = findViewById(R.id.rolls_remaining_text);
        updateRollsRemainingText(); // Met à jour le texte affichant le nombre de lancers restants

        // Initialisation du layout pour les dés gardés
        lockedDiceLayout = findViewById(R.id.locked_dice_layout);

        // Initialisation du layout pour le tableau des scores
        scoreTableLayout = findViewById(R.id.score_table_layout);

        // Initialisation du bouton de lancement
        Button rollButton = findViewById(R.id.roll_button);
        rollButton.setOnClickListener(view -> rollDice());

        // Initialisation des scores pour chaque joueur
        for (Player player : players) {
            playerScores.put(player, new Score()); // Initialise un score pour chaque joueur
        }

        // Permettre au joueur de cliquer sur les dés pour les garder
        for (int i = 0; i < diceImages.length; i++) {
            int finalI = i;
            diceImages[i].setOnClickListener(v -> toggleLockDice(finalI)); // Toggle pour garder ou relancer un dé
        }

        updateUI(); // Met à jour l'interface utilisateur
    }

    // Méthode pour afficher ou masquer le tableau des scores
    private void toggleScoreTableVisibility() {
        if (scoreTableLayout.getVisibility() == View.GONE) {
            showScoreTable();
        } else {
            scoreTableLayout.setVisibility(View.GONE);
        }
    }

    // Méthode pour gérer le lancer des dés
    private void rollDice() {
        if (rollsRemaining > 0) { // S'assurer qu'il reste des lancers disponibles
            for (int i = 0; i < 5; i++) {
                if (!diceLocked[i]) { // Ne relance pas les dés bloqués
                    diceImages[i].setImageResource(R.drawable.animrolling); // Définir l'animation comme l'image du dé
                    diceAnimations[i] = (AnimationDrawable) diceImages[i].getDrawable(); // Utiliser getDrawable ici
                    diceAnimations[i].start();
                }
            }

            // Arrêter l'animation après 2 secondes et mettre à jour les valeurs des dés
            diceImages[0].postDelayed(() -> {
                for (int i = 0; i < 5; i++) {
                    if (!diceLocked[i]) { // Ne change pas les dés bloqués
                        diceAnimations[i].stop(); // Arrête l'animation
                        diceValues[i] = random.nextInt(6) + 1; // Génère une nouvelle valeur aléatoire
                        updateDiceImage(i); // Met à jour l'image du dé avec la nouvelle valeur
                    }
                }
                rollsRemaining--; // Décrémente le nombre de lancers restants
                updateRollsRemainingText(); // Met à jour l'affichage des lancers restants

                if (rollsRemaining == 0) {
                    showScoreTable(); // Affiche le tableau des scores après les lancers
                }
            }, 2000); // Animation pendant 2 secondes
        }
    }

    // Méthode pour mettre à jour l'image des dés après chaque lancer
    private void updateDiceImage(int index) {
        int drawableResource = getResources().getIdentifier("dice_" + diceValues[index], "drawable", getPackageName());
        diceImages[index].setImageResource(drawableResource);
    }

    // Méthode pour verrouiller ou déverrouiller un dé
    private void toggleLockDice(int index) {
        diceLocked[index] = !diceLocked[index]; // Inverse l'état du dé (gardé ou non)
        if (diceLocked[index]) {
            // Déplacer le dé dans la section des dés gardés en bas
            lockedDiceLayout.addView(diceImages[index]);
            diceImages[index].setAlpha(0.5f); // Diminue l'opacité pour indiquer que le dé est gardé
        } else {
            // Remettre le dé dans sa position d'origine et restaurer l'opacité
            restoreDiceToOriginalPosition(index);
            diceImages[index].setAlpha(1f); // Rétablit l'opacité normale pour indiquer que le dé sera relancé
        }
    }

    // Méthode pour restaurer un dé à sa position d'origine si déverrouillé
    private void restoreDiceToOriginalPosition(int index) {
        // Si le dé est dans la première ligne (les deux premiers dés)
        if (index < 2) {
            LinearLayout lineTwoDice = findViewById(R.id.line_two_dice);
            lineTwoDice.addView(diceImages[index]); // Replace dans la première ligne
        } else {
            // Si le dé est dans la deuxième ligne (les trois derniers dés)
            LinearLayout lineThreeDice = findViewById(R.id.line_three_dice);
            lineThreeDice.addView(diceImages[index]); // Replace dans la deuxième ligne
        }
    }

    // Méthode pour mettre à jour l'affichage du nombre de lancers restants
    private void updateRollsRemainingText() {
        rollsRemainingTextView.setText(getString(R.string.rolls_remaining, rollsRemaining));
    }

    // Méthode pour afficher le tableau des scores
    private void showScoreTable() {
        scoreTableLayout.setVisibility(View.VISIBLE);
        LinearLayout scoreTableContent = findViewById(R.id.score_table_content);
        scoreTableContent.removeAllViews(); // Vider le tableau avant de le remplir

        // Ajout de la première ligne pour les noms des joueurs
        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView figureLabel = new TextView(this);
        figureLabel.setText("Figure");
        figureLabel.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        headerRow.addView(figureLabel);

        // Affichage des noms des joueurs en haut du tableau
        for (Player player : players) {
            TextView playerNameTextView = new TextView(this);
            playerNameTextView.setText(player.getName());
            playerNameTextView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            headerRow.addView(playerNameTextView);
        }

        scoreTableContent.addView(headerRow);

        addScoreRowForAllPlayers(getString(R.string.score_one), player -> playerScores.get(player).ones, 1);
        addScoreRowForAllPlayers(getString(R.string.score_two), player -> playerScores.get(player).twos, 2);
        addScoreRowForAllPlayers(getString(R.string.score_three), player -> playerScores.get(player).threes, 3);
        addScoreRowForAllPlayers(getString(R.string.score_four), player -> playerScores.get(player).fours, 4);
        addScoreRowForAllPlayers(getString(R.string.score_five), player -> playerScores.get(player).fives, 5);
        addScoreRowForAllPlayers(getString(R.string.score_six), player -> playerScores.get(player).sixes, 6);

        addScoreRowForAllPlayers(getString(R.string.total_minor), player -> playerScores.get(player).calculateMinorTotal(), null);
        addScoreRowForAllPlayers(getString(R.string.bonus), player -> playerScores.get(player).calculateBonusMinor(), null);

        addScoreRowForAllPlayers(getString(R.string.brelan), player -> playerScores.get(player).brelan, "brelan");
        addScoreRowForAllPlayers(getString(R.string.carre), player -> playerScores.get(player).carre, "carre");
        addScoreRowForAllPlayers(getString(R.string.full), player -> playerScores.get(player).full, "full");
        addScoreRowForAllPlayers(getString(R.string.petite_suite), player -> playerScores.get(player).petiteSuite, "petite_suite");
        addScoreRowForAllPlayers(getString(R.string.grande_suite), player -> playerScores.get(player).grandeSuite, "grande_suite");
        addScoreRowForAllPlayers(getString(R.string.yams), player -> playerScores.get(player).yams, "yams");
        addScoreRowForAllPlayers(getString(R.string.chance), player -> playerScores.get(player).chance, "chance");

        addScoreRowForAllPlayers(getString(R.string.total_general), player -> playerScores.get(player).calculateTotal(), null);
    }

    // Ajout d'une ligne dans le tableau des scores
    private void addScoreRowForAllPlayers(String label, Function<Player, Integer> getScoreForPlayer, Object actionParam) {
        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView labelTextView = new TextView(this);
        labelTextView.setText(label);
        labelTextView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        rowLayout.addView(labelTextView);

        // Ajouter une colonne pour chaque joueur avec son score
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            TextView scoreTextView = new TextView(this);
            Integer score = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                score = getScoreForPlayer.apply(player);
            }
            scoreTextView.setText(score == -1 ? " " : String.valueOf(score)); // Affiche le score ou vide si non rempli
            scoreTextView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            rowLayout.addView(scoreTextView);

            // Permettre uniquement au joueur actif de remplir la case
            if (score == -1 && actionParam != null && i == currentPlayerIndex) {
                rowLayout.setOnClickListener(v -> {
                    if (actionParam instanceof Integer) {
                        fillScoreForMinor(player, (Integer) actionParam);
                    } else if (actionParam instanceof String) {
                        fillScoreForMajor(player, (String) actionParam);
                    }
                    scoreTableLayout.setVisibility(View.GONE); // Cacher le tableau après avoir rempli une ligne
                    passTurnToNextPlayer(); // Passer au joueur suivant
                });
            }
        }

        LinearLayout scoreTableContent = findViewById(R.id.score_table_content);
        scoreTableContent.addView(rowLayout);
    }

    // Remplir une figure mineure (1 à 6)
    private void fillScoreForMinor(Player player, int diceNumber) {
        Score playerScore = playerScores.get(player);
        int sum = calculateSumForDice(diceNumber);
        switch (diceNumber) {
            case 1: playerScore.ones = sum; break;
            case 2: playerScore.twos = sum; break;
            case 3: playerScore.threes = sum; break;
            case 4: playerScore.fours = sum; break;
            case 5: playerScore.fives = sum; break;
            case 6: playerScore.sixes = sum; break;
        }
    }

    // Remplir une figure majeure (Brelan, Carré, Full, etc.)
    private void fillScoreForMajor(Player player, String majorType) {
        Score playerScore = playerScores.get(player);
        switch (majorType) {
            case "brelan": playerScore.brelan = calculateBrelan(); break;
            case "carre": playerScore.carre = calculateCarre(); break;
            case "full": playerScore.full = calculateFull(); break;
            case "petite_suite": playerScore.petiteSuite = calculatePetiteSuite(); break;
            case "grande_suite": playerScore.grandeSuite = calculateGrandeSuite(); break;
            case "yams": playerScore.yams = calculateYams(); break;
            case "chance": playerScore.chance = calculateChance(); break;
        }
    }

    // Calculer la somme pour une figure mineure (1 à 6)
    private int calculateSumForDice(int diceNumber) {
        int sum = 0;
        for (int value : diceValues) {
            if (value == diceNumber) {
                sum += value;
            }
        }
        return sum;
    }

    // Calculs pour les figures majeures (brelan, carré, etc.)
    private int calculateBrelan() {
        // Logique pour calculer un brelan
        return 0;
    }

    private int calculateCarre() {
        // Logique pour calculer un carré
        return 0;
    }

    private int calculateFull() {
        // Logique pour calculer un full
        return 0;
    }

    private int calculatePetiteSuite() {
        // Logique pour calculer une petite suite
        return 0;
    }

    private int calculateGrandeSuite() {
        // Logique pour calculer une grande suite
        return 0;
    }

    private int calculateYams() {
        // Logique pour calculer un Yam's
        return 0;
    }

    private int calculateChance() {
        int sum = 0;
        for (int value : diceValues) {
            sum += value;
        }
        return sum;
    }

    private void resetDice() {
        // Réinitialise l'état des dés (tous non gardés)
        for (int i = 0; i < diceLocked.length; i++) {
            diceLocked[i] = false;
            diceImages[i].setAlpha(1f); // Rétablit l'opacité normale
            restoreDiceToOriginalPosition(i); // Replace le dé dans sa position d'origine
        }

        // Réinitialise les lancers restants à 3
        rollsRemaining = 3;
        updateRollsRemainingText(); // Met à jour l'affichage des lancers restants
    }

    // Passer au joueur suivant
    private void passTurnToNextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size(); // Passer au joueur suivant
        rollsRemaining = 3; // Réinitialiser les lancers restants
        resetDice(); // Réinitialiser les dés
        updateUI(); // Mettre à jour l'interface utilisateur
    }

    private void updateUI() {
        // Mettre à jour l'interface utilisateur (indicateur de joueur, etc.)
        Player currentPlayer = players.get(currentPlayerIndex);
        currentPlayerText.setText(getString(R.string.show_turn, currentPlayer.getName()));
        if (currentPlayer.isIa()) {
            // Si le joueur est une IA, lancer automatiquement les dés
            rollDice();
        }
    }
}