package com.limayrac.yams;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import java.util.Locale;
import android.content.res.Configuration;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    Animation animtop, animbottom;

    private boolean isSoundEnabled = true; // Par défaut, le son est activé

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        ImageView imageView = findViewById(R.id.logo);

        Button playButton = findViewById(R.id.play_button);
        Button languageButton = findViewById(R.id.language_button);
        Button rulesButton = findViewById(R.id.rules_button);

        animtop = AnimationUtils.loadAnimation(this, R.anim.animation_top);
        animbottom = AnimationUtils.loadAnimation(this, R.anim.animation_bottom);

        imageView.setAnimation( animtop );
        playButton.setAnimation( animbottom );
        languageButton.setAnimation( animbottom );
        rulesButton.setAnimation( animbottom );

        playButton.setOnClickListener(view -> {
            playMenuSound();
            Intent intent = new Intent(MainActivity.this, PlayerSettingsActivity.class);
            startActivity(intent);
        });

        languageButton.setOnClickListener(view -> showLanguageSelector());

        rulesButton.setOnClickListener(v -> {
            playMenuSound();
            // Ouvrir le navigateur
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.yahtzee_rules_url)));
            startActivity(browserIntent);
        });
    }

    private void playMenuSound() {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.menu);
        mediaPlayer.setOnCompletionListener(MediaPlayer::release); // Libère la ressource une fois le son joué
        mediaPlayer.start();
    }

    private void showLanguageSelector() {
        playMenuSound();
        // Options de langues
        String[] languages = {"English", "Français"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_language);
        builder.setItems(languages, (dialog, which) -> {
            if (which == 0) {
                setLocale("en"); // Sélectionne l'anglais
            } else if (which == 1) {
                setLocale("fr"); // Sélectionne le français
            }
        });
        builder.show();
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        // Redémarrer l'activité pour appliquer la nouvelle langue
        Intent refresh = new Intent(this, MainActivity.class);
        startActivity(refresh);
        finish(); // Fermer l'activité actuelle
    }
}