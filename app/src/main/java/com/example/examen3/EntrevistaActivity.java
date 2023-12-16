package com.example.examen3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class EntrevistaActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    String idPeriodista;
    String descripcion, periodista, nombreImagen;
    ImageView imageView2;
    FirebaseStorage storage;
    StorageReference storageReference;
    Button btnver;

    // TextView para mostrar la descripción del periodista
    private TextView txtDescripcionPeriodista, txtperiodista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrevista);

        // Inicializar la instancia de Firestore
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        Intent intent = getIntent();
        periodista = intent.getStringExtra("Periodista");
        descripcion = intent.getStringExtra("Descripcion");

        Toast.makeText(getApplicationContext(), descripcion, Toast.LENGTH_SHORT).show();


        txtDescripcionPeriodista = findViewById(R.id.editText);
        txtperiodista = findViewById(R.id.textView6);
        imageView2 = findViewById(R.id.imageView2);
        btnver = findViewById(R.id.button4);

        btnver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EntrevistaActivity.this, activity_reproduccion_audio.class);
                intent.putExtra("Periodis", String.valueOf(txtDescripcionPeriodista.getText().toString().trim()));
                intent.putExtra("Descrip", String.valueOf(txtperiodista.getText().toString().trim()));
                startActivity(intent);
            }
        });


        buscarIdPeriodistaPorDescripcion();

        // obtenerYMostrarDescripcionPeriodista(idPeriodista); // Reemplaza con el ID real del periodista
    }

    public void obtenerYMostrarDescripcionPeriodista() {
        // Acceder al documento del periodista en la colección "info"
        db.collection("info").document(idPeriodista)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Obtener la descripción del periodista y mostrarla en el TextView
                                String descripcion = document.getString("Descripcion");
                                String periodista = document.getString("Periodista");
                                txtDescripcionPeriodista.setText(descripcion);
                                txtperiodista.setText(periodista);

                            } else {
                                Toast.makeText(EntrevistaActivity.this, "Periodista no encontrado", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("Perfil_entrevista", "Error al obtener la descripción del periodista", task.getException());
                            Toast.makeText(EntrevistaActivity.this, "Error al obtener la descripción del periodista", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void buscarIdPeriodistaPorDescripcion() {
        // Realizar la consulta en la colección "info" para encontrar el ID del periodista
        db.collection("info")
                .whereEqualTo("Descripcion", descripcion)
                .whereEqualTo("Periodista", periodista)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Obtener el ID del periodista y realizar las acciones necesarias
                                idPeriodista = document.getId();
                                // Realizar las acciones necesarias con el ID del periodista
                                // Puede mostrar el ID en un TextView, pasarlo a otra actividad, etc.
                                // Por ahora, solo lo imprimo en el Log
                                Log.d("EntrevistaActivity", "ID del periodista encontrado: " + idPeriodista);
                                obtenerYMostrarDescripcionPeriodista();
                                cargarImagenDesdeFirebaseStorage();
                            }
                            if (task.getResult().isEmpty()) {
                                Toast.makeText(EntrevistaActivity.this, "No se encontró ningún periodista con esa descripción", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("Perfil_entrevista", "Error al buscar el ID del periodista por descripción", task.getException());
                            Toast.makeText(EntrevistaActivity.this, "Error al buscar el ID del periodista por descripción", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void cargarImagenDesdeFirebaseStorage() {
        // Obtener referencia a la imagen en Firebase Storage
        // nombreImagen = "Entrevista TEDx de ingenieria.jpeg";  // Asumiendo que "nombreImagen" es el nombre del archivo en Storage
        storageReference = FirebaseStorage.getInstance().getReference().child(descripcion.toString().concat(".jpeg"));

        // Descargar la URL de la imagen desde Firebase Storage
        storageReference.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Utilizar la biblioteca Glide para cargar la imagen en el ImageView
                        cargarImagenConGlide(uri);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Manejar la falla aquí, por ejemplo, mostrar un mensaje Toast
                        Toast.makeText(EntrevistaActivity.this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cargarImagenConGlide(Uri uri) {
        // Utilizar la biblioteca Glide para cargar la imagen en el ImageView
        Glide.with(EntrevistaActivity.this)
                .load(uri)
                .into(imageView2);
    }

}