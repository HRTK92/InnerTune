package com.zionhuang.music.ui.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.paging.LoadState
import androidx.paging.LoadState.Loading
import androidx.recyclerview.widget.LinearLayoutManager
import com.zionhuang.music.R
import com.zionhuang.music.databinding.FragmentExploreBinding
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.models.SongParcel
import com.zionhuang.music.playback.queue.Queue
import com.zionhuang.music.ui.adapters.ExploreAdapter
import com.zionhuang.music.ui.adapters.LoadStateAdapter
import com.zionhuang.music.ui.fragments.base.MainFragment
import com.zionhuang.music.viewmodels.ExploreViewModel
import com.zionhuang.music.viewmodels.PlaybackViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ExploreFragment : MainFragment<FragmentExploreBinding>() {
    companion object {
        private const val TAG = "ExplorationFragment"
    }

    private val viewModel by viewModels<ExploreViewModel>()
    private val playbackViewModel by activityViewModels<PlaybackViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
        }
        val exploreAdapter = ExploreAdapter().apply {
            addLoadStateListener { loadState ->
                binding.progressBar.isVisible = loadState.refresh is Loading
                binding.btnRetry.isVisible = loadState.refresh is LoadState.Error
                binding.tvErrorMsg.isVisible = loadState.refresh is LoadState.Error
                if (loadState.refresh is LoadState.Error) {
                    binding.tvErrorMsg.text = (loadState.refresh as LoadState.Error).error.localizedMessage
                }
            }
        }
        binding.btnRetry.setOnClickListener { exploreAdapter.retry() }
        binding.recyclerView.apply {
            adapter = exploreAdapter.withLoadStateFooter(LoadStateAdapter {
                exploreAdapter.retry()
            })
            addOnClickListener { pos, _ ->
                val video = exploreAdapter.getItemByPosition(pos)
                playbackViewModel.playMedia(SongParcel.fromVideo(video), Queue.QUEUE_SINGLE)
            }
        }

        lifecycleScope.launch {
            viewModel.flow.collectLatest {
                exploreAdapter.submitData(it)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_search_icon, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_search) {
            NavHostFragment.findNavController(this).navigate(R.id.action_explorationFragment_to_searchSuggestionFragment)
        }
        return true
    }
}