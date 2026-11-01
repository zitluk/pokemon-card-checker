package com.pokemontcg.collection.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    private val APP_LOCALE: Locale = Locale.forLanguageTag("cs")

    fun wrap(context: Context): Context {
        val config = Configuration(context.resources.configuration)
        config.setLocale(APP_LOCALE)
        return context.createConfigurationContext(config)
    }
}
