package com.example.pokedex.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// Helper para gestionar la creación y actualización de la base de datos SQLite.
public class AyudanteBaseDatosPokemon extends SQLiteOpenHelper {

    public static final int VERSION_BASE_DATOS = 1;
    public static final String NOMBRE_BASE_DATOS = "Pokedex.db";

    // Sentencia SQL para crear la tabla
    private static final String SQL_CREAR_ENTRADAS = "CREATE TABLE " + ContratoPokemon.EntradaPokemon.NOMBRE_TABLA
            + " (" +
            ContratoPokemon.EntradaPokemon.COLUMNA_ID + " INTEGER PRIMARY KEY," +
            ContratoPokemon.EntradaPokemon.COLUMNA_NOMBRE + " TEXT," +
            ContratoPokemon.EntradaPokemon.COLUMNA_TIPOS + " TEXT," +
            ContratoPokemon.EntradaPokemon.COLUMNA_URL_IMAGEN + " TEXT," +
            ContratoPokemon.EntradaPokemon.COLUMNA_PESO + " INTEGER," +
            ContratoPokemon.EntradaPokemon.COLUMNA_ALTURA + " INTEGER," +
            ContratoPokemon.EntradaPokemon.COLUMNA_ESTADISTICAS + " TEXT," +
            ContratoPokemon.EntradaPokemon.COLUMNA_TIMESTAMP + " INTEGER)";

    // Sentencia SQL para eliminar la tabla
    private static final String SQL_ELIMINAR_ENTRADAS = "DROP TABLE IF EXISTS "
            + ContratoPokemon.EntradaPokemon.NOMBRE_TABLA;

    public AyudanteBaseDatosPokemon(Context contexto) {
        super(contexto, NOMBRE_BASE_DATOS, null, VERSION_BASE_DATOS);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Ejecutamos la creación de la tabla
        db.execSQL(SQL_CREAR_ENTRADAS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAntigua, int versionNueva) {
        // En caso de actualización, borramos la tabla antigua y la creamos de nuevo
        db.execSQL(SQL_ELIMINAR_ENTRADAS);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int versionAntigua, int versionNueva) {
        onUpgrade(db, versionAntigua, versionNueva);
    }
}
