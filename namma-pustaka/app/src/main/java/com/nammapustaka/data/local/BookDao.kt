package com.nammapustaka.data.local

import androidx.room.*
import com.nammapustaka.data.model.Book
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Query("SELECT * FROM books WHERE schoolId = :schoolId ORDER BY addedAt DESC")
    fun getBooksForSchool(schoolId: String): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE id = :bookId LIMIT 1")
    suspend fun getBookById(bookId: String): Book?

    @Query("SELECT * FROM books WHERE (title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%') AND schoolId = :schoolId")
    suspend fun searchBooks(query: String, schoolId: String): List<Book>

    @Upsert
    suspend fun upsertBooks(books: List<Book>)

    @Upsert
    suspend fun upsertBook(book: Book)

    @Query("DELETE FROM books WHERE schoolId = :schoolId")
    suspend fun deleteBooksForSchool(schoolId: String)

    @Query("SELECT COUNT(*) FROM books WHERE schoolId = :schoolId")
    suspend fun getBookCount(schoolId: String): Int
}
