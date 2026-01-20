package com.example.pokedex.database;

import android.provider.BaseColumns;

// Clase contrato que define la estructura de la base de datos (tablas y columnas).
public final class ContratoPokemon {
    // Constructor privado para evitar instanciación
    private ContratoPokemon() {
    }

    // Definición de la tabla Pokemon
    public static class EntradaPokemon implements BaseColumns {
        public static final String NOMBRE_TABLA = "pokemon";
        public static final String COLUMNA_ID = "id";
        public static final String COLUMNA_NOMBRE = "nombre";
        public static final String COLUMNA_TIPOS = "tipos";
        public static final String COLUMNA_URL_IMAGEN = "url_imagen";
        public static final String COLUMNA_PESO = "peso";
        public static final String COLUMNA_ALTURA = "altura";
        public static final String COLUMNA_ESTADISTICAS = "estadisticas";
        public static final String COLUMNA_TIMESTAMP = "timestamp";
    }
}
