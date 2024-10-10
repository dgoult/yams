package com.limayrac.yams;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder> {

    private ArrayList<Player> players;
    private PlayerSettingsActivity activity; // Référence à l'activité pour obtenir le nom d'IA

    public PlayerAdapter(ArrayList<Player> players, PlayerSettingsActivity activity) {
        this.players = players;
        this.activity = activity;
    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.player_item, parent, false);
        return new PlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
        Player player = players.get(position);

        holder.playerNameEditText.setText(player.getName());

        // Met à jour l'indicateur IA
        holder.iaIndicator.setVisibility(player.isIa() ? View.VISIBLE : View.GONE);

        holder.iaLevelSpinner.setVisibility(player.isIa() ? View.VISIBLE : View.GONE);

        // On attache un TextWatcher pour surveiller les changements dans EditText
        holder.playerNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Ne rien faire avant le changement de texte
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Met à jour le nom du joueur quand le texte change, si ce n'est pas une IA
                if (!player.isIa()) {
                    player.setName(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Ne rien faire après le changement de texte
            }
        });

        holder.toggleIaButton.setOnClickListener(v -> {
            player.toggleIa();
            activity.updateStartGameButtonState();
            if (player.isIa()) {
                holder.iaIndicator.setVisibility(View.VISIBLE);
                holder.iaLevelSpinner.setVisibility(View.VISIBLE);

                // Initialiser les options du Spinner (niveau de difficulté)
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(holder.itemView.getContext(),
                        R.array.ia_difficulty_levels, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                holder.iaLevelSpinner.setAdapter(adapter);

                // Pré-sélectionner le niveau de l'IA si défini
                if (player.getDifficulty() != null) {
                    int spinnerPosition = adapter.getPosition(player.getDifficulty());
                    holder.iaLevelSpinner.setSelection(spinnerPosition);
                }

                // Gérer les changements de niveau de difficulté
                holder.iaLevelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        String selectedLevel = (String) parentView.getItemAtPosition(position);
                        player.setDifficulty(selectedLevel); // Associe la difficulté au joueur
                        Log.d("PlayerAdapter", "IA difficulty set to: " + selectedLevel); // Ajout d'un log pour vérifier
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // Rien à faire ici
                    }
                });
                // Changer le nom en IA X et afficher l'indicateur IA
                player.setName(activity.getNextIaName());
                holder.playerNameEditText.setText(player.getName());
                holder.iaIndicator.setVisibility(View.VISIBLE);
            } else {
                // Remettre le nom par défaut s'il redevient un joueur
                player.setName("Player " + (position + 1));
                holder.playerNameEditText.setText(player.getName());
                // Masquer le Spinner et l'indicateur IA si c'est un joueur humain
                holder.iaIndicator.setVisibility(View.GONE);
                holder.iaLevelSpinner.setVisibility(View.GONE);
                activity.decrementtIaCount();
            }
            holder.toggleIaButton.setText(player.isIa()
                    ? v.getContext().getString(R.string.switch_to_player)
                    : v.getContext().getString(R.string.switch_to_ia));
        });

        holder.removePlayerButton.setOnClickListener(v -> {
            activity.decrementtIaCount();
            players.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, players.size());
            activity.updateStartGameButtonState();
        });
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    public static class PlayerViewHolder extends RecyclerView.ViewHolder {
        EditText playerNameEditText;
        Button toggleIaButton;
        Button removePlayerButton;
        TextView iaIndicator; // Nouveau TextView pour l'indicateur IA
        Spinner iaLevelSpinner;

        public PlayerViewHolder(@NonNull View itemView) {
            super(itemView);
            playerNameEditText = itemView.findViewById(R.id.player_name_edit_text);
            toggleIaButton = itemView.findViewById(R.id.toggle_ia_button);
            removePlayerButton = itemView.findViewById(R.id.remove_player_button);
            iaIndicator = itemView.findViewById(R.id.ia_indicator); // Récupération de l'indicateur IA
            iaLevelSpinner = itemView.findViewById(R.id.ia_level_spinner);
        }
    }
}