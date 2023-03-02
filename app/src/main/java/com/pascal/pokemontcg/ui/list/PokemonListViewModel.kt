package com.pascal.pokemontcg.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import com.pascal.pokemontcg.data.repo.PokemonRepository
import com.pascal.pokemontcg.model.PokemonData

class PokemonListViewModel(private val pokemonRepository: PokemonRepository) : ViewModel() {

    fun getPokemon(name: String): LiveData<PagingData<PokemonData>> =
        pokemonRepository.getPokemon(name).cachedIn(viewModelScope)

    fun deleteAllPokemon() {
        viewModelScope.launch(Dispatchers.IO) {
            pokemonRepository.deleteAllPokemon()
        }
    }

    suspend fun isDatabaseEmpty(): LiveData<Boolean> {
        val isEmpty = viewModelScope.async(Dispatchers.IO) {
            pokemonRepository.isPokemonEmpty()
        }

        return isEmpty.await()
    }
}