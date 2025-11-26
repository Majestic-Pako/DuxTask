package com.example.duxtask;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TaskListFragment extends Fragment implements TaskAdapter.TaskClickListener {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> tasks;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerView = view.findViewById(R.id.recyclerTasks);
        tasks = new ArrayList<>();

        adapter = new TaskAdapter(tasks, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        cargarTareasDesdeFirebase();

        return view;
    }

    private void cargarTareasDesdeFirebase() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(getContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("task")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Error al cargar tareas: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        tasks.clear();
                        for (QueryDocumentSnapshot document : value) {
                            Task task = document.toObject(Task.class);
                            task.setDocumentId(document.getId());
                            tasks.add(task);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onEditClick(Task task, int position) {
        Intent intent = new Intent(getContext(), TaskActivity.class);

        intent.putExtra("documentId", task.getDocumentId());
        intent.putExtra("title", task.getTitle());
        intent.putExtra("description", task.getDescription());
        intent.putExtra("category", task.getCategory());
        intent.putExtra("subcategory", task.getSubcategory());

        if (task.getState() != null && !task.getState().isEmpty()) {
            intent.putExtra("state", task.getState().get(0));
        }

        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Task task, int position) {
        new AlertDialog.Builder(requireContext(), R.style.DuxTaskAlertDialog)
                .setTitle("Eliminar tarea")
                .setMessage("¿Estás seguro de eliminar '" + task.getTitle() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    eliminarTareaDeFirebase(task.getDocumentId());
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void eliminarTareaDeFirebase(String documentId) {
        db.collection("task")
                .document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Tarea eliminada correctamente", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}