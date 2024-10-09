package com.limayrac.yams;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
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
    private Button rollButton; // Button pour lancé les dés avec décompte
    private int[] diceValues = new int[5]; // Valeurs des dés après chaque lancer
    private boolean[] diceLocked = new boolean[5]; // Garde trace des dés à garder
    private ImageView[] diceImages = new ImageView[5]; // Références aux ImageView pour chaque dé
    private Random random = new Random(); // Générateur de nombres aléatoires
    private AnimationDrawable[] diceAnimations = new AnimationDrawable[5]; // Animations pour chaque dé
    private int rollsRemaining = 3; // Nombre de lancers restants (max 3)
    private TextView rollsRemainingTextView; // TextView pour afficher les lancers restants
    private TextView lockedDiceSumTextView; // TextView pour afficher la somme des dés gardés
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

        // Initialiser les éléments d'interface
        Button pauseButton = findViewById(R.id.pause_button);
        pauseButton.setOnClickListener(v -> showPauseDialog());

        // Initialiser le TextView du joueur à qui est le tour
        currentPlayerText = findViewById(R.id.current_player_text);

        // Initialisation des ImageView pour chaque dé
        diceImages[0] = findViewById(R.id.dice1);
        diceImages[1] = findViewById(R.id.dice2);
        diceImages[2] = findViewById(R.id.dice3);
        diceImages[3] = findViewById(R.id.dice4);
        diceImages[4] = findViewById(R.id.dice5);

        // Initialisation des dés : les rendre invisibles et désactiver les interactions
        for (ImageView dice : diceImages) {
            dice.setVisibility(View.INVISIBLE); // Désactiver la visibilité
            dice.setClickable(false); // Désactiver les clics
            dice.setEnabled(false); // Désactiver l'état activé
        }

        // Initialisation du bouton de lancement des dés
        rollButton = findViewById(R.id.roll_button);
        rollButton.setOnClickListener(view -> rollDice());

        // Initialisation du TextView pour les lancers restants
        rollsRemainingTextView = findViewById(R.id.rolls_remaining_text);
        updateRollsRemainingText(); // Met à jour le texte affichant le nombre de lancers restants

        // Initialisation du TextView pour afficher la somme des dés gardés
        lockedDiceSumTextView = findViewById(R.id.locked_dice_sum_text);
        updateLockedDiceSum(); // Met à jour la somme des dés gardés après chaque action

        // Initialisation du layout pour les dés gardés
        lockedDiceLayout = findViewById(R.id.locked_dice_layout_container);

        // Initialisation du layout pour le tableau des scores
        scoreTableLayout = findViewById(R.id.score_table_layout);

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

    // Méthode pour afficher la boîte de dialogue de pause
    private void showPauseDialog() {
        // Créer une boîte de dialogue pour mettre en pause ou quitter
        new AlertDialog.Builder(this)
                .setTitle(R.string.pause_menu_title)
                .setMessage(R.string.pause_menu_message)
                .setPositiveButton(R.string.resume, (dialog, which) -> dialog.dismiss()) // Reprendre la partie
                .setNegativeButton(R.string.quit, (dialog, which) -> {
                    Intent intent = new Intent(GameActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Fermer l'activité actuelle
                })
                .setCancelable(false) // Empêche la fermeture de la boîte de dialogue en dehors des boutons
                .show();
    }

    // Méthode pour désactiver les interactions sur les dés
    private void disableDiceInteraction() {
        for (ImageView dice : diceImages) {
            dice.setClickable(false);
        }
    }

    // Méthode pour réactiver les interactions sur les dés
    private void enableDiceInteraction() {
        for (ImageView dice : diceImages) {
            dice.setClickable(true);
        }
    }

    // Méthode pour gérer le lancer des dés
    private void rollDice() {
        if (rollsRemaining > 0) { // S'assurer qu'il reste des lancers disponibles
            disableDiceInteraction(); // Désactive les interactions avec les dés

            // Rendre les dés visibles et actifs après le premier lancer
            for (ImageView dice : diceImages) {
                dice.setVisibility(View.VISIBLE); // Les rendre visibles
                dice.setEnabled(true); // Les rendre activables
                dice.setClickable(true); // Les rendre cliquables
            }

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
                updateLockedDiceSum(); // Met à jour la somme des dés
                checkForCombinations(); // Vérifie les combinaisons majeures et mineures possibles
                enableDiceInteraction(); // Réactive les interactions avec les dés après l'animation

                if (rollsRemaining == 0) {
                    new Handler().postDelayed(() -> showScoreTable(), 2000); // 1 secondes Affiche le tableau des scores après les lancers
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
        // Vérifier si l'animation du dé est terminée avant de permettre le verrouillage
        if (diceImages[index].getVisibility() != View.VISIBLE || diceAnimations[index] != null && diceAnimations[index].isRunning()) {
            // Si l'animation est encore en cours, ne pas permettre de verrouiller le dé
            return;
        }

        diceLocked[index] = !diceLocked[index]; // Inverse l'état du dé (gardé ou non)

        if (diceLocked[index]) {
            // Vérifiez si le dé a déjà un parent, et s'il en a un, retirez-le de son parent
            if (diceImages[index].getParent() != null) {
                ((ViewGroup) diceImages[index].getParent()).removeView(diceImages[index]);
            }

            // Ajouter le dé dans les lignes de dés verrouillés
            if (index < 2) {
                LinearLayout lineOne = findViewById(R.id.locked_dice_line_one);
                lineOne.addView(diceImages[index]);
            } else {
                LinearLayout lineTwo = findViewById(R.id.locked_dice_line_two);
                lineTwo.addView(diceImages[index]);
            }

            diceImages[index].setAlpha(0.5f); // Diminue l'opacité pour indiquer que le dé est gardé
        } else {
            // Remettre le dé dans sa position d'origine et restaurer l'opacité
            restoreDiceToOriginalPosition(index);
            diceImages[index].setAlpha(1f); // Rétablit l'opacité normale pour indiquer que le dé sera relancé
        }

        updateLockedDiceSum(); // Met à jour la somme des dés gardés après chaque modification
        checkForCombinations(); // Vérifie les combinaisons majeures et mineures possibles
    }

    // Méthode pour restaurer un dé à sa position d'origine si déverrouillé
    private void restoreDiceToOriginalPosition(int index) {
        // Vérifiez si le dé a déjà un parent, et s'il en a un, retirez-le de son parent
        if (diceImages[index].getParent() != null) {
            ((ViewGroup) diceImages[index].getParent()).removeView(diceImages[index]);
        }

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

    // Méthode pour calculer et afficher la somme des dés gardés
    private void updateLockedDiceSum() {
        lockedDiceSumTextView.setText(getString(R.string.locked_dice_sum, calculateSumForLockedDice())); // Met à jour l'affichage avec la somme des dés gardés
    }

    // Méthode pour mettre à jour l'affichage du nombre de lancers restants
    private void updateRollsRemainingText() {
        rollsRemainingTextView.setText(getString(R.string.rolls_remaining, rollsRemaining));
        rollButton.setText(getString(R.string.roll, rollsRemaining)); // Mise à jour du bouton avec les lancers restants
    }

    private void checkForCombinations() {
        LinearLayout scoreOptionsLayout = findViewById(R.id.score_options_layout);
        scoreOptionsLayout.removeAllViews(); // Supprimer les anciens boutons avant de les regénérer

        // Vérifier les combinaisons majeures et mineures

        // Utilisation de la méthode pour compter le nombre de dés verrouillés pour chaque valeur
        int ones = countLockedDiceWithValue(1);
        int twos = countLockedDiceWithValue(2)*2;
        int threes = countLockedDiceWithValue(3)*3;
        int fours = countLockedDiceWithValue(4)*4;
        int fives = countLockedDiceWithValue(5)*5;
        int sixes = countLockedDiceWithValue(6)*6;

        int brelan = calculateBrelan();
        int carre = calculateCarre();
        int full = calculateFull();
        int petiteSuite = calculatePetiteSuite();
        int grandeSuite = calculateGrandeSuite();
        int yams = calculateYams();

        // Ajouter des boutons pour chaque combinaison trouvée en utilisant les ressources de chaîne
        if (ones > 0) addScoreOptionButton(scoreOptionsLayout, getString(R.string.score_one), ones);
        if (twos > 0) addScoreOptionButton(scoreOptionsLayout, getString(R.string.score_two), twos);
        if (threes > 0) addScoreOptionButton(scoreOptionsLayout, getString(R.string.score_three), threes);
        if (fours > 0) addScoreOptionButton(scoreOptionsLayout, getString(R.string.score_four), fours);
        if (fives > 0) addScoreOptionButton(scoreOptionsLayout, getString(R.string.score_five), fives);
        if (sixes > 0) addScoreOptionButton(scoreOptionsLayout, getString(R.string.score_six), sixes);
        if (brelan > 0) addScoreOptionButton(scoreOptionsLayout, getString(R.string.brelan), brelan);
        if (carre > 0) addScoreOptionButton(scoreOptionsLayout, getString(R.string.carre), carre);
        if (full > 0) addScoreOptionButton(scoreOptionsLayout, getString(R.string.full), full);
        if (petiteSuite > 0) addScoreOptionButton(scoreOptionsLayout, getString(R.string.petite_suite), petiteSuite);
        if (grandeSuite > 0) addScoreOptionButton(scoreOptionsLayout, getString(R.string.grande_suite), grandeSuite);
        if (yams > 0) addScoreOptionButton(scoreOptionsLayout, getString(R.string.yams), yams);
    }

    private void addScoreOptionButton(LinearLayout layout, String text, int score) {
        Button scoreButton = new Button(this);
        scoreButton.setText(text + " : " + score + " points");
        layout.addView(scoreButton);

        // Logique pour sélectionner cette option et attribuer le score
        scoreButton.setOnClickListener(v -> {
            fillScoreForCurrentPlayer(text, score);
            layout.removeAllViews(); // Supprimer les options après sélection
            passTurnToNextPlayer(); // Passer au joueur suivant
            showScoreTable(); // Affiche les scores
            disableDiceInteraction(); // Désactive l'intéraction des dés
            updateLockedDiceSum(); // Met à jour la somme des dés gardés après chaque modification
        });
    }

    private void fillScoreForCurrentPlayer(String figure, int score) {
        Score playerScore = playerScores.get(players.get(currentPlayerIndex));

        // Comparer les figures en utilisant les ressources de chaîne
        if (figure.equals(getString(R.string.score_one))) {
            playerScore.ones = score;
        } else if (figure.equals(getString(R.string.score_two))) {
            playerScore.twos = score;
        } else if (figure.equals(getString(R.string.score_three))) {
            playerScore.threes = score;
        } else if (figure.equals(getString(R.string.score_four))) {
            playerScore.fours = score;
        } else if (figure.equals(getString(R.string.score_five))) {
            playerScore.fives = score;
        } else if (figure.equals(getString(R.string.score_six))) {
            playerScore.sixes = score;
        } else if (figure.equals(getString(R.string.brelan))) {
            playerScore.brelan = score;
        } else if (figure.equals(getString(R.string.carre))) {
            playerScore.carre = score;
        } else if (figure.equals(getString(R.string.full))) {
            playerScore.full = score;
        } else if (figure.equals(getString(R.string.petite_suite))) {
            playerScore.petiteSuite = score;
        } else if (figure.equals(getString(R.string.grande_suite))) {
            playerScore.grandeSuite = score;
        } else if (figure.equals(getString(R.string.yams))) {
            playerScore.yams = score;
        }
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
//            if (score == -1 && actionParam != null && i == currentPlayerIndex) {
//                rowLayout.setOnClickListener(v -> {
//                    if (actionParam instanceof Integer) {
//                        fillScoreForMinor(player, (Integer) actionParam);
//                    } else if (actionParam instanceof String) {
//                        fillScoreForMajor(player, (String) actionParam);
//                    }
//                    scoreTableLayout.setVisibility(View.GONE); // Cacher le tableau après avoir rempli une ligne
//                    passTurnToNextPlayer(); // Passer au joueur suivant
//                });
//            }
        }

        LinearLayout scoreTableContent = findViewById(R.id.score_table_content);
        scoreTableContent.addView(rowLayout);
    }

    // Remplir une figure mineure (1 à 6)
    private void fillScoreForMinor(Player player, int diceNumber) {
        Score playerScore = playerScores.get(player);
        int sum = calculateSumForLockedDice();
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

    private int calculateSumForLockedDice() {
        int sum = 0;
        for (int i = 0; i < diceValues.length; i++) {
            if (diceLocked[i]) {
                sum += diceValues[i];
            }
        }
        return sum;
    }

    private int countLockedDiceWithValue(int diceValue) {
        int count = 0;
        for (int i = 0; i < diceValues.length; i++) {
            if (diceLocked[i] && diceValues[i] == diceValue) {
                count++;
            }
        }
        return count;
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