package com.pascal.pokemontcg.ui.list

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.pascal.pokemontcg.ui.adapter.LoadingStateAdapter
import com.pascal.pokemontcg.ui.adapter.PokemonListAdapter
import com.pascal.pokemontcg.databinding.ActivityPokemonListBinding
import com.pascal.pokemontcg.ui.detail.PokemonDetailActivity

class PokemonListActivity : AppCompatActivity() {

    private val viewModel: PokemonListViewModel by viewModel()
    private lateinit var binding: ActivityPokemonListBinding
    private lateinit var adapter: PokemonListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPokemonListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initAdapter()
        onSearchPokemon()
        onRefreshListener()
        getData()
    }

    private fun initAdapter() {
        adapter = PokemonListAdapter()

        binding.rvPokemon.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                adapter.retry()
            }
        )

        adapter.onItemClick = { pokemonData ->
            Intent(this, PokemonDetailActivity::class.java).apply {
                putExtra(POKEMON_EXTRA, pokemonData)
                startActivity(this)
            }
        }

        adapter.addLoadStateListener { loadState ->
            onFetchLoading(loadState)

            lifecycleScope.launch {
                viewModel.isDatabaseEmpty().observe(
                    this@PokemonListActivity
                ) { isEmpty ->
                    Log.d(TAG, "initAdapter: $isEmpty")
                    if (isEmpty) {
                        onFetchError(loadState)
                    }
                }
            }

            onFetchEmpty(loadState)
        }
    }

    private fun onFetchLoading(loadState: CombinedLoadStates) {
        if (loadState.refresh is LoadState.Loading || loadState.append is LoadState.Loading) {
            binding.swipeRefresh.isRefreshing = true
            binding.tvErrorState.visibility = View.GONE
        } else {
            binding.rvPokemon.visibility = View.VISIBLE
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun onFetchError(loadState: CombinedLoadStates) {
        Log.d(TAG, "onFetchError: Should be error")
        binding.tvErrorState.visibility = View.VISIBLE
        binding.rvPokemon.visibility = View.GONE
        onFetchEmpty(loadState)
    }

    private fun onFetchEmpty(loadState: CombinedLoadStates) {
        if (loadState.append.endOfPaginationReached) {
            if (adapter.itemCount < 1) {
                Log.d(TAG, "onFetchEmpty: Should be empty")
                binding.tvErrorState.visibility = View.VISIBLE
            }
        }
    }

    private fun onSearchPokemon() {
        binding.btnSearch.setOnClickListener {
            viewModel.deleteAllPokemon()

            val query = binding.etSearch.text.toString()
            getData(query)
        }
    }

    private fun onRefreshListener() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.deleteAllPokemon()

            binding.etSearch.setText("")

            getData()
        }
    }

    private fun getData(query: String = "") {
        viewModel.getPokemon(query).observe(this) {
            it?.let {
                adapter.submitData(lifecycle, it)
                Log.d(TAG, "getData: ${adapter.itemCount}")
            }
        }
    }

    companion object {
        private val TAG = PokemonListActivity::class.java.simpleName
        const val POKEMON_EXTRA = "Pokemon Data"
    }
}