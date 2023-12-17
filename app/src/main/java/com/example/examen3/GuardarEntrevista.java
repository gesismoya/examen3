package com.example.examen3;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;

public class GuardarEntrevista extends AppCompatActivity {

    private static final int PICK_AUDIO_REQUEST = 1;
    private static final int PICK_IMAGE_REQUEST = 2;

    private Button btnSelectAudio;
    private Button btnSelectImage;
    private Button btnUpload;
    private EditText etDescripcion, edtfecha;
    private TextView edtperiodista;
    private Uri audioUri;
    private Uri imageUri;
    private ProgressDialog progressDialog;

    private StorageReference audioStorageReference;
    private StorageReference imageStorageReference;
    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guardar_entrevista);
        db = FirebaseFirestore.getInstance();

        btnSelectAudio = findViewById(R.id.btnSelectAudio);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnUpload = findViewById(R.id.btnUpload);
        edtfecha = findViewById(R.id.editTextDate);
        edtperiodista = findViewById(R.id.textView6);
        etDescripcion = findViewById(R.id.etDescripcion);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");

        audioStorageReference = FirebaseStorage.getInstance().getReference("audios");
        imageStorageReference = FirebaseStorage.getInstance().getReference("images");

        btnSelectAudio.setOnClickListener(v -> openFileChooser(PICK_AUDIO_REQUEST));
        btnSelectImage.setOnClickListener(v -> openFileChooser(PICK_IMAGE_REQUEST));
        btnUpload.setOnClickListener(v -> uploadFile());
    }

    private void openFileChooser(int requestCode) {
        Intent intent = new Intent();
        if (requestCode == PICK_AUDIO_REQUEST) {
            intent.setType("audio/*");
        } else if (requestCode == PICK_IMAGE_REQUEST) {
            intent.setType("image/*");
        }
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, requestCode);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadFile() {
        if ((audioUri != null || imageUri != null) &&
                etDescripcion.getText() != null && !etDescripcion.getText().toString().isEmpty()) {
            String fileName = etDescripcion.getText().toString();

            if (audioUri != null) {
                fileName += "." + getFileExtension(audioUri);
                uploadFileToStorage(audioStorageReference.child(fileName), audioUri);
            }

            if (imageUri != null) {
                fileName += "." + getFileExtension(imageUri);
                uploadFileToStorage(imageStorageReference.child(fileName), imageUri);
            }
        } else {
            Toast.makeText(this, "Seleccione al menos un archivo y escriba una descripción antes de subir", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadFileToStorage(StorageReference fileReference, Uri fileUri) {
        progressDialog.show();

        fileReference.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    progressDialog.dismiss();
                    registrarUsuarioRespaldoFirebase();
                    Toast.makeText(GuardarEntrevista.this, "Archivo almacenado con éxito", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(GuardarEntrevista.this, MainActivity.class);

                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(GuardarEntrevista.this, "Error al almacenar el archivo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            if (requestCode == PICK_AUDIO_REQUEST) {
                audioUri = data.getData();
            } else if (requestCode == PICK_IMAGE_REQUEST) {
                imageUri = data.getData();
            }
        }
    }

    private void registrarUsuarioRespaldoFirebase() {
        String nombre = etDescripcion.getText().toString();
        String peri = edtperiodista.getText().toString();
        String fec = String.valueOf(edtfecha.getText());

        if (nombre.isEmpty() || peri.isEmpty() || fec.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Favor, revisar que todos los campos estén llenos", Toast.LENGTH_SHORT).show();
        } else {
            try {
                postUser(nombre, peri, fec);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error al registrar usuario", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void postUser(String nombre, String peri, String fec) {
        Map<String, Object> map = new HashMap<>();
        map.put("Descripcion", nombre);
        map.put("Periodista", peri);
        map.put("Fecha", fec);

        db.collection("info")
                .add(map)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getApplicationContext(), "Registro completo", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(GuardarEntrevista.this, EntrevistaActivity.class);
                        intent.putExtra("Periodista", String.valueOf(edtperiodista.getText().toString().trim()));
                        intent.putExtra("Descripcion", String.valueOf(etDescripcion.getText().toString().trim()));
                        intent.putExtra("fecha", String.valueOf(edtfecha.toString()));
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Registro no ingresado", Toast.LENGTH_LONG).show();
                    }
                });
    }
}