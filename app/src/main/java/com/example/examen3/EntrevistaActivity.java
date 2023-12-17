package com.example.examen3;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;


public class EntrevistaActivity extends AppCompatActivity {

    // Referencia a la instancia de Firestore
    private FirebaseFirestore db;

    String idPeriodista;
    String descripcion, periodista, nombreImagen, fec;
    ImageView imageView2;
    FirebaseStorage storage;
    StorageReference storageReference;
    Button btnver, Eliminar, btnmod;

    // TextView para mostrar la descripción del periodista
    private TextView txtDescripcionPeriodista, txtperiodista,txtfecha;



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
        fec = intent.getStringExtra("fecha");


        Toast.makeText(getApplicationContext(), descripcion, Toast.LENGTH_SHORT).show();


        txtDescripcionPeriodista = findViewById(R.id.etDescripcion);
        txtperiodista = findViewById(R.id.textView6);
        txtfecha = findViewById(R.id.editTextDate2);
        imageView2 = findViewById(R.id.imageView2);
        btnver = findViewById(R.id.button4);
        Eliminar = findViewById(R.id.button3);
        btnmod = findViewById(R.id.button2);
        buscarIdPeriodistaPorDescripcion();

        Eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dentro del bucle for después de obtener el idPeriodista
                eliminarPeriodistaPorId();

            }
        });

        btnmod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modificarInfo();


            }
        });

        btnver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EntrevistaActivity.this, ReproduccionAudio.class);
                intent.putExtra("Descrip", String.valueOf(txtDescripcionPeriodista.getText().toString().trim()));
                intent.putExtra("Periodis", String.valueOf(txtperiodista.getText().toString().trim()));
                startActivity(intent);
            }
        });


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
                                String fecha = document.getString("Fecha");
                                txtDescripcionPeriodista.setText(descripcion);
                                txtperiodista.setText(periodista);
                                txtfecha.setText(fecha);

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
                .whereEqualTo("Descripcion", descripcion.toString())
                // .whereEqualTo("Periodista", periodista.toString())
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
                                Log.d("Perfil_entrevista", "ID del periodista encontrado: " + idPeriodista);


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
        // Asumiendo que "nombreImagen" es el nombre del archivo en Storage
        storageReference = FirebaseStorage.getInstance().getReference().child("Tesis".concat(".jpg"));

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

    public void eliminarPeriodistaPorId() {
        String periodistaId = idPeriodista;

        // Eliminar documento en Firestore
        db.collection("info").document(periodistaId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Perfil_entrevista", "Periodista eliminado con éxito en Firestore");
                        Toast.makeText(EntrevistaActivity.this, "Periodista eliminado con éxito en Firestore", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(EntrevistaActivity.this, MainActivity.class);

                        startActivity(intent);

                        // Después de eliminar en Firestore, también elimina el archivo en Firebase Storage
                        eliminarArchivoEnStorage(descripcion);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Perfil_entrevista", "Error al eliminar el periodista en Firestore", e);
                        Toast.makeText(EntrevistaActivity.this, "Error al eliminar el periodista en Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void eliminarArchivoEnStorage(String descripcionn) {
        // Construye la referencia al archivo en Firebase Storage
        StorageReference archivoReference = FirebaseStorage.getInstance().getReference().child("audios/" + descripcion + ".mp3");

        // Eliminar el archivo en Firebase Storage
        archivoReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Perfil_entrevista", "Archivo eliminado con éxito en Storage");
                        Toast.makeText(EntrevistaActivity.this, "Archivo eliminado con éxito en Storage", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Perfil_entrevista", "Error al eliminar el archivo en Storage", e);
                        Toast.makeText(EntrevistaActivity.this, "Error al eliminar el archivo en Storage", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public void modificarInfo() {
        // Verificar que se haya encontrado un ID de periodista
        if (idPeriodista != null && !idPeriodista.isEmpty()) {
            // Crear un mapa con los nuevos valores a actualizar
            Map<String, Object> nuevosDatos = new HashMap<>();
            nuevosDatos.put("Descripcion", txtDescripcionPeriodista.getText().toString());
            nuevosDatos.put("Periodista", txtperiodista.getText().toString());
            // nuevosDatos.put("Fecha", fecha.getText().toString());

            // Actualizar los datos en la colección "info" usando el ID del periodista
            db.collection("info")
                    .document(idPeriodista)
                    .update(nuevosDatos)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Perfil_entrevista", "Datos actualizados correctamente");
                            // Puedes realizar acciones adicionales después de la actualización
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Perfil_entrevista", "Error al actualizar los datos", e);
                            Toast.makeText(EntrevistaActivity.this, "Error al actualizar los datos", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Manejar el caso en el que no se haya encontrado un ID de periodista
            Log.e("Perfil_entrevista", "No se ha encontrado un ID de periodista");
            Toast.makeText(EntrevistaActivity.this, "No se ha encontrado un ID de periodista", Toast.LENGTH_SHORT).show();
        }
    }

}