package com.example.pokedex;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import com.example.pokedex.database.RepositorioPokemon;
import com.example.pokedex.models.Pokemon;
import java.util.Random;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private NavController controladorNavegacion;
    private SensorManager gestorSensores;
    private Sensor acelerometro;
    private long ultimoTiempoSacudida;
    private static final float UMBRAL_SACUDIDA = 12.0f;
    private static final int ENFRIAMIENTO_SACUDIDA_MS = 1000;

    @Override
    protected void onCreate(Bundle estadoGuardado) {
        super.onCreate(estadoGuardado);
        setContentView(R.layout.activity_main);

        // Aquí configuro la barra de arriba (Toolbar)
        Toolbar barraHerramientas = findViewById(R.id.toolbar);
        setSupportActionBar(barraHerramientas);

        // Si pulsas en el título, vuelves al inicio y resetea la lista
        barraHerramientas.setOnClickListener(v -> {
            if (controladorNavegacion != null) {
                // Configuro para borrar el historial y volver a crear el fragment limpio
                NavOptions opciones = new NavOptions.Builder()
                        .setPopUpTo(R.id.pokemonListFragment, true)
                        .build();
                controladorNavegacion.navigate(R.id.pokemonListFragment, null, opciones);
            }
        });

        NavHostFragment fragmentoHostNav = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (fragmentoHostNav != null) {
            controladorNavegacion = fragmentoHostNav.getNavController();
        }

        gestorSensores = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (gestorSensores != null) {
            acelerometro = gestorSensores.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_clear_cache) {
            new RepositorioPokemon(this).limpiarCache();
            // Limpiamos los favoritos
            new com.example.pokedex.utils.GestorPreferencias(this).limpiarFavoritos();
            Toast.makeText(this, R.string.cache_cleared, Toast.LENGTH_SHORT).show();
            // Recargar fragment actual
            controladorNavegacion.popBackStack();
            controladorNavegacion.navigate(R.id.pokemonListFragment);
            return true;
        } else if (id == R.id.action_favorites) {
            controladorNavegacion.navigate(R.id.action_global_favorites);
            return true;
        } else if (id == R.id.action_about) {
            Toast.makeText(this, "Pokédex v1.0 - Enzo Sevilla", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gestorSensores != null && acelerometro != null) {
            gestorSensores.registerListener(this, acelerometro, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gestorSensores != null) {
            gestorSensores.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent evento) {
        if (evento.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = evento.values[0];
            float y = evento.values[1];
            float z = evento.values[2];

            // Cálculo simple de magnitud de aceleración
            float gX = x / SensorManager.GRAVITY_EARTH;
            float gY = y / SensorManager.GRAVITY_EARTH;
            float gZ = z / SensorManager.GRAVITY_EARTH;

            // Fuerza G total
            float fuerzaG = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

            if (fuerzaG > 2.5f) { // Umbral empírico
                long ahora = System.currentTimeMillis();
                if (ahora - ultimoTiempoSacudida > ENFRIAMIENTO_SACUDIDA_MS) {
                    ultimoTiempoSacudida = ahora;
                    manejarEventoSacudida();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int precision) {
        // No necesario
    }

    private void manejarEventoSacudida() {
        Toast.makeText(this, R.string.shake_message, Toast.LENGTH_SHORT).show();

        // Cargar pokemon aleatorio y navegar
        Executors.newSingleThreadExecutor().execute(() -> {
            int idAleatorio = new Random().nextInt(151) + 1; // Gen 1
            RepositorioPokemon repo = new RepositorioPokemon(this);
            Pokemon p = repo.obtenerPokemon(String.valueOf(idAleatorio));

            if (p != null) {
                runOnUiThread(() -> {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("pokemon", p);
                    // Navegamos globalmente al detalle
                    controladorNavegacion.navigate(R.id.action_global_detail, bundle);
                });
            }
        });
    }
}
