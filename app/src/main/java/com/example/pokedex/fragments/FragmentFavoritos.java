package com.example.pokedex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
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
import java.util.Set;
import java.util.concurrent.Executors;

public class FragmentFavoritos extends Fragment {

    private ListView vistaLista;
    private AdapterPokemon adaptador;
    private List<Pokemon> listaFavoritos;
    private RepositorioPokemon repositorio;
    private GestorPreferencias preferencias;

    public FragmentFavoritos() {

    }

    @Override
    public View onCreateView(LayoutInflater inflador, ViewGroup contenedor,
            Bundle estadoGuardado) {
        return inflador.inflate(R.layout.fragment_favorites, contenedor, false);
    }

    @Override
    public void onViewCreated(@NonNull View vista, @Nullable Bundle estadoGuardado) {
        super.onViewCreated(vista, estadoGuardado);

        vistaLista = vista.findViewById(R.id.lvFavorites);
        repositorio = new RepositorioPokemon(requireContext());
        preferencias = new GestorPreferencias(requireContext());
        listaFavoritos = new ArrayList<>();
        adaptador = new AdapterPokemon(requireContext(), listaFavoritos);
        vistaLista.setAdapter(adaptador);

        cargarFavoritos();

        vistaLista.setOnItemClickListener((padre, v, posicion, id) -> {
            Pokemon seleccionado = listaFavoritos.get(posicion);
            Bundle bundle = new Bundle();
            bundle.putSerializable("pokemon", seleccionado);
            Navigation.findNavController(vista).navigate(R.id.action_favorites_to_detail, bundle);
        });
    }

    private void cargarFavoritos() {
        Set<String> idsFavoritos = preferencias.obtenerFavoritos();
        if (idsFavoritos.isEmpty()) {
            Toast.makeText(getContext(), "No tienes favoritos", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            listaFavoritos.clear();
            for (String id : idsFavoritos) {
                Pokemon p = repositorio.obtenerPokemon(id);
                if (p != null) {
                    listaFavoritos.add(p);
                }
            }
            requireActivity().runOnUiThread(() -> adaptador.notifyDataSetChanged());
        });
    }
}
