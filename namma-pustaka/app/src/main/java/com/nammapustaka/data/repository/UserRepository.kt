package com.nammapustaka.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nammapustaka.data.model.LeaderboardEntry
import com.nammapustaka.data.model.User
import com.nammapustaka.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    val currentUserId: String? get() = auth.currentUser?.uid

    fun getCurrentUser(): Flow<Resource<User>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run {
            trySend(Resource.Error("Not authenticated"))
            close()
            return@callbackFlow
        }
        trySend(Resource.Loading())
        val listener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error"))
                    return@addSnapshotListener
                }
                val user = snapshot?.toObject(User::class.java)
                if (user != null) trySend(Resource.Success(user))
                else trySend(Resource.Error("User not found"))
            }
        awaitClose { listener.remove() }
    }

    suspend fun login(email: String, password: String): Resource<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Resource.Error("Login failed")
            val doc = firestore.collection("users").document(uid).get().await()
            val user = doc.toObject(User::class.java) ?: return Resource.Error("User not found")
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Login failed")
        }
    }

    suspend fun register(name: String, email: String, password: String, role: String, schoolId: String): Resource<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Resource.Error("Registration failed")
            val user = User(
                id = uid,
                name = name,
                email = email,
                role = role,
                schoolId = schoolId,
                createdAt = System.currentTimeMillis()
            )
            firestore.collection("users").document(uid).set(user).await()
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Registration failed")
        }
    }

    fun logout() = auth.signOut()

    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun getLeaderboard(schoolId: String): Flow<Resource<List<LeaderboardEntry>>> = callbackFlow {
        trySend(Resource.Loading())
        val currentUid = currentUserId
        val listener = firestore.collection("users")
            .whereEqualTo("schoolId", schoolId)
            .whereEqualTo("role", "STUDENT")
            .orderBy("totalBooksRead", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error"))
                    return@addSnapshotListener
                }
                val entries = snapshot?.documents?.mapIndexed { index, doc ->
                    val user = doc.toObject(User::class.java) ?: User()
                    LeaderboardEntry(
                        rank = index + 1,
                        userId = user.id,
                        userName = user.name,
                        userAvatarUrl = user.avatarUrl,
                        classGrade = user.classGrade,
                        booksRead = user.totalBooksRead,
                        streak = user.currentStreak,
                        isCurrentUser = user.id == currentUid
                    )
                } ?: emptyList()
                trySend(Resource.Success(entries))
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateProfile(userId: String, updates: Map<String, Any>): Resource<Unit> {
        return try {
            firestore.collection("users").document(userId).update(updates).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Profile update failed")
        }
    }

    suspend fun updateFcmToken(userId: String, token: String) {
        try {
            firestore.collection("users").document(userId)
                .update("fcmToken", token).await()
        } catch (_: Exception) {}
    }
}
