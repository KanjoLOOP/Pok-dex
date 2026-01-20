package com.example.pokedex.fragments;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.pokedex.R;
import com.example.pokedex.adapters.AdapterPokemon;
import com.example.pokedex.database.RepositorioPokemon;
import com.example.pokedex.models.Pokemon;
import com.example.pokedex.utils.GestorPreferencias;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FragmentListaPokemon extends Fragment {

    private ListView vistaLista;
    private ProgressBar barraProgreso;
    private AutoCompleteTextView etBusqueda;
    private Button btnBuscar;
    private AdapterPokemon adaptador;
    private List<Pokemon> listaPokemon;
    private RepositorioPokemon repositorio;
    private GestorPreferencias preferencias;
    private ExecutorService servicioExecutor;
    private int posicionSeleccionada = -1;

    // Variables para la paginación (Bonus)
    private boolean estaCargando = false;
    private int offsetActual = 0;
    private final int LIMITE_CARGA = 20;

    public FragmentListaPokemon() {

    }

    @Override
    public View onCreateView(LayoutInflater inflador, ViewGroup contenedor,
            Bundle estadoGuardado) {
        return inflador.inflate(R.layout.fragment_pokemon_list, contenedor, false);
    }

    @Override
    public void onViewCreated(@NonNull View vista, @Nullable Bundle estadoGuardado) {
        super.onViewCreated(vista, estadoGuardado);

        // Inicializamos componentes
        vistaLista = vista.findViewById(R.id.listView);
        barraProgreso = vista.findViewById(R.id.progressBar);
        etBusqueda = vista.findViewById(R.id.etSearch);
        btnBuscar = vista.findViewById(R.id.btnSearch);

        // Inicializamos repositorio y preferencias
        repositorio = new RepositorioPokemon(requireContext());
        preferencias = new GestorPreferencias(requireContext());

        listaPokemon = new ArrayList<>();
        adaptador = new AdapterPokemon(requireContext(), listaPokemon);
        vistaLista.setAdapter(adaptador);

        // Registramos el menú contextual para la lista
        registerForContextMenu(vistaLista);

        // Servicio para búsquedas (si fuera necesario lanzarlas en backgroud
        // manualmente,
        // aunque el repositorio ya lo hace)
        servicioExecutor = Executors.newSingleThreadExecutor();

        // Configuramos el autoComplete para sugerencias
        ArrayAdapter<String> adaptadorSugerencias = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, repositorio.obtenerNombresPokemon());
        etBusqueda.setAdapter(adaptadorSugerencias);

        // Listener de scroll para la paginación infinita
        vistaLista.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            //cargamos mas pokemons al final de la lista
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount >= totalItemCount && totalItemCount > 0 && !estaCargando) {
                    cargarMasPokemon();
                }
            }
        });

        // Listener para click en item
        vistaLista.setOnItemClickListener((padre, v, posicion, id) -> {
            Pokemon seleccionado = listaPokemon.get(posicion);
            // Navegamos al fragment de detalle pasando el ID
            Bundle bundle = new Bundle();
            bundle.putSerializable("pokemon", seleccionado);
            Navigation.findNavController(vista).navigate(R.id.action_list_to_detail, bundle);
        });

        vistaLista.setOnItemLongClickListener((padre, v, posicion, id) -> {
            posicionSeleccionada = posicion;
            return false;
        });

        // La barra de busqueda
        btnBuscar.setOnClickListener(v -> {
            String consulta = etBusqueda.getText().toString();
            if (!consulta.isEmpty()) {
                buscarPokemon(consulta);
            } else {
                // Si se borra la búsqueda, recargamos la lista normal
                offsetActual = 0;
                listaPokemon.clear();
                cargarMasPokemon();
            }
        });

        // Cargar lista inicial
        cargarMasPokemon();
    }

    // Metodo para cargar los Pokémon
    private void cargarMasPokemon() {
        estaCargando = true;
        if (offsetActual == 0)
            barraProgreso.setVisibility(View.VISIBLE);

        repositorio.obtenerPokemon(LIMITE_CARGA, offsetActual, pokemons -> {
            requireActivity().runOnUiThread(() -> {
                barraProgreso.setVisibility(View.GONE);
                estaCargando = false;

                if (pokemons != null && !pokemons.isEmpty()) {
                    listaPokemon.addAll(pokemons);
                    adaptador.notifyDataSetChanged();
                    offsetActual += LIMITE_CARGA;
                } else if (offsetActual == 0) {
                    Toast.makeText(getContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void buscarPokemon(String consulta) {
        // Para buscar paramos la paginación temporalmente
        estaCargando = true;
        barraProgreso.setVisibility(View.VISIBLE);
        listaPokemon.clear();
        adaptador.notifyDataSetChanged();

        servicioExecutor.execute(() -> {
            // Buscamos en repositorio
            final Pokemon p = repositorio.obtenerPokemon(consulta.toLowerCase());

            requireActivity().runOnUiThread(() -> {
                barraProgreso.setVisibility(View.GONE);
                estaCargando = false;
                if (p != null) {
                    listaPokemon.add(p);
                    adaptador.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Pokémon no encontrado", Toast.LENGTH_SHORT).show();
                    // Si falla, recargamos la lista normal
                    offsetActual = 0;
                    cargarMasPokemon();
                }
            });
        });
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v,
            @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflador = requireActivity().getMenuInflater();
        inflador.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        // Verificamos si este fragment es visible antes de procesar
        if (!isVisible())
            return super.onContextItemSelected(item);

        if (posicionSeleccionada == -1)
            return super.onContextItemSelected(item);

        if (posicionSeleccionada >= listaPokemon.size())
            return super.onContextItemSelected(item);

        Pokemon pokemonSeleccionado = listaPokemon.get(posicionSeleccionada);

        if (item.getItemId() == R.id.ctx_add_favorite) {
            preferencias.anadirFavorito(pokemonSeleccionado.getId());
            Toast.makeText(getContext(), "Añadido a favoritos", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.ctx_remove_favorite) {
            preferencias.eliminarFavorito(pokemonSeleccionado.getId());
            Toast.makeText(getContext(), "Eliminado de favoritos", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onContextItemSelected(item);
    }
}
