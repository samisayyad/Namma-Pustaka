package com.nammapustaka.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
@Entity(tableName = "books")
data class Book(
    @PrimaryKey
    @DocumentId
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val genre: String = "",
    val description: String = "",
    val coverUrl: String = "",
    val isbn: String = "",
    val pages: Int = 0,
    val publishedYear: Int = 0,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val isAvailable: Boolean = true,
    val qrCode: String = "",
    val language: String = "English",
    val schoolId: String = "",
    val addedAt: Long = 0L,
    val borrowCount: Int = 0
) : Parcelable

@Parcelize
data class Category(
    val id: String = "",
    val name: String = "",
    val iconRes: Int = 0,
    val colorHex: String = "#5B4FCF",
    val bookCount: Int = 0
) : Parcelable
