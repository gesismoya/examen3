package com.example.examen3;

import android.os.Bundle;
import android.text.TextUtils;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private Adapter adapter;
    private CollectionReference personsCollection;
    private SearchView searchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        personsCollection = db.collection("info");

        recyclerView = findViewById(R.id.recycler1);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filtrar resultados cuando se cambia el texto de búsqueda
                if (!TextUtils.isEmpty(newText)) {
                    searchFirestore(newText);
                } else {
                    // Si el texto de búsqueda está vacío, muestra todos los usuarios
                    setupRecyclerView("");
                }
                return true;
            }
        });

        setupRecyclerView("");

        private void searchFirestore(String query) {
            // Filtra los resultados de Firestore según la consulta
            Query searchQuery = personsCollection.whereEqualTo("Periodista", query); // Reemplaza "nombre" con el campo que deseas buscar
            FirestoreRecyclerOptions<Persona> options = new FirestoreRecyclerOptions.Builder<Persona>()
                    .setQuery(searchQuery, Persona.class)
                    .build();

            adapter.updateOptions(options);
        }

        private void setupRecyclerView(String query) {
            // Configura el RecyclerView con los datos de Firestore
            Query baseQuery = personsCollection.orderBy("Periodista"); // Reemplaza "nombre" con el campo por el cual deseas ordenar
            if (!TextUtils.isEmpty(query)) {
                baseQuery = personsCollection.whereEqualTo("Periodista", query);
            }

            FirestoreRecyclerOptions<Persona> options = new FirestoreRecyclerOptions.Builder<Persona>()
                    .setQuery(baseQuery, Persona.class)
                    .build();

            adapter = new Adapter(options);
            recyclerView.setAdapter(adapter);
        }

        @Override
        protected void onStart() {
            super.onStart();
            adapter.startListening();
        }

        @Override
        protected void onStop() {
            super.onStop();
            adapter.stopListening();
        }
    }
}
