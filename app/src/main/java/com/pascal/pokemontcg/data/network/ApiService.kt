package com.pascal.pokemontcg.data.network

import com.pascal.pokemontcg.model.PokemonResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("cards")
    suspend fun getPokemon(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("pageSize") size: Int
    ): PokemonResponse
}