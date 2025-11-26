package com.example.duxtask;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private TextInputEditText campoCorreo, campoContraseña;
    private MaterialButton botonIniciarSesion, botonRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        campoCorreo = findViewById(R.id.campoCorreo);
        campoContraseña = findViewById(R.id.campoContraseña);
        botonIniciarSesion = findViewById(R.id.botonIniciarSesion);
        botonRegistrar = findViewById(R.id.botonRegistrar);

        botonIniciarSesion.setOnClickListener(v -> iniciarSesion());
        botonRegistrar.setOnClickListener(v -> registrarUsuario());
    }

    protected void onStart() {
        super.onStart();

        boolean fromLogout = getIntent().getBooleanExtra("FROM_LOGOUT", false);

        if (auth.getCurrentUser() != null && !fromLogout) {
            irAlMain();
        }
    }

    private void registrarUsuario() {
        String correo = campoCorreo.getText().toString().trim();
        String contraseña = campoContraseña.getText().toString().trim();

        if (correo.isEmpty() || contraseña.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (contraseña.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(correo, contraseña)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = auth.getCurrentUser().getUid();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("email", correo);

                        db.collection("users")
                                .document(uid)
                                .set(userData)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Cuenta creada correctamente", Toast.LENGTH_SHORT).show();
                                    irAlMain();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Error al guardar en Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                );
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Error desconocido";
                        Toast.makeText(this, "Error al crear cuenta: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void iniciarSesion() {
        String correo = campoCorreo.getText().toString().trim();
        String contraseña = campoContraseña.getText().toString().trim();

        if (correo.isEmpty() || contraseña.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(correo, contraseña)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                        irAlMain();
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Error desconocido";
                        Toast.makeText(this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void irAlMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}