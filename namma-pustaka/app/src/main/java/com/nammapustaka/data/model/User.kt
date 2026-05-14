package com.nammapustaka.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize

enum class UserRole { STUDENT, TEACHER, ADMIN }

@Parcelize
data class User(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val role: String = UserRole.STUDENT.name,
    val classGrade: String = "",
    val schoolId: String = "",
    val totalBooksRead: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val points: Int = 0,
    val rank: Int = 0,
    val badges: List<Badge> = emptyList(),
    val favoriteBookIds: List<String> = emptyList(),
    val createdAt: Long = 0L,
    val fcmToken: String = ""
) : Parcelable

@Parcelize
data class Badge(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val iconUrl: String = "",
    val earnedAt: Long = 0L
) : Parcelable

@Parcelize
data class LeaderboardEntry(
    val rank: Int = 0,
    val userId: String = "",
    val userName: String = "",
    val userAvatarUrl: String = "",
    val classGrade: String = "",
    val booksRead: Int = 0,
    val streak: Int = 0,
    val isCurrentUser: Boolean = false
) : Parcelable

data class ChatMessage(
    val id: String = "",
    val content: String = "",
    val isFromUser: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false
)
