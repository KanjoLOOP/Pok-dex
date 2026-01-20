package com.example.pokedex.models;

import java.io.Serializable;

// Clase modelo para representar un Pokémon.
// Implementa Serializable para poder pasar objetos entre fragments/activities si fuera necesario.
public class Pokemon implements Serializable {
    private int id;
    private String nombre;
    private String tipos; // Guardaremos los tipos separados por coma, ej: "fuego,volador"
    private String urlImagen;
    private int peso;
    private int altura;
    private String estadisticas; // JSON con las stats como texto
    private long timestamp; // Para controlar la caché (24h)

    public Pokemon(int id, String nombre, String tipos, String urlImagen, int peso, int altura, String estadisticas,
            long timestamp) {
        this.id = id;
        this.nombre = nombre;
        this.tipos = tipos;
        this.urlImagen = urlImagen;
        this.peso = peso;
        this.altura = altura;
        this.estadisticas = estadisticas;
        this.timestamp = timestamp;
    }

    // Constructor vacío
    public Pokemon() {
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipos() {
        return tipos;
    }

    public void setTipos(String tipos) {
        this.tipos = tipos;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    public int getPeso() {
        return peso;
    }

    public void setPeso(int peso) {
        this.peso = peso;
    }

    public int getAltura() {
        return altura;
    }

    public void setAltura(int altura) {
        this.altura = altura;
    }

    public String getEstadisticas() {
        return estadisticas;
    }

    public void setEstadisticas(String estadisticas) {
        this.estadisticas = estadisticas;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
