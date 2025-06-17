package com.carwise.android.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.carwise.android.view.components.ListingCardType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.cardTypeDataStore by preferencesDataStore("card_type_pref")

object CardTypePreference {
    private val CARD_TYPE_KEY = stringPreferencesKey("card_type")

    fun getCardTypeFlow(context: Context): Flow<ListingCardType> =
        context.cardTypeDataStore.data.map { prefs ->
            when (prefs[CARD_TYPE_KEY]) {
                "DETAILED" -> ListingCardType.DETAILED
                else -> ListingCardType.COMPACT
            }
        }

    suspend fun setCardType(context: Context, type: ListingCardType) {
        context.cardTypeDataStore.edit { prefs ->
            prefs[CARD_TYPE_KEY] = type.name
        }
    }
} 