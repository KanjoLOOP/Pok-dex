package com.example.pokedex.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.pokedex.R;
import com.example.pokedex.models.Pokemon;
import com.example.pokedex.utils.GestorPreferencias;
import java.io.InputStream;
import java.util.concurrent.Executors;

// Este fragmento es el que muestra toda la info de un Pokémon concreto
public class FragmentDetallePokemon extends Fragment {

    private ImageView ivImagen;
    private TextView tvNombre, tvPeso, tvAltura, tvStats;
    private LinearLayout llTipos;
    private Button btnFavoritos;

    public FragmentDetallePokemon() {

    }

    @Override
    public View onCreateView(LayoutInflater inflador, ViewGroup contenedor,
            Bundle estadoGuardado) {
        return inflador.inflate(R.layout.fragment_pokemon_detail, contenedor, false);
    }

    @Override
    public void onViewCreated(@NonNull View vista, @Nullable Bundle estadoGuardado) {
        super.onViewCreated(vista, estadoGuardado);

        // Inicializamos vistas
        ivImagen = vista.findViewById(R.id.ivDetailImage);
        tvNombre = vista.findViewById(R.id.tvDetailName);
        llTipos = vista.findViewById(R.id.llDetailTypes);
        tvPeso = vista.findViewById(R.id.tvDetailWeight);
        tvAltura = vista.findViewById(R.id.tvDetailHeight);
        tvStats = vista.findViewById(R.id.tvDetailStats);
        btnFavoritos = vista.findViewById(R.id.btnAFavoritos);

        // Si me han pasan un Pokémon, muestra sus datos
        if (getArguments() != null) {
            Pokemon pokemon = (Pokemon) getArguments().getSerializable("pokemon");
            if (pokemon != null) {
                mostrarPokemon(pokemon);

                // Aquí hago que el botón funcione
                btnFavoritos.setOnClickListener(v -> {
                    GestorPreferencias prefs = new GestorPreferencias(requireContext());
                    prefs.anadirFavorito(pokemon.getId());
                    // Aviso al usuario de que se ha guardado
                    Toast.makeText(getContext(), "¡" + pokemon.getNombre() + " guardado en favoritos!",
                            Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    // Este metodo rellena los campos de texto con la info del Pokémon
    private void mostrarPokemon(Pokemon pokemon) {
        tvNombre.setText(pokemon.getNombre());
        configurarTipos(pokemon.getTipos());
        tvPeso.setText("Peso: " + pokemon.getPeso());
        tvAltura.setText("Altura: " + pokemon.getAltura());
        // Parsear y formatear estadísticas
        try {
            org.json.JSONArray arrayStats = new org.json.JSONArray(pokemon.getEstadisticas());
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < arrayStats.length(); i++) {
                org.json.JSONObject statObj = arrayStats.getJSONObject(i);
                int baseStat = statObj.getInt("base_stat");
                String name = statObj.getJSONObject("stat").getString("name");

                // Mayus para la primera letra del nombre
                name = name.substring(0, 1).toUpperCase() + name.substring(1);

                sb.append(name).append(": ").append(baseStat).append("\n");
            }
            tvStats.setText(sb.toString().trim());

        } catch (org.json.JSONException e) {
            e.printStackTrace();
            tvStats.setText(pokemon.getEstadisticas());
        }

        if (pokemon.getUrlImagen() != null && !pokemon.getUrlImagen().isEmpty()) {
            cargarImagenPokemon(pokemon.getUrlImagen());
        }
    }

    // Esto carga la imagen de internet en segundo plano para no bloquear
    private void cargarImagenPokemon(String urlString) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                InputStream entrada = new java.net.URL(urlString).openStream();
                Bitmap bmp = BitmapFactory.decodeStream(entrada);
                // Cuando tengo la imagen, vuelvo al hilo principal para ponerla
                new Handler(Looper.getMainLooper()).post(() -> ivImagen.setImageBitmap(bmp));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void configurarTipos(String tiposRaw) {
        llTipos.removeAllViews();
        String[] tipos = tiposRaw.split(",");

        for (String tipo : tipos) {
            TextView tvTipo = new TextView(getContext());
            tvTipo.setText(tipo.trim().toUpperCase());
            tvTipo.setTextColor(Color.WHITE);
            tvTipo.setTextSize(12);
            tvTipo.setPadding(32, 12, 32, 12);
            tvTipo.setTypeface(null, android.graphics.Typeface.BOLD);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            tvTipo.setLayoutParams(params);

            // Background drawable con color dinámico
            GradientDrawable drawable = (GradientDrawable) ContextCompat
                    .getDrawable(requireContext(), R.drawable.bg_type_badge).mutate();
            drawable.setColor(obtenerColorTipo(tipo.trim().toLowerCase()));
            tvTipo.setBackground(drawable);

            llTipos.addView(tvTipo);
        }
    }

    private int obtenerColorTipo(String tipo) {
        switch (tipo) {
            case "normal":
                return Color.parseColor("#A8A878");
            case "fire":
                return Color.parseColor("#F08030");
            case "water":
                return Color.parseColor("#6890F0");
            case "grass":
                return Color.parseColor("#78C850");
            case "electric":
                return Color.parseColor("#F8D030");
            case "ice":
                return Color.parseColor("#98D8D8");
            case "fighting":
                return Color.parseColor("#C03028");
            case "poison":
                return Color.parseColor("#A040A0");
            case "ground":
                return Color.parseColor("#E0C068");
            case "flying":
                return Color.parseColor("#A890F0");
            case "psychic":
                return Color.parseColor("#F85888");
            case "bug":
                return Color.parseColor("#A8B820");
            case "rock":
                return Color.parseColor("#B8A038");
            case "ghost":
                return Color.parseColor("#705898");
            case "dragon":
                return Color.parseColor("#7038F8");
            case "steel":
                return Color.parseColor("#B8B8D0");
            case "fairy":
                return Color.parseColor("#EE99AC");
            default:
                return Color.parseColor("#68A090");
        }
    }
}
