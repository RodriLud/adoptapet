package com.cibertec.adoptapet.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class FirebaseRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun currentUserEmail(): String = auth.currentUser?.email.orEmpty()

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun getCurrentUserUid(): String? = auth.currentUser?.uid

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "No se pudo iniciar sesión.") }
    }

    fun register(
        email: String,
        password: String,
        name: String,
        fono: String,
        dni: String,
        fechaNacimiento: String,
        direccion: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // 1. Crear usuario en Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: ""

                // 2. Preparar datos para Firestore
                val userData = hashMapOf(
                    "uid" to uid,
                    "name" to name,
                    "email" to email,
                    "fono" to fono,
                    "dni" to dni,
                    "fechaNacimiento" to fechaNacimiento,
                    "direccion" to direccion,
                    "rol" to "ROLE_ADOPTANTE"
                )

                // 3. Guardar en Firestore
                db.collection("users").document(uid)
                    .set(userData)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e ->
                        onError("Cuenta creada, pero error al guardar datos: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "No se pudo registrar la cuenta.")
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun removeListener(listener: ListenerRegistration) {
        listener.remove()
    }

    fun getUserData(uid: String, onSuccess: (Map<String, Any>?) -> Unit, onError: (String) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document -> onSuccess(document.data) }
            .addOnFailureListener { e -> onError(e.message ?: "Error al obtener perfil") }
    }
}
