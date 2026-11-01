package com.pokemontcg.collection.data

import android.content.Context
import com.pokemontcg.collection.BuildConfig
import androidx.room.Room
import com.pokemontcg.collection.data.local.AppDatabase
import com.pokemontcg.collection.data.remote.TcgDexApi
import com.pokemontcg.collection.data.repository.CatalogRepository
import com.pokemontcg.collection.data.repository.CollectionRepository
import com.pokemontcg.collection.data.repository.PricingRepository
import com.pokemontcg.collection.data.repository.SettingsRepository
import com.pokemontcg.collection.data.update.UpdateChecker
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BASIC
                    },
                )
            }
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.tcgdex.net/v2/")
        .client(okHttp)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val api: TcgDexApi = retrofit.create(TcgDexApi::class.java)

    val database: AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "pokemon_card_checker.db",
    ).fallbackToDestructiveMigration()
        .build()

    val collectionRepository = CollectionRepository(
        database.collectionDao(),
        database.cardCacheDao(),
    )
    val catalogRepository = CatalogRepository(api, database.cardCacheDao(), database.searchHistoryDao())
    val pricingRepository = PricingRepository(database.cardCacheDao(), catalogRepository)
    val settingsRepository = SettingsRepository(context)
    val updateChecker = UpdateChecker(okHttp)
}
