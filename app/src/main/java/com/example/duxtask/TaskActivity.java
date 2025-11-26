package com.example.duxtask;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskActivity extends AppCompatActivity {

    private TextInputEditText etTitulo, etDescripcion;
    private Spinner spinnerCategoria, spinnerSubcategoria, spinnerEstado;
    private FloatingActionButton btnGuardarTask;
    private MaterialButton btnVolver;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Map<String, List<String>> categoriasMap;

    private boolean modoEdicion = false;
    private String documentIdEditar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_task);

        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        spinnerSubcategoria = findViewById(R.id.spinnerSubcategoria);
        spinnerEstado = findViewById(R.id.spinnerEstado);
        btnGuardarTask = findViewById(R.id.btnGuardarTask);
        btnVolver = findViewById(R.id.btnVolver);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        cargarCategorias();

        ArrayAdapter<String> estadoAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                new String[]{"Pendiente", "Completado"});
        estadoAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerEstado.setAdapter(estadoAdapter);

        spinnerCategoria.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                String catSeleccionada = spinnerCategoria.getSelectedItem().toString();
                List<String> subcats = categoriasMap.get(catSeleccionada);
                if (subcats == null) subcats = new ArrayList<>();
                ArrayAdapter<String> subAdapter = new ArrayAdapter<>(
                        TaskActivity.this,
                        R.layout.spinner_item,
                        subcats);
                subAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                spinnerSubcategoria.setAdapter(subAdapter);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        verificarModoEdicion();

        btnGuardarTask.setOnClickListener(v -> {
            if (modoEdicion) {
                editarTarea();
            } else {
                guardarTarea();
            }
        });

        btnVolver.setOnClickListener(v -> volverAMain());
    }

    private void cargarCategorias() {
        categoriasMap = new HashMap<>();
        categoriasMap.put("Estudio", List.of("Examenes", "Tps", "Apuntes"));
        categoriasMap.put("Programación", List.of("Java", "Python", "PHP"));
        categoriasMap.put("Trabajo", List.of("Informes", "Reuniones", "Tareas"));

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                new ArrayList<>(categoriasMap.keySet()));
        catAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerCategoria.setAdapter(catAdapter);
    }

    private void verificarModoEdicion() {
        Intent intent = getIntent();
        if (intent.hasExtra("documentId")) {
            modoEdicion = true;
            documentIdEditar = intent.getStringExtra("documentId");

            etTitulo.setText(intent.getStringExtra("title"));
            etDescripcion.setText(intent.getStringExtra("description"));

            String categoria = intent.getStringExtra("category");
            if (categoria != null) {
                int catPosition = new ArrayList<>(categoriasMap.keySet()).indexOf(categoria);
                if (catPosition >= 0) {
                    spinnerCategoria.setSelection(catPosition);
                }
            }

            spinnerCategoria.post(() -> {
                String subcategoria = intent.getStringExtra("subcategory");
                if (subcategoria != null && spinnerSubcategoria.getAdapter() != null) {
                    ArrayAdapter<String> subAdapter = (ArrayAdapter<String>) spinnerSubcategoria.getAdapter();
                    int subPosition = subAdapter.getPosition(subcategoria);
                    if (subPosition >= 0) {
                        spinnerSubcategoria.setSelection(subPosition);
                    }
                }
            });

            String estado = intent.getStringExtra("state");
            if (estado != null) {
                ArrayAdapter<String> estadoAdapter = (ArrayAdapter<String>) spinnerEstado.getAdapter();
                int estadoPosition = estadoAdapter.getPosition(estado);
                if (estadoPosition >= 0) {
                    spinnerEstado.setSelection(estadoPosition);
                }
            }
        }
    }

    private void guardarTarea() {
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String categoria = spinnerCategoria.getSelectedItem() != null ? spinnerCategoria.getSelectedItem().toString() : "";
        String subcategoria = spinnerSubcategoria.getSelectedItem() != null ? spinnerSubcategoria.getSelectedItem().toString() : "";
        String estado = spinnerEstado.getSelectedItem() != null ? spinnerEstado.getSelectedItem().toString() : "";

        if (titulo.isEmpty() || descripcion.isEmpty()) {
            Toast.makeText(this, "Por favor completa título y descripción", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "usuarioEjemplo";

        Map<String, Object> tareaMap = new HashMap<>();
        tareaMap.put("title", titulo);
        tareaMap.put("description", descripcion);
        tareaMap.put("category", categoria);
        tareaMap.put("subcategory", subcategoria);
        tareaMap.put("state", List.of(estado));
        tareaMap.put("userId", userId);

        db.collection("task")
                .add(tareaMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(TaskActivity.this, "Tarea guardada correctamente", Toast.LENGTH_SHORT).show();
                    volverAMain();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(TaskActivity.this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void editarTarea() {
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String categoria = spinnerCategoria.getSelectedItem() != null ? spinnerCategoria.getSelectedItem().toString() : "";
        String subcategoria = spinnerSubcategoria.getSelectedItem() != null ? spinnerSubcategoria.getSelectedItem().toString() : "";
        String estado = spinnerEstado.getSelectedItem() != null ? spinnerEstado.getSelectedItem().toString() : "";

        if (titulo.isEmpty() || descripcion.isEmpty()) {
            Toast.makeText(this, "Por favor completa título y descripción", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> tareaMap = new HashMap<>();
        tareaMap.put("title", titulo);
        tareaMap.put("description", descripcion);
        tareaMap.put("category", categoria);
        tareaMap.put("subcategory", subcategoria);
        tareaMap.put("state", List.of(estado));

        db.collection("task")
                .document(documentIdEditar)
                .update(tareaMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(TaskActivity.this, "Tarea actualizada correctamente", Toast.LENGTH_SHORT).show();
                    volverAMain();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(TaskActivity.this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void volverAMain() {
        Intent intent = new Intent(TaskActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Limpia el stack y vuelve a MainActivity
        startActivity(intent);
        finish();
    }
}