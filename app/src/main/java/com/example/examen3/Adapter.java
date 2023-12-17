package com.example.examen3;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class Adapter extends FirestoreRecyclerAdapter<Persona, Adapter.PersonViewHolder> {

    public Adapter(@NonNull FirestoreRecyclerOptions<Persona> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull PersonViewHolder holder, int position, @NonNull Persona model) {
        holder.bind(model);
    }

    @NonNull
    @Override
    public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.persona, parent, false);
        return new PersonViewHolder(view);
    }

    public class PersonViewHolder extends RecyclerView.ViewHolder {
        private TextView nombrePeriodista;
        private TextView Descripcion;
        private TextView fecha;
        private Button button;

        public PersonViewHolder(@NonNull View itemView) {
            super(itemView);
            nombrePeriodista = itemView.findViewById(R.id.firstname);
            Descripcion = itemView.findViewById(R.id.lastname);
            button = itemView.findViewById(R.id.button); // Reemplaza "miBoton" con el ID de tu botón en el diseño XML

            // Agregar el ClickListener al botón
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(itemView.getContext(), EntrevistaActivity.class);
                    intent.putExtra("Periodista", String.valueOf(nombrePeriodista.getText().toString().trim()));
                    intent.putExtra("Descripcion", String.valueOf(Descripcion.getText().toString().trim()));
                    itemView.getContext().startActivity(intent);
                }
            });
        }

        public void bind(Persona persona) {
            // Bind data to TextViews
            nombrePeriodista.setText(persona.getPeriodista());
            Descripcion.setText(persona.getDescripcion());
            //fecha.setText(persona.getFecha());
        }
    }
}