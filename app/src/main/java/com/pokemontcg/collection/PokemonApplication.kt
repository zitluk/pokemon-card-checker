package com.pokemontcg.collection

import android.app.Application
import com.pokemontcg.collection.data.AppContainer
import com.pokemontcg.collection.util.LocaleHelper

class PokemonApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun attachBaseContext(base: android.content.Context) {
        super.attachBaseContext(LocaleHelper.wrap(base))
    }

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
