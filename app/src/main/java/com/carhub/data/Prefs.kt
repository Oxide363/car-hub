package com.carhub.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "carhub")

class Prefs(private val context: Context) {

    companion object {
        private val PIN = stringPreferencesKey("pin_hash")
        private val TREE = stringPreferencesKey("tree_uri")
        private val PASSENGER = booleanPreferencesKey("passenger_mode")
        private val FAVORITES = stringSetPreferencesKey("favorites")
        private val RESUME = stringPreferencesKey("resume_json")
        private val KIDS = stringSetPreferencesKey("kids_categories")
    }

    val pinHash: Flow<String?> = context.dataStore.data.map { it[PIN] }
    val treeUri: Flow<String?> = context.dataStore.data.map { it[TREE] }
    val passenger: Flow<Boolean> = context.dataStore.data.map { it[PASSENGER] ?: false }
    val favorites: Flow<Set<String>> = context.dataStore.data.map { it[FAVORITES] ?: emptySet() }
    val resumeJson: Flow<String> = context.dataStore.data.map { it[RESUME] ?: "{}" }
    val kidsCategories: Flow<Set<String>> = context.dataStore.data.map { it[KIDS] ?: emptySet() }

    suspend fun setPin(hash: String) {
        context.dataStore.edit { it[PIN] = hash }
    }

    suspend fun setTree(uri: String) {
        context.dataStore.edit { it[TREE] = uri }
    }

    suspend fun setPassenger(value: Boolean) {
        context.dataStore.edit { it[PASSENGER] = value }
    }

    suspend fun setFavorites(value: Set<String>) {
        context.dataStore.edit { it[FAVORITES] = value }
    }

    suspend fun setResumeJson(value: String) {
        context.dataStore.edit { it[RESUME] = value }
    }

    suspend fun setKidsCategories(value: Set<String>) {
        context.dataStore.edit { it[KIDS] = value }
    }
}
