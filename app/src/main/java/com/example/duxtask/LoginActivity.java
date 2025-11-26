package com.example.duxtask;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText campoCorreo, campoContraseña;
    private Button botonIniciarSesion, botonRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null && !getIntent().getBooleanExtra("FROM_LOGOUT", false)) {
            irAlMain();
            return;
        }

        campoCorreo = findViewById(R.id.campoCorreo);
        campoContraseña = findViewById(R.id.campoContraseña);
        botonIniciarSesion = findViewById(R.id.botonIniciarSesion);
        botonRegistrar = findViewById(R.id.botonRegistrar);

        botonIniciarSesion.setOnClickListener(v -> iniciarSesion());
        botonRegistrar.setOnClickListener(v -> registrarUsuario());
    }

    private void registrarUsuario() {
        String correo = campoCorreo.getText().toString().trim();
        String contraseña = campoContraseña.getText().toString().trim();

        if (correo.isEmpty() || contraseña.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(this, "Error al guardar en Firestore", Toast.LENGTH_LONG).show()
                                );

                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
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
        startActivity(intent);
        finish();
    }
}
