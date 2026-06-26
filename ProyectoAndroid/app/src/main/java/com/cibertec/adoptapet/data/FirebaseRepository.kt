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

    fun crearCuentaFirebase(
        email: String,
        password: String,
        onSuccess: (uid: String) -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                onSuccess(result.user?.uid ?: "")
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "No se pudo registrar la cuenta.")
            }
    }

    fun guardarDatosUsuario(
        uid: String,
        idUsuario: Int,
        username: String,
        nomAdoptante: String,
        apeAdoptante: String,
        email: String,
        telefono: String,
        dni: String,
        fechaNacimiento: String,
        direccion: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userData = hashMapOf(
            "uid" to uid,
            "id_usuario" to idUsuario,
            "username" to username,
            "nom_adoptante" to nomAdoptante,
            "ape_adoptante" to apeAdoptante,
            "email" to email,
            "telefono" to telefono,
            "dni" to dni,
            "fec_nacimiento" to fechaNacimiento,
            "direccion" to direccion,
            "rol" to "ROLE_ADOPTANTE",
            "activo" to true
        )
        db.collection("users").document(uid)
            .set(userData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error al guardar datos de usuario")
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
