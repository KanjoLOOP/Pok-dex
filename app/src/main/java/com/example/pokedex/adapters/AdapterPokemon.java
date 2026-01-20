package com.example.pokedex.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.pokedex.R;
import com.example.pokedex.models.Pokemon;
import androidx.core.content.ContextCompat;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Color;
import android.widget.LinearLayout;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Adaptador personalizado para mostrar los Pokémon en el ListView.
public class AdapterPokemon extends BaseAdapter {

    private Context contexto;
    private List<Pokemon> listaPokemon;
    private ExecutorService servicioExecutor; // Para cargar imágenes en fondo

    public AdapterPokemon(Context contexto, List<Pokemon> listaPokemon) {
        this.contexto = contexto;
        this.listaPokemon = listaPokemon;
        this.servicioExecutor = Executors.newFixedThreadPool(4); // Pool de hilos para imágenes
    }

    @Override
    public int getCount() {
        return listaPokemon.size();
    }

    @Override
    public Object getItem(int posicion) {
        return listaPokemon.get(posicion);
    }

    @Override
    public long getItemId(int posicion) {
        return listaPokemon.get(posicion).getId();
    }

    @Override
    public View getView(int posicion, View vistaConvertida, ViewGroup padre) {
        // Patrón ViewHolder para optimización
        ContenedorVistas contenedor;
        if (vistaConvertida == null) {
            vistaConvertida = LayoutInflater.from(contexto).inflate(R.layout.item_pokemon, padre, false);
            contenedor = new ContenedorVistas();
            contenedor.ivImagen = vistaConvertida.findViewById(R.id.imageView);
            contenedor.tvNombre = vistaConvertida.findViewById(R.id.tvNombre);
            contenedor.llTipos = vistaConvertida.findViewById(R.id.llTipos);
            vistaConvertida.setTag(contenedor);
        } else {
            contenedor = (ContenedorVistas) vistaConvertida.getTag();
        }

        Pokemon pokemon = listaPokemon.get(posicion);
        contenedor.tvNombre.setText(pokemon.getNombre());

        // Configurar los badges de tipos
        contenedor.llTipos.removeAllViews();
        String[] tipos = pokemon.getTipos().split(",");
        for (String tipo : tipos) {
            TextView tvTipo = new TextView(contexto);
            tvTipo.setText(tipo.trim().toUpperCase());
            tvTipo.setTextColor(Color.WHITE);
            tvTipo.setTextSize(10);
            tvTipo.setPadding(16, 8, 16, 8);
            tvTipo.setTypeface(null, android.graphics.Typeface.BOLD);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 8, 0); // Margen derecho entre badges
            tvTipo.setLayoutParams(params);

            // Background drawable con color dinámico
            // Copiando la lógica chula de los colores
            GradientDrawable drawable = (GradientDrawable) ContextCompat
                    .getDrawable(contexto, R.drawable.bg_type_badge).mutate();
            drawable.setColor(obtenerColorTipo(tipo.trim().toLowerCase()));
            tvTipo.setBackground(drawable);

            contenedor.llTipos.addView(tvTipo);
        }

        // Cargar imagen (Simple descarga asíncrona)
        if (pokemon.getUrlImagen() != null && !pokemon.getUrlImagen().isEmpty()) {
            // Limpiamos imagen anterior
            contenedor.ivImagen.setImageResource(android.R.drawable.ic_menu_gallery);
            cargarImagenPokemon(pokemon.getUrlImagen(), contenedor.ivImagen);
        }

        return vistaConvertida;
    }

    // Método simple para cargar imagen desde URL en segundo plano
    private void cargarImagenPokemon(String urlString, ImageView imagenVista) {
        servicioExecutor.execute(() -> {
            try {
                InputStream entrada = new java.net.URL(urlString).openStream();
                Bitmap bmp = BitmapFactory.decodeStream(entrada);
                // Volvemos al hilo principal para actualizar la UI
                new Handler(Looper.getMainLooper()).post(() -> imagenVista.setImageBitmap(bmp));
            } catch (Exception e) {
                Log.e("AdapterPokemon", "Error imagen: " + e.getMessage());
            }
        });
    }

    // Método para los colores (igual que en detalle)
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

    static class ContenedorVistas {
        ImageView ivImagen;
        TextView tvNombre;
        LinearLayout llTipos;
    }
}
