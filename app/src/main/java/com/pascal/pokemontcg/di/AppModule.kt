package com.pascal.pokemontcg.di

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.pascal.pokemontcg.data.repo.PokemonRepository
import com.pascal.pokemontcg.data.local.PokemonDatabase
import com.pascal.pokemontcg.data.network.ApiService
import com.pascal.pokemontcg.ui.list.PokemonListViewModel
import java.util.concurrent.TimeUnit

val networkModule = module {
    single {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()
    }

    single {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.pokemontcg.io/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(get())
            .build()

        retrofit.create(ApiService::class.java)
    }
}

val repositoryModule = module {
    factory { PokemonDatabase.getDatabase(androidContext()) }
    factory { PokemonRepository(get(), get()) }
}

val viewModelModule = module {
    viewModel { PokemonListViewModel(get()) }
}