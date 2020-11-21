package sk.stuba.fei.mv.android.zaverecne.feed

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import sk.stuba.fei.mv.android.zaverecne.R
import sk.stuba.fei.mv.android.zaverecne.databinding.FeedFragmentBinding
import sk.stuba.fei.mv.android.zaverecne.repository.MasterRepository

class FeedFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FeedFragmentBinding.inflate(inflater)
        val application = requireNotNull(this.activity).application
        val viewModelFactory = FeedViewModelFactory(application)
        val feedViewModel = ViewModelProvider(this, viewModelFactory).get(FeedViewModel::class.java)

        binding.lifecycleOwner = this

        binding.viewModel = feedViewModel

        return binding.root
    }
}