package com.example.data.firebase

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class AuthUserState(
  val isAuthenticated: Boolean = false,
  val uid: String? = null,
  val email: String? = null,
  val isAnonymous: Boolean = false,
  val isFirebaseConfigured: Boolean = true,
  val errorMessage: String? = null
)

class FirebaseAuthManager {

  private val auth: FirebaseAuth? by lazy {
    try {
      if (FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext).isNotEmpty()) {
        FirebaseAuth.getInstance()
      } else {
        null
      }
    } catch (_: Exception) {
      null
    }
  }

  fun isAvailable(): Boolean = auth != null

  fun getCurrentUser(): FirebaseUser? = auth?.currentUser

  fun userStateFlow(): Flow<AuthUserState> = callbackFlow {
    val firebaseAuth = auth
    if (firebaseAuth == null) {
      trySend(
        AuthUserState(
          isAuthenticated = false,
          isFirebaseConfigured = false,
          errorMessage = "Firebase not initialized. Add google-services.json for remote authentication."
        )
      )
      awaitClose { }
      return@callbackFlow
    }

    val listener = FirebaseAuth.AuthStateListener { fa ->
      val user = fa.currentUser
      trySend(
        AuthUserState(
          isAuthenticated = user != null,
          uid = user?.uid,
          email = user?.email,
          isAnonymous = user?.isAnonymous ?: false,
          isFirebaseConfigured = true,
          errorMessage = null
        )
      )
    }

    firebaseAuth.addAuthStateListener(listener)
    awaitClose {
      firebaseAuth.removeAuthStateListener(listener)
    }
  }

  suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUser?> {
    val firebaseAuth = auth
      ?: return Result.failure(IllegalStateException("Firebase is not initialized."))
    return try {
      val res = firebaseAuth.signInWithEmailAndPassword(email.trim(), pass).await()
      Result.success(res.user)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun signUpWithEmail(email: String, pass: String): Result<FirebaseUser?> {
    val firebaseAuth = auth
      ?: return Result.failure(IllegalStateException("Firebase is not initialized."))
    return try {
      val res = firebaseAuth.createUserWithEmailAndPassword(email.trim(), pass).await()
      Result.success(res.user)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun signInAnonymously(): Result<FirebaseUser?> {
    val firebaseAuth = auth
      ?: return Result.failure(IllegalStateException("Firebase is not initialized."))
    return try {
      val res = firebaseAuth.signInAnonymously().await()
      Result.success(res.user)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun signOut() {
    auth?.signOut()
  }
}
