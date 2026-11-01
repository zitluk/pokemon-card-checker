package com.pokemontcg.collection.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Query("SELECT * FROM collection_items ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<CollectionItemEntity>>

    @Query("SELECT * FROM collection_items WHERE id = :id")
    suspend fun getById(id: Long): CollectionItemEntity?

    @Query("SELECT * FROM collection_items WHERE tcgdexCardId = :cardId LIMIT 1")
    suspend fun getByCardId(cardId: String): CollectionItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CollectionItemEntity): Long

    @Update
    suspend fun update(item: CollectionItemEntity)

    @Query("DELETE FROM collection_items WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface CardCacheDao {
    @Query("SELECT * FROM cards_cache WHERE tcgdexCardId = :cardId")
    suspend fun get(cardId: String): CardCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CardCacheEntity)

    @Query("DELETE FROM cards_cache")
    suspend fun clearAll()
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY searchedAt DESC LIMIT 10")
    fun observeRecent(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: SearchHistoryEntity)

    @Query("DELETE FROM search_history")
    suspend fun clearAll()
}
