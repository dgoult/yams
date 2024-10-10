package com.limayrac.yams;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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

    // Variable pour stocker le dernier coup joué
    private String lastScoreFigure = null;
    private Player lastPlayer = null;

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
                .setCancelable(false) // Ne permet pas la fermeture de la boîte de dialogue en dehors des boutons
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
        rollButton.setClickable(true);
    }

    private void disableDiceVisibility() {
        for (ImageView dice : diceImages) {
            dice.setVisibility(View.INVISIBLE);
        }
    }

    private void enableDiceVisibility() {
        for (ImageView dice : diceImages) {
            dice.setVisibility(View.VISIBLE);
        }
    }

    private void disableRollInteraction() {
        rollButton.setClickable(false);
    }

    private void enableRollInteraction() {
        rollButton.setClickable(true);
    }

    private void disableSetScoreButtonInteraction() {
        rollButton.setClickable(false);
    }

    private void enableSetScoreButtonInteraction() {
        rollButton.setClickable(true);
    }

    // Méthode pour gérer le lancer des dés
    private void rollDice() {
        Player currentPlayer = players.get(currentPlayerIndex);

        if (rollsRemaining > 0) { // S'assurer qu'il reste des lancers disponibles
                // Rendre les dés visibles et actifs après le premier lancer
                for (ImageView dice : diceImages) {
                    dice.setVisibility(View.VISIBLE); // Les rendre visibles

                    if (!currentPlayer.isIa()) {
                        dice.setEnabled(true); // Les rendre activables
                        dice.setClickable(true); // Les rendre cliquables
                    }
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

                if (!currentPlayer.isIa()) {
                    checkForCombinations(); // Vérifie les combinaisons majeures et mineures possibles
                    enableDiceInteraction(); // Réactive les interactions avec les dés après l'animation
                    enableRollInteraction(); // Réactive les interactions avec le bouton de lancer après l'animation
                }

//                if (rollsRemaining == 0) {
//                    new Handler().postDelayed(() -> showScoreTable(), 2000); // 1 secondes Affiche le tableau des scores après les lancers
//                }
            }, 500); // Animation pendant 2 secondes
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

        Score currentPlayerScore = playerScores.get(players.get(currentPlayerIndex));

        // Variables pour vérifier si au moins une combinaison est possible
        boolean hasCombination = false;

        // Vérifier les combinaisons mineures et ajouter des boutons s'ils ne sont pas déjà remplis
        int ones = countLockedDiceWithValue(1);
        int twos = countLockedDiceWithValue(2) * 2;
        int threes = countLockedDiceWithValue(3) * 3;
        int fours = countLockedDiceWithValue(4) * 4;
        int fives = countLockedDiceWithValue(5) * 5;
        int sixes = countLockedDiceWithValue(6) * 6;

        if (currentPlayerScore.ones == -1 && ones > 0) {
            addScoreOptionButton(scoreOptionsLayout, getString(R.string.score_one), ones);
            hasCombination = true;
        }
        if (currentPlayerScore.twos == -1 && twos > 0) {
            addScoreOptionButton(scoreOptionsLayout, getString(R.string.score_two), twos);
            hasCombination = true;
        }
        if (currentPlayerScore.threes == -1 && threes > 0) {
            addScoreOptionButton(scoreOptionsLayout, getString(R.string.score_three), threes);
            hasCombination = true;
        }
        if (currentPlayerScore.fours == -1 && fours > 0) {
            addScoreOptionButton(scoreOptionsLayout, getString(R.string.score_four), fours);
            hasCombination = true;
        }
        if (currentPlayerScore.fives == -1 && fives > 0) {
            addScoreOptionButton(scoreOptionsLayout, getString(R.string.score_five), fives);
            hasCombination = true;
        }
        if (currentPlayerScore.sixes == -1 && sixes > 0) {
            addScoreOptionButton(scoreOptionsLayout, getString(R.string.score_six), sixes);
            hasCombination = true;
        }

        // Vérifier les combinaisons majeures
        int brelan = calculateBrelan();
        int carre = calculateCarre();
        int full = calculateFull();
        int petiteSuite = calculatePetiteSuite();
        int grandeSuite = calculateGrandeSuite();
        int yams = calculateYams();
        int chance = calculateChance();

        if (currentPlayerScore.brelan == -1 && brelan > 0) {
            addScoreOptionButton(scoreOptionsLayout, getString(R.string.brelan), brelan);
            hasCombination = true;
        }
        if (currentPlayerScore.carre == -1 && carre > 0) {
            addScoreOptionButton(scoreOptionsLayout, getString(R.string.carre), carre);
            hasCombination = true;
        }
        if (currentPlayerScore.full == -1 && full > 0) {
            addScoreOptionButton(scoreOptionsLayout, getString(R.string.full), full);
            hasCombination = true;
        }
        if (currentPlayerScore.petiteSuite == -1 && petiteSuite > 0) {
            addScoreOptionButton(scoreOptionsLayout, getString(R.string.petite_suite), petiteSuite);
            hasCombination = true;
        }
        if (currentPlayerScore.grandeSuite == -1 && grandeSuite > 0) {
            addScoreOptionButton(scoreOptionsLayout, getString(R.string.grande_suite), grandeSuite);
            hasCombination = true;
        }
        if (currentPlayerScore.yams == -1 && yams > 0) {
            addScoreOptionButton(scoreOptionsLayout, getString(R.string.yams), yams);
            hasCombination = true;
        }
        if (currentPlayerScore.chance == -1 && chance > 0) {
            addScoreOptionButton(scoreOptionsLayout, getString(R.string.chance), chance);
            hasCombination = true;
        }

        // Si aucune combinaison n'est possible, ajouter l'option de barrer une figure
        if (!hasCombination) {
            addBarFigureOption(scoreOptionsLayout);
        }
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
        });
    }

    private void fillScoreForCurrentPlayer(String figure, int score) {
        Score playerScore = playerScores.get(players.get(currentPlayerIndex));

        // Enregistrer la figure et le joueur qui ont marqué le dernier coup
        lastScoreFigure = figure;
        lastPlayer = players.get(currentPlayerIndex);

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
        } else if (figure.equals(getString(R.string.chance))) {
            playerScore.chance = score;
        }
    }

    private void addBarFigureOption(LinearLayout layout) {
        Button barButton = new Button(this);
        barButton.setText(getString(R.string.bar_figure));
        barButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light)); // Mettre le bouton en rouge
        layout.addView(barButton);

        // Lorsque le joueur clique sur le bouton, lui proposer de barrer une figure
        barButton.setOnClickListener(v -> {
            showBarFigureDialog(layout, barButton);
            barButton.setClickable(false);
        });
    }

    private void showBarFigureDialog(LinearLayout layout, Button barButton) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.choose_figure_to_bar);

        String[] figures = getAvailableFiguresToBar(); // Récupérer les figures disponibles
        builder.setItems(figures, (dialog, which) -> {
            String selectedFigure = figures[which];
            barFigure(selectedFigure); // Marquer la figure comme barrée
            layout.removeAllViews(); // Supprimer les options après sélection
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) ->
        {
            dialog.dismiss();
            barButton.setClickable(true);
        });
        builder.setCancelable(false);
        builder.show();
    }

    private String[] getAvailableFiguresToBar() {
        ArrayList<String> availableFigures = new ArrayList<>();
        Score currentPlayerScore = playerScores.get(players.get(currentPlayerIndex));

        if (currentPlayerScore.ones == -1) availableFigures.add(getString(R.string.score_one));
        if (currentPlayerScore.twos == -1) availableFigures.add(getString(R.string.score_two));
        if (currentPlayerScore.threes == -1) availableFigures.add(getString(R.string.score_three));
        if (currentPlayerScore.fours == -1) availableFigures.add(getString(R.string.score_four));
        if (currentPlayerScore.fives == -1) availableFigures.add(getString(R.string.score_five));
        if (currentPlayerScore.sixes == -1) availableFigures.add(getString(R.string.score_six));

        if (currentPlayerScore.brelan == -1) availableFigures.add(getString(R.string.brelan));
        if (currentPlayerScore.carre == -1) availableFigures.add(getString(R.string.carre));
        if (currentPlayerScore.full == -1) availableFigures.add(getString(R.string.full));
        if (currentPlayerScore.petiteSuite == -1) availableFigures.add(getString(R.string.petite_suite));
        if (currentPlayerScore.grandeSuite == -1) availableFigures.add(getString(R.string.grande_suite));
        if (currentPlayerScore.yams == -1) availableFigures.add(getString(R.string.yams));
        if (currentPlayerScore.chance == -1) availableFigures.add(getString(R.string.chance));

        return availableFigures.toArray(new String[0]);
    }

    private void barFigure(String figure) {
        Score currentPlayerScore = playerScores.get(players.get(currentPlayerIndex));

        if (figure.equals(getString(R.string.score_one))) {
            currentPlayerScore.ones = 0;
        } else if (figure.equals(getString(R.string.score_two))) {
            currentPlayerScore.twos = 0;
        } else if (figure.equals(getString(R.string.score_three))) {
            currentPlayerScore.threes = 0;
        } else if (figure.equals(getString(R.string.score_four))) {
            currentPlayerScore.fours = 0;
        } else if (figure.equals(getString(R.string.score_five))) {
            currentPlayerScore.fives = 0;
        } else if (figure.equals(getString(R.string.score_six))) {
            currentPlayerScore.sixes = 0;
        } else if (figure.equals(getString(R.string.brelan))) {
            currentPlayerScore.brelan = 0;
        } else if (figure.equals(getString(R.string.carre))) {
            currentPlayerScore.carre = 0;
        } else if (figure.equals(getString(R.string.full))) {
            currentPlayerScore.full = 0;
        } else if (figure.equals(getString(R.string.petite_suite))) {
            currentPlayerScore.petiteSuite = 0;
        } else if (figure.equals(getString(R.string.grande_suite))) {
            currentPlayerScore.grandeSuite = 0;
        } else if (figure.equals(getString(R.string.yams))) {
            currentPlayerScore.yams = 0;
        } else if (figure.equals(getString(R.string.chance))) {
            currentPlayerScore.chance = 0;
        }

        // Passer au joueur suivant
        passTurnToNextPlayer();
        showScoreTable(); // Affiche les scores après avoir barré une figure
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

        // Texte pour la figure
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

            // Surligner le score si c'est le dernier coup joué
            if (label.equals(lastScoreFigure) && player.equals(lastPlayer)) {
                scoreTextView.setBackgroundColor(getResources().getColor(R.color.highlight_color)); // Couleur de surbrillance
            }

            rowLayout.addView(scoreTextView);
        }

        LinearLayout scoreTableContent = findViewById(R.id.score_table_content);
        scoreTableContent.addView(rowLayout);

        // Ajouter une ligne de séparation
        View separator = new View(this);
        separator.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        separator.setBackgroundColor(getResources().getColor(R.color.separator_color));
        scoreTableContent.addView(separator);
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

    private int calculateSumForDice() {
        int sum = 0;
        for (int i = 0; i < diceValues.length; i++) {
            sum += diceValues[i];
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

    private int calculateBrelan() {
        for (int i = 1; i <= 6; i++) {
            if (countLockedDiceWithValue(i) >= 3) {
                return calculateSumForDice(); // Somme des 5 dés
            }
        }
        return 0;
    }

    private int calculateCarre() {
        for (int i = 1; i <= 6; i++) {
            if (countLockedDiceWithValue(i) >= 4) {
                return calculateSumForDice(); // Somme des 5 dés
            }
        }
        return 0;
    }

    private int calculateFull() {
        boolean hasBrelan = false;
        boolean hasPair = false;

        for (int i = 1; i <= 6; i++) {
            if (countLockedDiceWithValue(i) == 3) {
                hasBrelan = true;
            } else if (countLockedDiceWithValue(i) == 2) {
                hasPair = true;
            }
        }

        if (hasBrelan && hasPair) {
            return 25; // Full vaut toujours 25 points
        }

        return 0;
    }

    private int calculatePetiteSuite() {
        int[] values = {1, 2, 3, 4, 5, 6};
        if (checkSequence(4, values)) {
            return 30; // Petite suite vaut toujours 30 points
        }
        return 0;
    }

    private int calculateGrandeSuite() {
        int[] values = {1, 2, 3, 4, 5, 6};
        if (checkSequence(5, values)) {
            return 40; // Grande suite vaut toujours 40 points
        }
        return 0;
    }

    private int calculateYams() {
        for (int i = 1; i <= 6; i++) {
            if (countLockedDiceWithValue(i) == 5) {
                return 50; // Yam's vaut toujours 50 points
            }
        }
        return 0;
    }

    private int calculateChance() {
        return calculateSumForLockedDice(); // Somme des 5 dés
    }

    // Méthode utilitaire pour vérifier une suite
    private boolean checkSequence(int length, int[] values) {
        int count = 0;
        for (int value : values) {
            if (countLockedDiceWithValue(value) > 0) {
                count++;
                if (count == length) {
                    return true;
                }
            } else {
                count = 0;
            }
        }
        return false;
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

    private boolean isGameOver() {
        for (Player player : players) {
            Score score = playerScores.get(player);
            if (!score.isAllFiguresFilled()) {
                return false; // Si un joueur n'a pas rempli toutes ses figures, le jeu n'est pas terminé
            }
        }
        return true; // Si tous les joueurs ont rempli leurs figures, le jeu est terminé
    }

    // Passer au joueur suivant
    private void passTurnToNextPlayer() {
        Player currentPlayer = players.get(currentPlayerIndex);
        if (isGameOver()) {
            showGameOverDialog();
        } else {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size(); // Passer au joueur suivant
            rollsRemaining = 3; // Réinitialiser les lancers restants
            resetDice(); // Réinitialiser les dés
            if (currentPlayer.isIa()) {
                disableDiceVisibility();
            }
            updateUI(); // Mettre à jour l'interface utilisateur
            showScoreTable(); // Affiche les scores
            disableDiceInteraction(); // Désactive l'intéraction des dés
            updateLockedDiceSum(); // Met à jour la somme des dés gardés après chaque modification
        }
    }

    private void showGameOverDialog() {
        Player winner = getWinner();
        String message = getString(R.string.winner_message, winner.getName(), playerScores.get(winner).calculateTotal());

        new AlertDialog.Builder(this)
                .setTitle(R.string.game_over)
                .setMessage(message)
                .setNegativeButton(R.string.resume, (dialog, which) -> dialog.dismiss()) // Reprendre la partie
                .setPositiveButton(R.string.quit, (dialog, which) -> {
                    Intent intent = new Intent(GameActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Fermer l'activité actuelle
                })
                .setCancelable(true)
                .show();
    }

    private Player getWinner() {
        Player winner = players.get(0);
        int highestScore = playerScores.get(winner).calculateTotal();

        for (Player player : players) {
            int playerScore = playerScores.get(player).calculateTotal();
            if (playerScore > highestScore) {
                winner = player;
                highestScore = playerScore;
            }
        }
        return winner;
    }

    private void rollDiceForIa(Player player) {
        String easyLevel = getString(R.string.easy);
        String hardLevel = getString(R.string.hard);

        // Cacher le tableau des scores lorsque les dés sont lancés
        scoreTableLayout.setVisibility(View.GONE);

        // Désactiver les interactions pour le tour de l'IA
        disableDiceInteraction();
        disableRollInteraction();

        Log.d("GameActivity", "IA turn started. Difficulty: " + player.getDifficulty());

        if (easyLevel.equals(player.getDifficulty())) {
            Log.d("GameActivity", "Easy level IA - Rolling dice first time.");
            rollDice(); // Lancer une première fois
            new Handler().postDelayed(() -> {
                Log.d("GameActivity", "Easy level IA - Locking random dice.");
                lockRandomDice(); // Verrouiller des dés au hasard
                new Handler().postDelayed(() -> {
                    Log.d("GameActivity", "Easy level IA - Rolling dice second time.");
                    rollDice(); // Relancer les dés restants
                    new Handler().postDelayed(() -> {
                        Log.d("GameActivity", "Easy level IA - Locking random dice.");
                        lockRandomDice(); // Verrouiller des dés au hasard
                        new Handler().postDelayed(() -> {
                            Log.d("GameActivity", "Easy level IA - Choosing best combination.");
                            chooseBestCombinationEasyLevel(); // Choisir la meilleure combinaison après relance
                            new Handler().postDelayed(() -> {
                                Log.d("GameActivity", "Easy level IA - Passing turn to next player.");
                                passTurnToNextPlayer();
                            }, 2000);
                        }, 3000);
                    }, 3000); // Délai pour que la deuxième animation se termine
                }, 3000); // Délai pour que la première animation se termine
            }, 3000);
        }
        else if (hardLevel.equals(player.getDifficulty())) {
            Log.d("GameActivity", "Hard level IA - Rolling dice first time.");
            rollDice(); // Lancer une première fois
            new Handler().postDelayed(() -> {
                Log.d("GameActivity", "Hard level IA - Locking strategic dice.");
                lockStrategicDice(); // Verrouiller des dés stratégiques
                new Handler().postDelayed(() -> {
                    Log.d("GameActivity", "Hard level IA - Rolling dice second time.");
                    rollDice(); // Relancer les dés restants
                    new Handler().postDelayed(() -> {
                        Log.d("GameActivity", "Hard level IA - Locking strategic dice.");
                        lockStrategicDice(); // Verrouiller des dés stratégiques
                        new Handler().postDelayed(() -> {
                            Log.d("GameActivity", "Hard level IA - Rolling dice last time.");
                            rollDice(); // Dernier lancer de dés
                            new Handler().postDelayed(() -> {
                                Log.d("GameActivity", "Hard level IA - Locking all remaining dice.");
                                lockAllRemainingDice(); // Verrouiller tous les dés restants lors du dernier lancer
                                new Handler().postDelayed(() -> {
                                    Log.d("GameActivity", "Hard level IA - Choosing best combination.");
                                    chooseBestCombinationHardLevel(); // Choisir la meilleure combinaison après relance
                                    new Handler().postDelayed(() -> {
                                        Log.d("GameActivity", "Hard level IA - Passing turn to next player.");
                                        passTurnToNextPlayer();
                                    }, 2000);
                                }, 3000);
                            }, 3000); // Délai pour que l'animation se termine
                        }, 3000); // Délai pour que la deuxième animation se termine
                    }, 3000); // Délai pour que la deuxième animation se termine
                }, 3000); // Délai pour que la première animation se termine
            }, 3000);
        }
    }

    private void lockAllRemainingDice() {
        Log.d("GameActivity", "IA Difficile: Verrouillage de tous les dés restants.");
        for (int i = 0; i < diceValues.length; i++) {
            if (!diceLocked[i]) { // Verrouiller uniquement les dés non verrouillés
                final int index = i;
                diceLocked[i] = true; // Marquer le dé comme verrouillé
                new Handler().postDelayed(() -> {
                    Log.d("GameActivity", "Verrouillage du dé " + (index + 1) + " avec la valeur " + diceValues[index]);
                    moveDiceForIa(index); // Déplacer le dé
                }, i * 500); // Appliquer un délai pour le déplacement des dés
            }
        }
    }

    private void lockRandomDice() {
        Log.d("GameActivity", "IA Facile: Début du verrouillage aléatoire des dés.");
        Handler handler = new Handler();
        for (int i = 0; i < diceValues.length; i++) {
            // Vérifie si l'IA décide de verrouiller ce dé
            if (random.nextBoolean()) {
                final int index = i;
                handler.postDelayed(() -> {
                    Log.d("GameActivity", "IA Facile: Verrouille le dé " + (index + 1) + " avec la valeur " + diceValues[index]);
                    moveDiceForIa(index); // Déplace le dé progressivement
                }, i * 1000); // Appliquer un délai de 1 seconde entre chaque dé
            }
        }
    }

    private void lockStrategicDice() {
        Log.d("GameActivity", "IA Difficile: Début du verrouillage stratégique des dés.");

        // Tableau pour compter combien de fois chaque valeur de dé apparaît (1 à 6)
        int[] diceCounts = new int[6];

        // Compter combien de dés de chaque valeur sont présents
        for (int value : diceValues) {
            diceCounts[value - 1]++;
        }

        // Trouver la valeur ayant le plus grand nombre de dés
        int bestValueToLock = -1;
        int maxCount = 0;
        for (int i = 0; i < diceCounts.length; i++) {
            if (diceCounts[i] > maxCount) {
                maxCount = diceCounts[i];
                bestValueToLock = i + 1; // Ajuste pour les valeurs de dés (1-6)
            }
        }

        Handler handler = new Handler();
        boolean hasLockedNewDice = false;

        // Étape 1 : Verrouiller les dés ayant la valeur majoritaire (la plus fréquente)
        for (int i = 0; i < diceValues.length; i++) {
            if (!diceLocked[i] && diceValues[i] == bestValueToLock) {
                final int index = i;
                handler.postDelayed(() -> {
                    Log.d("GameActivity", "IA Difficile: Verrouille le dé " + (index + 1) + " avec la valeur majoritaire " + diceValues[index]);
                    moveDiceForIa(index);
                }, i * 1000);
                hasLockedNewDice = true;
            }
        }

        // Étape 2 : Si des dés non verrouillés ont la même valeur que des dés verrouillés, les verrouiller
        if (!hasLockedNewDice) {
            for (int i = 0; i < diceValues.length; i++) {
                if (!diceLocked[i]) {
                    // Cherche si la valeur du dé non verrouillé correspond à une valeur déjà verrouillée
                    for (int j = 0; j < diceValues.length; j++) {
                        if (diceLocked[j] && diceValues[i] == diceValues[j]) {
                            final int index = i;
                            handler.postDelayed(() -> {
                                Log.d("GameActivity", "IA Difficile: Verrouille le dé " + (index + 1) + " avec la même valeur que les dés déjà verrouillés (" + diceValues[index] + ")");
                                moveDiceForIa(index);
                            }, i * 1000);
                            hasLockedNewDice = true;
                            break; // Sortir de la boucle si un dé est verrouillé
                        }
                    }
                }
                if (hasLockedNewDice) {
                    break; // Si on a déjà verrouillé un dé, sortir de la boucle principale
                }
            }
        }

        // Étape 3 : Si aucun dé pertinent n'a été verrouillé, verrouiller le dé avec la valeur la plus élevée
        if (!hasLockedNewDice) {
            int highestValueIndex = -1;
            int highestValue = 0;

            for (int i = 0; i < diceValues.length; i++) {
                if (!diceLocked[i] && diceValues[i] > highestValue) {
                    highestValue = diceValues[i];
                    highestValueIndex = i;
                }
            }

            if (highestValueIndex != -1) {
                final int index = highestValueIndex;
                handler.postDelayed(() -> {
                    Log.d("GameActivity", "IA Difficile: Verrouille le dé " + (index + 1) + " avec la valeur la plus élevée " + diceValues[index]);
                    moveDiceForIa(index);
                }, 1000);
                hasLockedNewDice = true;
            }
        }

        // Étape 4 : Si aucun dé n'a été verrouillé, verrouiller un dé au hasard
        if (!hasLockedNewDice) {
            for (int i = 0; i < diceValues.length; i++) {
                if (!diceLocked[i]) {
                    final int index = i;
                    handler.postDelayed(() -> {
                        Log.d("GameActivity", "IA Difficile: Verrouille un dé au hasard " + (index + 1) + " avec la valeur " + diceValues[index]);
                        moveDiceForIa(index);
                    }, i * 1000);
                    break; // Verrouiller un seul dé au hasard
                }
            }
        }
    }

    private void moveDiceForIa(int index) {
        // Vérifier que le dé est visible et que l'animation est terminée
        if (diceImages[index].getVisibility() != View.VISIBLE || diceAnimations[index] != null && diceAnimations[index].isRunning()) {
            return; // Ne pas permettre le déplacement si l'animation est encore en cours
        }

        diceLocked[index] = true; // Verrouille le dé
        if (diceImages[index].getParent() != null) {
            ((ViewGroup) diceImages[index].getParent()).removeView(diceImages[index]); // Retire le dé de sa position actuelle
        }

        // Ajoute le dé dans la ligne correspondante pour les dés verrouillés
        if (index < 2) {
            LinearLayout lineOne = findViewById(R.id.locked_dice_line_one);
            lineOne.addView(diceImages[index]); // Ajouter dans la première ligne
        } else {
            LinearLayout lineTwo = findViewById(R.id.locked_dice_line_two);
            lineTwo.addView(diceImages[index]); // Ajouter dans la deuxième ligne
        }

        diceImages[index].setAlpha(0.5f); // Diminue l'opacité pour indiquer que le dé est verrouillé

        // Mettre à jour l'affichage des dés verrouillés
        updateLockedDiceSum();
    }

    private void chooseBestCombinationEasyLevel() {
        Score currentPlayerScore = playerScores.get(players.get(currentPlayerIndex));

        // Choisir la meilleure combinaison en fonction des dés verrouillés
        if (currentPlayerScore.ones == -1 && countLockedDiceWithValue(1) > 0) {
            fillScoreForCurrentPlayer(getString(R.string.score_one), countLockedDiceWithValue(1));
            Log.d("GameActivity", "IA: A choisi de marquer les " + countLockedDiceWithValue(1) + " points pour les '1'.");
        } else if (currentPlayerScore.twos == -1 && countLockedDiceWithValue(2) > 0) {
            fillScoreForCurrentPlayer(getString(R.string.score_two), countLockedDiceWithValue(2) * 2);
            Log.d("GameActivity", "IA: A choisi de marquer les " + countLockedDiceWithValue(2) + " points pour les '2'.");
        } else if (currentPlayerScore.threes == -1 && countLockedDiceWithValue(3) > 0) {
            fillScoreForCurrentPlayer(getString(R.string.score_three), countLockedDiceWithValue(3) * 3);
            Log.d("GameActivity", "IA: A choisi de marquer les " + countLockedDiceWithValue(3) + " points pour les '3'.");
        } else if (currentPlayerScore.fours == -1 && countLockedDiceWithValue(4) > 0) {
            fillScoreForCurrentPlayer(getString(R.string.score_four), countLockedDiceWithValue(4) * 4);
            Log.d("GameActivity", "IA: A choisi de marquer les " + countLockedDiceWithValue(4) + " points pour les '4'.");
        } else if (currentPlayerScore.fives == -1 && countLockedDiceWithValue(5) > 0) {
            fillScoreForCurrentPlayer(getString(R.string.score_five), countLockedDiceWithValue(5) * 5);
            Log.d("GameActivity", "IA: A choisi de marquer les " + countLockedDiceWithValue(5) + " points pour les '5'.");
        } else if (currentPlayerScore.sixes == -1 && countLockedDiceWithValue(6) > 0) {
            fillScoreForCurrentPlayer(getString(R.string.score_six), countLockedDiceWithValue(6) * 6);
            Log.d("GameActivity", "IA: A choisi de marquer les " + countLockedDiceWithValue(6) + " points pour les '6'.");
        }
        // Vérifier les combinaisons majeures
        else if (currentPlayerScore.brelan == -1 && calculateBrelan() > 0) {
            fillScoreForCurrentPlayer(getString(R.string.brelan), calculateBrelan());
            Log.d("GameActivity", "IA: A choisi de marquer " + calculateBrelan() + " points pour le brelan.");
        } else if (currentPlayerScore.carre == -1 && calculateCarre() > 0) {
            fillScoreForCurrentPlayer(getString(R.string.carre), calculateCarre());
            Log.d("GameActivity", "IA: A choisi de marquer " + calculateCarre() + " points pour le carré.");
        } else if (currentPlayerScore.full == -1 && calculateFull() > 0) {
            fillScoreForCurrentPlayer(getString(R.string.full), calculateFull());
            Log.d("GameActivity", "IA: A choisi de marquer " + calculateFull() + " points pour le full.");
        } else if (currentPlayerScore.petiteSuite == -1 && calculatePetiteSuite() > 0) {
            fillScoreForCurrentPlayer(getString(R.string.petite_suite), calculatePetiteSuite());
            Log.d("GameActivity", "IA: A choisi de marquer " + calculatePetiteSuite() + " points pour la petite suite.");
        } else if (currentPlayerScore.grandeSuite == -1 && calculateGrandeSuite() > 0) {
            fillScoreForCurrentPlayer(getString(R.string.grande_suite), calculateGrandeSuite());
            Log.d("GameActivity", "IA: A choisi de marquer " + calculateGrandeSuite() + " points pour la grande suite.");
        } else if (currentPlayerScore.yams == -1 && calculateYams() > 0) {
            fillScoreForCurrentPlayer(getString(R.string.yams), calculateYams());
            Log.d("GameActivity", "IA: A choisi de marquer " + calculateYams() + " points pour le Yam's.");
        } else if (currentPlayerScore.chance == -1) {
            fillScoreForCurrentPlayer(getString(R.string.chance), calculateChance());
            Log.d("GameActivity", "IA: A choisi de marquer " + calculateChance() + " points pour la chance.");
        }
        // Si aucune combinaison n'est trouvée, barrer une figure
        else {
            barFigureForIA();
        }
    }

    private void chooseBestCombinationHardLevel() {
        Score currentPlayerScore = playerScores.get(players.get(currentPlayerIndex));

        // Dictionnaire pour stocker les combinaisons possibles et leurs points
        HashMap<String, Integer> possibleScores = new HashMap<>();

        // Calculer les points pour chaque combinaison disponible et non utilisée
        if (currentPlayerScore.ones == -1 && countLockedDiceWithValue(1) > 0) {
            possibleScores.put(getString(R.string.score_one), countLockedDiceWithValue(1));
        }
        if (currentPlayerScore.twos == -1 && countLockedDiceWithValue(2) > 0) {
            possibleScores.put(getString(R.string.score_two), countLockedDiceWithValue(2) * 2);
        }
        if (currentPlayerScore.threes == -1 && countLockedDiceWithValue(3) > 0) {
            possibleScores.put(getString(R.string.score_three), countLockedDiceWithValue(3) * 3);
        }
        if (currentPlayerScore.fours == -1 && countLockedDiceWithValue(4) > 0) {
            possibleScores.put(getString(R.string.score_four), countLockedDiceWithValue(4) * 4);
        }
        if (currentPlayerScore.fives == -1 && countLockedDiceWithValue(5) > 0) {
            possibleScores.put(getString(R.string.score_five), countLockedDiceWithValue(5) * 5);
        }
        if (currentPlayerScore.sixes == -1 && countLockedDiceWithValue(6) > 0) {
            possibleScores.put(getString(R.string.score_six), countLockedDiceWithValue(6) * 6);
        }
        if (currentPlayerScore.brelan == -1 && calculateBrelan() > 0) {
            possibleScores.put(getString(R.string.brelan), calculateBrelan());
        }
        if (currentPlayerScore.carre == -1 && calculateCarre() > 0) {
            possibleScores.put(getString(R.string.carre), calculateCarre());
        }
        if (currentPlayerScore.full == -1 && calculateFull() > 0) {
            possibleScores.put(getString(R.string.full), calculateFull());
        }
        if (currentPlayerScore.petiteSuite == -1 && calculatePetiteSuite() > 0) {
            possibleScores.put(getString(R.string.petite_suite), calculatePetiteSuite());
        }
        if (currentPlayerScore.grandeSuite == -1 && calculateGrandeSuite() > 0) {
            possibleScores.put(getString(R.string.grande_suite), calculateGrandeSuite());
        }
        if (currentPlayerScore.yams == -1 && calculateYams() > 0) {
            possibleScores.put(getString(R.string.yams), calculateYams());
        }
        if (currentPlayerScore.chance == -1) {
            possibleScores.put(getString(R.string.chance), calculateChance());
        }

        // Si des combinaisons sont possibles, choisir celle avec le score le plus élevé
        if (!possibleScores.isEmpty()) {
            String bestCombination = null;
            int maxScore = -1;

            // Parcourir toutes les combinaisons et trouver celle qui donne le plus de points
            for (String combination : possibleScores.keySet()) {
                int score = possibleScores.get(combination);
                if (score > maxScore) {
                    maxScore = score;
                    bestCombination = combination;
                }
            }

            // Remplir la meilleure combinaison trouvée
            if (bestCombination != null) {
                fillScoreForCurrentPlayer(bestCombination, maxScore);
                Log.d("GameActivity", "IA: A choisi de marquer " + maxScore + " points pour " + bestCombination + ".");
            }
        }
        // Si aucune combinaison n'est trouvée, barrer une figure
        else {
            barFigureForIA();
        }
    }

    // Méthode pour barrer une figure si aucune combinaison n'est possible
    private void barFigureForIA() {
        Score currentPlayerScore = playerScores.get(players.get(currentPlayerIndex));

        // Barrer la première figure disponible
        if (currentPlayerScore.ones == -1) {
            barFigure(getString(R.string.score_one));
            Log.d("GameActivity", "IA: A barré les '1'.");
        } else if (currentPlayerScore.twos == -1) {
            barFigure(getString(R.string.score_two));
            Log.d("GameActivity", "IA: A barré les '2'.");
        } else if (currentPlayerScore.threes == -1) {
            barFigure(getString(R.string.score_three));
            Log.d("GameActivity", "IA: A barré les '3'.");
        } else if (currentPlayerScore.fours == -1) {
            barFigure(getString(R.string.score_four));
            Log.d("GameActivity", "IA: A barré les '4'.");
        } else if (currentPlayerScore.fives == -1) {
            barFigure(getString(R.string.score_five));
            Log.d("GameActivity", "IA: A barré les '5'.");
        } else if (currentPlayerScore.sixes == -1) {
            barFigure(getString(R.string.score_six));
            Log.d("GameActivity", "IA: A barré les '6'.");
        } else if (currentPlayerScore.brelan == -1) {
            barFigure(getString(R.string.brelan));
            Log.d("GameActivity", "IA: A barré le 'brelan'.");
        } else if (currentPlayerScore.carre == -1) {
            barFigure(getString(R.string.carre));
            Log.d("GameActivity", "IA: A barré le 'carré'.");
        } else if (currentPlayerScore.full == -1) {
            barFigure(getString(R.string.full));
            Log.d("GameActivity", "IA: A barré le 'full'.");
        } else if (currentPlayerScore.petiteSuite == -1) {
            barFigure(getString(R.string.petite_suite));
            Log.d("GameActivity", "IA: A barré la 'petite suite'.");
        } else if (currentPlayerScore.grandeSuite == -1) {
            barFigure(getString(R.string.grande_suite));
            Log.d("GameActivity", "IA: A barré la 'grande suite'.");
        } else if (currentPlayerScore.yams == -1) {
            barFigure(getString(R.string.yams));
            Log.d("GameActivity", "IA: A barré le 'Yam's'.");
        } else if (currentPlayerScore.chance == -1) {
            barFigure(getString(R.string.chance));
            Log.d("GameActivity", "IA: A barré la 'chance'.");
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateUI() {
        // Mettre à jour l'interface utilisateur (indicateur de joueur, etc.)
        Player currentPlayer = players.get(currentPlayerIndex);

        // Vérifier si le joueur est une IA
        if (currentPlayer.isIa()) {
            // Si c'est une IA, afficher le nom avec le niveau de difficulté
            String iaLevel = currentPlayer.getDifficulty(); // Récupérer le niveau de difficulté
            currentPlayerText.setText(getString(R.string.show_turn, currentPlayer.getName()) + " (IA - " + iaLevel + ")");
        } else {
            // Si ce n'est pas une IA, afficher juste le nom du joueur
            currentPlayerText.setText(getString(R.string.show_turn, currentPlayer.getName()));
        }

        TextView countdownText = findViewById(R.id.countdown_text);
        countdownText.setVisibility(View.GONE);

        if (currentPlayer.isIa()) {
            // Désactiver les interactions pour les dés et le bouton de lancer pendant le tour de l'IA
            disableDiceInteraction();
            disableRollInteraction();

            // Débuter un décompte de 5 secondes
            countdownText.setVisibility(View.VISIBLE);
            disableDiceVisibility();

            new Handler().post(new Runnable() {
                int secondsRemaining = 5;

                @Override
                public void run() {
                    if (secondsRemaining > 0) {
                        countdownText.setText(String.valueOf(secondsRemaining)); // Affiche le temps restant
                        secondsRemaining--;

                        // Re-appelle cette méthode chaque seconde
                        new Handler().postDelayed(this, 1000);
                    } else {
                        // Cacher le décompte après 5 secondes et lancer les dés pour l'IA
                        countdownText.setVisibility(View.GONE);
                        rollDiceForIa(currentPlayer);
                    }
                }
            });
        } else {
            // Activer les interactions pour le joueur humain
            enableDiceVisibility();
            enableDiceInteraction();
            enableRollInteraction();
        }
    }
}