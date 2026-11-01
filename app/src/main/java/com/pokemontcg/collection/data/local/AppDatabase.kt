package com.pokemontcg.collection.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        CollectionItemEntity::class,
        CardCacheEntity::class,
        SearchHistoryEntity::class,
    ],
    version = 2,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun collectionDao(): CollectionDao
    abstract fun cardCacheDao(): CardCacheDao
    abstract fun searchHistoryDao(): SearchHistoryDao
}
