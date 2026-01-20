package com.example.pokedex.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.pokedex.models.Pokemon;
import com.example.pokedex.network.ControladorApiPokemon;

// Clase Repositorio que gestiona la lógica de caché (BD vs API).
import java.util.ArrayList;
import java.util.List;
import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Clase Repositorio que gestiona la lógica de la caché
public class RepositorioPokemon {

    public interface Callback<T> {
        void onResult(T result);
    }

    // Lista simple de nombres para el autocompletado
    private static final String[] NOMBRES_POKEMON = {
            "Bulbasaur", "Ivysaur", "Venusaur", "Charmander", "Charmeleon", "Charizard",
            "Squirtle", "Wartortle", "Blastoise", "Caterpie", "Metapod", "Butterfree",
            "Weedle", "Kakuna", "Beedrill", "Pidgey", "Pidgeotto", "Pidgeot",
            "Rattata", "Raticate", "Spearow", "Fearow", "Ekans", "Arbok",
            "Pikachu", "Raichu", "Sandshrew", "Sandslash", "Nidoran♀", "Nidorina",
            "Nidoqueen", "Nidoran♂", "Nidorino", "Nidoking", "Clefairy", "Clefable",
            "Vulpix", "Ninetales", "Jigglypuff", "Wigglytuff", "Zubat", "Golbat",
            "Oddish", "Gloom", "Vileplume", "Paras", "Parasect", "Venonat", "Venomoth",
            "Diglett", "Dugtrio", "Meowth", "Persian", "Psyduck", "Golduck",
            "Mankey", "Primeape", "Growlithe", "Arcanine", "Poliwag", "Poliwhirl",
            "Poliwrath", "Abra", "Kadabra", "Alakazam", "Machop", "Machoke", "Machamp",
            "Bellsprout", "Weepinbell", "Victreebel", "Tentacool", "Tentacruel",
            "Geodude", "Graveler", "Golem", "Ponyta", "Rapidash", "Slowpoke", "Slowbro",
            "Magnemite", "Magneton", "Farfetch'd", "Doduo", "Dodrio", "Seel", "Dewgong",
            "Grimer", "Muk", "Shellder", "Cloyster", "Gastly", "Haunter", "Gengar",
            "Onix", "Drowzee", "Hypno", "Krabby", "Kingler", "Voltorb", "Electrode",
            "Exeggcute", "Exeggutor", "Cubone", "Marowak", "Hitmonlee", "Hitmonchan",
            "Lickitung", "Koffing", "Weezing", "Rhyhorn", "Rhydon", "Chansey",
            "Tangela", "Kangaskhan", "Horsea", "Seadra", "Goldeen", "Seaking",
            "Staryu", "Starmie", "Mr. Mime", "Scyther", "Jynx", "Electabuzz",
            "Magmar", "Pinsir", "Tauros", "Magikarp", "Gyarados", "Lapras", "Ditto",
            "Eevee", "Vaporeon", "Jolteon", "Flareon", "Porygon", "Omanyte", "Omastar",
            "Kabuto", "Kabutops", "Aerodactyl", "Snorlax", "Articuno", "Zapdos",
            "Moltres", "Dratini", "Dragonair", "Dragonite", "Mewtwo", "Mew"
    };

    public String[] obtenerNombresPokemon() {
        return NOMBRES_POKEMON;
    }

    private AyudanteBaseDatosPokemon ayudanteBd;
    private ControladorApiPokemon controladorApi;
    private static final long EXPIRACION_CACHE_MS = 24 * 60 * 60 * 1000; // 24 horas
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Constructor: aquí preparo la base de datos
    public RepositorioPokemon(Context context) {
        ayudanteBd = new AyudanteBaseDatosPokemon(context);
        controladorApi = new ControladorApiPokemon();
    }

    // Metodo para obtener la lista de Pokémon (los primeros 20 para el ejemplo)
    public void obtenerPokemon(int limit, int offset, Callback<List<Pokemon>> callback) {
        executor.execute(() -> {
            List<Pokemon> lista = new ArrayList<>();
            // Intentamos cargar
            for (int i = offset + 1; i <= offset + limit; i++) {
                Pokemon p = obtenerPokemon(String.valueOf(i));
                if (p != null) {
                    lista.add(p);
                }
            }
            new Handler(Looper.getMainLooper()).post(() -> callback.onResult(lista));
        });
    }

    public void obtenerPokemon(Callback<List<Pokemon>> callback) {
        obtenerPokemon(20, 0, callback);
    }

    // Metodo para obtener la lista de Pokémon
    // Primero miro si los tengo guardados en el móvil (base de datos)
    // Si no están, los pido a la API
    public Pokemon obtenerPokemon(String idONombre) { // Changed method signature back to original
        Pokemon pokemon = obtenerPokemonDeBd(idONombre);

        if (pokemon != null && !estaCaducado(pokemon.getTimestamp())) {
            Log.d("RepositorioPokemon", "Pokemon encontrado en caché y válido: " + pokemon.getNombre());
            return pokemon;
        }

        // Si no está en BD, vamos a la API
        Log.d("RepositorioPokemon", "Buscando en API: " + idONombre);
        pokemon = controladorApi.obtenerPokemonDeApi(idONombre);

        if (pokemon != null) {
            guardarPokemonEnBd(pokemon);
        }

        return pokemon;
    }

    // Metodo para limpiar la caché (borrar tabla y recrearla)
    public void limpiarCache() {
        SQLiteDatabase db = ayudanteBd.getWritableDatabase();
        db.execSQL("DELETE FROM " + ContratoPokemon.EntradaPokemon.NOMBRE_TABLA);
        db.close();
    }

    private boolean estaCaducado(long timestamp) {
        return (System.currentTimeMillis() - timestamp) > EXPIRACION_CACHE_MS;
    }

    // Lee de la base de datos
    private Pokemon obtenerPokemonDeBd(String idONombre) {
        SQLiteDatabase db = ayudanteBd.getReadableDatabase();

        String seleccion;
        String[] argumentosSeleccion;

        // Intentamos determinar si es ID (número) o nombre
        boolean esNumerico = idONombre.chars().allMatch(Character::isDigit);

        if (esNumerico) {
            seleccion = ContratoPokemon.EntradaPokemon.COLUMNA_ID + " = ?";
            argumentosSeleccion = new String[] { idONombre };
        } else {
            seleccion = ContratoPokemon.EntradaPokemon.COLUMNA_NOMBRE + " = ?";
            argumentosSeleccion = new String[] { idONombre };
        }

        Cursor cursor = db.query(
                ContratoPokemon.EntradaPokemon.NOMBRE_TABLA,
                null,
                seleccion,
                argumentosSeleccion,
                null,
                null,
                null);

        Pokemon pokemon = null;
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(ContratoPokemon.EntradaPokemon.COLUMNA_ID));
            String nombre = cursor
                    .getString(cursor.getColumnIndexOrThrow(ContratoPokemon.EntradaPokemon.COLUMNA_NOMBRE));
            String tipos = cursor.getString(cursor.getColumnIndexOrThrow(ContratoPokemon.EntradaPokemon.COLUMNA_TIPOS));
            String url = cursor
                    .getString(cursor.getColumnIndexOrThrow(ContratoPokemon.EntradaPokemon.COLUMNA_URL_IMAGEN));
            int peso = cursor.getInt(cursor.getColumnIndexOrThrow(ContratoPokemon.EntradaPokemon.COLUMNA_PESO));
            int altura = cursor.getInt(cursor.getColumnIndexOrThrow(ContratoPokemon.EntradaPokemon.COLUMNA_ALTURA));
            String stats = cursor
                    .getString(cursor.getColumnIndexOrThrow(ContratoPokemon.EntradaPokemon.COLUMNA_ESTADISTICAS));
            long tiempo = cursor
                    .getLong(cursor.getColumnIndexOrThrow(ContratoPokemon.EntradaPokemon.COLUMNA_TIMESTAMP));

            pokemon = new Pokemon(id, nombre, tipos, url, peso, altura, stats, tiempo);
            cursor.close();
        }
        db.close();
        return pokemon;
    }

    // Guarda o actualiza en la base de datos
    private void guardarPokemonEnBd(Pokemon pokemon) {
        SQLiteDatabase db = ayudanteBd.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put(ContratoPokemon.EntradaPokemon.COLUMNA_ID, pokemon.getId());
        valores.put(ContratoPokemon.EntradaPokemon.COLUMNA_NOMBRE, pokemon.getNombre());
        valores.put(ContratoPokemon.EntradaPokemon.COLUMNA_TIPOS, pokemon.getTipos());
        valores.put(ContratoPokemon.EntradaPokemon.COLUMNA_URL_IMAGEN, pokemon.getUrlImagen());
        valores.put(ContratoPokemon.EntradaPokemon.COLUMNA_PESO, pokemon.getPeso());
        valores.put(ContratoPokemon.EntradaPokemon.COLUMNA_ALTURA, pokemon.getAltura());
        valores.put(ContratoPokemon.EntradaPokemon.COLUMNA_ESTADISTICAS, pokemon.getEstadisticas());
        valores.put(ContratoPokemon.EntradaPokemon.COLUMNA_TIMESTAMP, pokemon.getTimestamp());

        // Usamos replace para insertar o actualizar si ya existe
        db.replace(ContratoPokemon.EntradaPokemon.NOMBRE_TABLA, null, valores);
        db.close();
    }
}
