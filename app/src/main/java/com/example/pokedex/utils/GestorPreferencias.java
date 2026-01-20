package com.example.pokedex.utils;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

// Gestor de Preferencias Compartidas para guardar los Favoritos.
public class GestorPreferencias {

    private static final String NOMBRE_PREF = "PokedexPrefs";
    private static final String CLAVE_FAVORITOS = "favoritos";
    private SharedPreferences preferenciasCompartidas;

    public GestorPreferencias(Context contexto) {
        preferenciasCompartidas = contexto.getSharedPreferences(NOMBRE_PREF, Context.MODE_PRIVATE);
    }

    // Añade un ID a favoritos
    public void anadirFavorito(int idPokemon) {
        Set<String> favoritos = obtenerFavoritos();
        favoritos.add(String.valueOf(idPokemon));
        preferenciasCompartidas.edit().putStringSet(CLAVE_FAVORITOS, favoritos).apply();
    }

    // Elimina un ID de favoritos
    public void eliminarFavorito(int idPokemon) {
        Set<String> favoritos = obtenerFavoritos();
        favoritos.remove(String.valueOf(idPokemon));
        preferenciasCompartidas.edit().putStringSet(CLAVE_FAVORITOS, favoritos).apply();
    }

    // Comprueba si es favorito
    public boolean esFavorito(int idPokemon) {
        Set<String> favoritos = obtenerFavoritos();
        return favoritos.contains(String.valueOf(idPokemon));
    }

    // Obtiene todos los favoritos (devuelve copia mutable)
    public Set<String> obtenerFavoritos() {
        Set<String> favoritos = preferenciasCompartidas.getStringSet(CLAVE_FAVORITOS, new HashSet<>());
        return new HashSet<>(favoritos); // Devolvemos copia para poder modificar
    }

    // Elimina TODOS los favoritos
    public void limpiarFavoritos() {
        preferenciasCompartidas.edit().remove(CLAVE_FAVORITOS).apply();
    }
}
