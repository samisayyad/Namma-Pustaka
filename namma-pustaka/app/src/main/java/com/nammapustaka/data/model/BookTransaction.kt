package com.nammapustaka.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize

@Parcelize
data class BookTransaction(
    @DocumentId
    val id: String = "",
    val bookId: String = "",
    val bookTitle: String = "",
    val bookCoverUrl: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val schoolId: String = "",
    val issuedAt: Long = 0L,
    val dueDate: Long = 0L,
    val returnedAt: Long? = null,
    val isOverdue: Boolean = false,
    val finePaid: Boolean = false
) : Parcelable

@Parcelize
data class Review(
    @DocumentId
    val id: String = "",
    val bookId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatarUrl: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable
