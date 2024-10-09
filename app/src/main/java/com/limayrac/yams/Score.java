package com.limayrac.yams;

public class Score {
    // Attributs pour chaque ligne de score (mineure et majeure)
    public int ones = -1; // -1 signifie que la ligne n'a pas encore été remplie
    public int twos = -1;
    public int threes = -1;
    public int fours = -1;
    public int fives = -1;
    public int sixes = -1;

    public int brelan = -1;
    public int carre = -1;
    public int full = -1;
    public int petiteSuite = -1;
    public int grandeSuite = -1;
    public int yams = -1;
    public int chance = -1;

    // Méthode pour calculer le total de la partie mineure
    public int calculateMinorTotal() {
        int total = 0;
        if (ones != -1) total += ones;
        if (twos != -1) total += twos;
        if (threes != -1) total += threes;
        if (fours != -1) total += fours;
        if (fives != -1) total += fives;
        if (sixes != -1) total += sixes;
        return total;
    }

    // Méthode pour calculer le bonus de 35
    public int calculateBonusMinor() {
        int total = 0;
        total = calculateMinorTotal();
        if (total >= 63) total = 35;
        return total;
    }

    // Méthode pour calculer le total de la partie majeure
    public int calculateMajorTotal() {
        int total = 0;
        if (brelan != -1) total += brelan;
        if (carre != -1) total += carre;
        if (full != -1) total += full;
        if (petiteSuite != -1) total += petiteSuite;
        if (grandeSuite != -1) total += grandeSuite;
        if (yams != -1) total += yams;
        if (chance != -1) total += chance;
        return total;
    }

    public boolean isAllFiguresFilled() {
        return ones != -1 && twos != -1 && threes != -1 && fours != -1 && fives != -1 && sixes != -1
                && brelan != -1 && carre != -1 && full != -1 && petiteSuite != -1 && grandeSuite != -1
                && yams != -1 && chance != -1;
    }

    // Calculer le score total (mineur + majeur)
    public int calculateTotal() {
        return calculateMinorTotal() + calculateMajorTotal();
    }
}