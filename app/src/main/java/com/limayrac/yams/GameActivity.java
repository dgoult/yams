package com.limayrac.yams;

import android.app.AlertDialog;
import android.content.Intent;
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
        if (rollsRemaining > 0) { // S'assurer qu'il reste des lancers disponibles
            disableDiceInteraction(); // Désactive les interactions avec les dés
            disableRollInteraction(); // Désactive les interactions avec le bouton de lancé

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
                enableRollInteraction(); // Réactive les interactions avec le bouton de lancer après l'animation

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
        }

        LinearLayout scoreTableContent = findViewById(R.id.score_table_content);
        scoreTableContent.addView(rowLayout);
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
        if (isGameOver()) {
            showGameOverDialog();
        } else {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size(); // Passer au joueur suivant
            rollsRemaining = 3; // Réinitialiser les lancers restants
            resetDice(); // Réinitialiser les dés
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