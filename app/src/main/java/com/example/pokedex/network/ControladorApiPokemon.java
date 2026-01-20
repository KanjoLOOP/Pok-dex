package com.example.pokedex.network;

import android.util.Log;
import com.example.pokedex.models.Pokemon;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

// Clase encargada de las peticiones de red a la PokéAPI.
public class ControladorApiPokemon {

    private static final String URL_BASE = "https://pokeapi.co/api/v2/pokemon/";

    // Método para obtener un Pokémon por ID o nombre desde la API.
    // Este método debe llamarse desde un hilo secundario.
    public Pokemon obtenerPokemonDeApi(String idONombre) {
        HttpURLConnection conexionUrl = null;
        BufferedReader lector = null;
        String jsonStr = null;

        try {
            // Construimos la URL
            URL url = new URL(URL_BASE + idONombre);

            // Abrimos la conexión
            conexionUrl = (HttpURLConnection) url.openConnection();
            conexionUrl.setRequestMethod("GET");
            conexionUrl.connect();

            // Leemos la respuesta
            InputStream flujoEntrada = conexionUrl.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (flujoEntrada == null) {
                return null;
            }
            lector = new BufferedReader(new InputStreamReader(flujoEntrada));

            String linea;
            while ((linea = lector.readLine()) != null) {
                buffer.append(linea).append("\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            jsonStr = buffer.toString();

            // Parseamos el JSON
            return analizarJsonPokemon(jsonStr);

        } catch (IOException e) {
            Log.e("ControladorApiPokemon", "Error ", e);
            return null;
        } finally {
            // Cerramos recursos
            if (conexionUrl != null) {
                conexionUrl.disconnect();
            }
            if (lector != null) {
                try {
                    lector.close();
                } catch (final IOException e) {
                    Log.e("ControladorApiPokemon", "Error cerrando flujo", e);
                }
            }
        }
    }

    // Método auxiliar para parsear el JSON de respuesta.
    private Pokemon analizarJsonPokemon(String jsonStr) {
        try {
            JSONObject objetoJson = new JSONObject(jsonStr);

            int id = objetoJson.getInt("id");
            String nombre = objetoJson.getString("name");
            int peso = objetoJson.getInt("weight");
            int altura = objetoJson.getInt("height");

            // Obtener Tipos
            JSONArray arrayTipos = objetoJson.getJSONArray("types");
            StringBuilder constructorTipos = new StringBuilder();
            for (int i = 0; i < arrayTipos.length(); i++) {
                JSONObject objetoTipo = arrayTipos.getJSONObject(i);
                String nombreTipo = objetoTipo.getJSONObject("type").getString("name");
                if (i > 0)
                    constructorTipos.append(",");
                constructorTipos.append(nombreTipo);
            }
            String tipos = constructorTipos.toString();

            // Obtener Imagen (Sprite frontal por defecto)
            JSONObject sprites = objetoJson.getJSONObject("sprites");
            String urlImagen = sprites.getString("front_default");

            // Obtener Estadísticas
            JSONArray arrayStats = objetoJson.getJSONArray("stats");
            String estadisticas = arrayStats.toString();

            // Creamos el objeto Pokémon
            return new Pokemon(id, nombre, tipos, urlImagen, peso, altura, estadisticas, System.currentTimeMillis());

        } catch (JSONException e) {
            Log.e("ControladorApiPokemon", "Error analizando JSON", e);
            return null;
        }
    }
}
