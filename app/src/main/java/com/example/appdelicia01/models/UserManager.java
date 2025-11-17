package com.example.appdelicia01.models;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserManager {
    private static UserManager instance;

    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;
    private User currentUserData; // Tu clase 'models.User'

    private static final String TAG = "UserManager";

    // Interfaz para notificar a las Activities/Fragments sobre cambios de datos.
    public interface UserDataChangeListener {
        void onUserDataChanged();
    }

    private final List<UserDataChangeListener> listeners = new ArrayList<>();

    // El constructor ahora es privado y configura el AuthStateListener.
    private UserManager() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // --- PUNTO CLAVE: El AuthStateListener centraliza toda la lógica ---
        mAuth.addAuthStateListener(firebaseAuth -> {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser != null) {
                // Si hay un usuario en Firebase Auth, cargamos sus datos de Firestore.
                Log.d(TAG, "AuthStateListener: Usuario detectado (" + firebaseUser.getUid() + "). Cargando datos...");
                loadRoleAndNotify(firebaseUser);
            } else {
                // Si no hay usuario (sesión cerrada), limpiamos los datos y notificamos.
                Log.d(TAG, "AuthStateListener: No hay usuario. Limpiando datos.");
                clearData(); // clearData() ya llama a notifyListeners().
            }
        });
    }

    // Método para obtener la instancia única (Singleton).
    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    // --- LÓGICA DE CARGA CENTRALIZADA ---
    private void loadRoleAndNotify(FirebaseUser firebaseUser) {
        db.collection("users").document(firebaseUser.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            // El usuario existe en Firestore, convertimos el documento a nuestro objeto User.
                            this.currentUserData = document.toObject(User.class);
                            Log.d(TAG, "Datos de usuario cargados correctamente. Rol: " + (currentUserData != null ? currentUserData.getRole() : "null"));
                        } else {
                            // El usuario existe en Auth pero no en Firestore (caso raro).
                            // Creamos un objeto User básico para evitar NullPointerExceptions.
                            Log.w(TAG, "Usuario existe en Auth pero no tiene documento en Firestore.");
                            this.currentUserData = new User(); // Asumimos que tienes un constructor vacío.
                            this.currentUserData.setEmail(firebaseUser.getEmail());
                            // No se asigna rol, por lo que isAdmin() devolverá false.
                        }
                    } else {
                        // Ocurrió un error al intentar obtener el documento.
                        Log.e(TAG, "Error al cargar datos de usuario desde Firestore.", task.getException());
                        this.currentUserData = null;
                    }
                    // --- PUNTO CLAVE 2: Notificar SOLO DESPUÉS de que toda la operación ha terminado ---
                    notifyListeners();
                });
    }

    // --- MÉTODOS PARA MANEJAR LISTENERS ---

    public void addUserDataChangeListener(UserDataChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeUserDataChangeListener(UserDataChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        // Notifica a todas las Activities suscritas que los datos han cambiado.
        Log.d(TAG, "Notificando a " + listeners.size() + " listeners sobre cambio de datos.");
        for (UserDataChangeListener listener : listeners) {
            if (listener != null) {
                // Esto ejecutará onUserDataChanged() en tu CatalogActivity.
                listener.onUserDataChanged();
            }
        }
    }

    // --- MÉTODOS DE ACCESO PÚBLICO ---

    /**
     * Comprueba si el usuario actual tiene el rol de "admin".
     * Es seguro llamar a este método después de que onUserDataChanged() ha sido invocado.
     * @return true si el usuario es administrador, false en caso contrario.
     */
    public boolean isAdmin() {
        return currentUserData != null && "admin".equals(currentUserData.getRole());
    }

    /**
     * Devuelve el objeto User completo del usuario actualmente logueado.
     * @return El objeto User, o null si no hay sesión.
     */
    public User getCurrentUserData() {
        return currentUserData;
    }

    /**
     * Limpia los datos del usuario actual y notifica a los listeners.
     * Se llama al cerrar sesión o cuando el AuthStateListener no detecta usuario.
     */
    public void clearData() {
        if (currentUserData != null) {
            currentUserData = null;
            notifyListeners();
        }
    }
}
