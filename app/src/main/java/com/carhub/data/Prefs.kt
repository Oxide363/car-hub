package com.carhub.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "carhub")

class Prefs(private val context: Context) {

    companion object {
        private val PIN = stringPreferencesKey("pin_hash")
        private val TREE = stringPreferencesKey("tree_uri")
        private val PASSENGER = booleanPreferencesKey("passenger_mode")
    }

    val pinHash: Flow<String?> = context.dataStore.data.map { it[PIN] }
    val treeUri: Flow<String?> = context.dataStore.data.map { it[TREE] }
    val passenger: Flow<Boolean> = context.dataStore.data.map { it[PASSENGER] ?: false }

    suspend fun setPin(hash: String) {
        context.dataStore.edit { it[PIN] = hash }
    }

    suspend fun setTree(uri: String) {
        context.dataStore.edit { it[TREE] = uri }
    }

    suspend fun setPassenger(value: Boolean) {
        context.dataStore.edit { it[PASSENGER] = value }
    }
}
