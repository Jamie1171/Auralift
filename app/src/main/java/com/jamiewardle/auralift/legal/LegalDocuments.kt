package com.jamiewardle.auralift.legal

import android.content.Context
import java.util.Locale

/** Every advertised language has a complete, offline document pair. */
object LegalDocuments {
    val languages = setOf("en", "es", "fr")

    fun language(tag: String): String = Locale.forLanguageTag(tag).language
        .takeIf { it in languages } ?: "en"

    fun name(language: String): String = when (language) {
        "es" -> "Español"
        "fr" -> "Français"
        else -> "English"
    }

    fun read(context: Context, page: String, languageTag: String): String {
        require(page == "terms" || page == "privacy")
        val locale = language(languageTag)
        val folder = if (locale == "en") "legal" else "legal/$locale"
        return context.assets.open("$folder/$page.txt").bufferedReader(Charsets.UTF_8).use { it.readText() }
    }
}
