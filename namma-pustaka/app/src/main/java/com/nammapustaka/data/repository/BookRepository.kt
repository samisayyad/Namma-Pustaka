package com.nammapustaka.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nammapustaka.data.model.Book
import com.nammapustaka.data.model.BookTransaction
import com.nammapustaka.data.model.Review
import com.nammapustaka.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    fun getBooks(schoolId: String, genre: String? = null): Flow<Resource<List<Book>>> = callbackFlow {
        trySend(Resource.Loading())
        var query: Query = firestore.collection("books")
            .whereEqualTo("schoolId", schoolId)
            .orderBy("addedAt", Query.Direction.DESCENDING)

        if (!genre.isNullOrEmpty() && genre != "All") {
            query = query.whereEqualTo("genre", genre)
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.message ?: "Unknown error"))
                return@addSnapshotListener
            }
            val books = snapshot?.toObjects(Book::class.java) ?: emptyList()
            trySend(Resource.Success(books))
        }

        awaitClose { listener.remove() }
    }

    fun searchBooks(schoolId: String, query: String): Flow<Resource<List<Book>>> = flow {
        emit(Resource.Loading())
        try {
            val queryLower = query.lowercase()
            val result = firestore.collection("books")
                .whereEqualTo("schoolId", schoolId)
                .get()
                .await()
            val books = result.toObjects(Book::class.java).filter { book ->
                book.title.lowercase().contains(queryLower) ||
                book.author.lowercase().contains(queryLower) ||
                book.genre.lowercase().contains(queryLower)
            }
            emit(Resource.Success(books))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Search failed"))
        }
    }

    fun getBookById(bookId: String): Flow<Resource<Book>> = flow {
        emit(Resource.Loading())
        try {
            val doc = firestore.collection("books").document(bookId).get().await()
            val book = doc.toObject(Book::class.java)
            if (book != null) emit(Resource.Success(book))
            else emit(Resource.Error("Book not found"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error fetching book"))
        }
    }

    fun getBookByQrCode(qrCode: String, schoolId: String): Flow<Resource<Book>> = flow {
        emit(Resource.Loading())
        try {
            val result = firestore.collection("books")
                .whereEqualTo("qrCode", qrCode)
                .whereEqualTo("schoolId", schoolId)
                .limit(1)
                .get()
                .await()
            val book = result.firstOrNull()?.toObject(Book::class.java)
            if (book != null) emit(Resource.Success(book))
            else emit(Resource.Error("Book not found for QR code"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "QR scan error"))
        }
    }

    suspend fun addBook(book: Book): Resource<String> {
        return try {
            val doc = firestore.collection("books").add(book).await()
            Resource.Success(doc.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add book")
        }
    }

    suspend fun issueBook(transaction: BookTransaction): Resource<Unit> {
        return try {
            val batch = firestore.batch()
            val transRef = firestore.collection("transactions").document()
            batch.set(transRef, transaction)
            val bookRef = firestore.collection("books").document(transaction.bookId)
            batch.update(bookRef, "isAvailable", false)
            batch.commit().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to issue book")
        }
    }

    suspend fun returnBook(transactionId: String, bookId: String): Resource<Unit> {
        return try {
            val batch = firestore.batch()
            val transRef = firestore.collection("transactions").document(transactionId)
            batch.update(transRef, mapOf(
                "returnedAt" to System.currentTimeMillis(),
                "isOverdue" to false
            ))
            val bookRef = firestore.collection("books").document(bookId)
            batch.update(bookRef, "isAvailable", true)
            batch.commit().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to return book")
        }
    }

    fun getReviews(bookId: String): Flow<Resource<List<Review>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = firestore.collection("reviews")
            .whereEqualTo("bookId", bookId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error"))
                    return@addSnapshotListener
                }
                val reviews = snapshot?.toObjects(Review::class.java) ?: emptyList()
                trySend(Resource.Success(reviews))
            }
        awaitClose { listener.remove() }
    }

    suspend fun addReview(review: Review): Resource<Unit> {
        return try {
            firestore.collection("reviews").add(review).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add review")
        }
    }

    fun getActiveTransactions(schoolId: String): Flow<Resource<List<BookTransaction>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = firestore.collection("transactions")
            .whereEqualTo("schoolId", schoolId)
            .whereEqualTo("returnedAt", null)
            .orderBy("issuedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error"))
                    return@addSnapshotListener
                }
                val transactions = snapshot?.toObjects(BookTransaction::class.java) ?: emptyList()
                trySend(Resource.Success(transactions))
            }
        awaitClose { listener.remove() }
    }

    fun getStudentTransactions(studentId: String): Flow<Resource<List<BookTransaction>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = firestore.collection("transactions")
            .whereEqualTo("studentId", studentId)
            .orderBy("issuedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error"))
                    return@addSnapshotListener
                }
                val transactions = snapshot?.toObjects(BookTransaction::class.java) ?: emptyList()
                trySend(Resource.Success(transactions))
            }
        awaitClose { listener.remove() }
    }
}
