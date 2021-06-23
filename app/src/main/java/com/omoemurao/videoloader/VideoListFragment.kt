package com.omoemurao.videoloader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class VideoListFragment : Fragment() {
    companion object {
        fun newInstance() = VideoListFragment()
    }

    private lateinit var viewModel: MainViewModel
    private val adapter = VideoAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val root: View? = inflater.inflate(R.layout.fragment_video_list, container, false)

        val list: RecyclerView? = root?.findViewById(R.id.list)
        val button: Button? = root?.findViewById(R.id.btn_add)

        if (list != null) {
            list.layoutManager = LinearLayoutManager(requireContext())
            list.adapter = adapter
            list.setHasFixedSize(true)
        }

        button?.setOnClickListener {
            findNavController().navigate(R.id.action_videoListFragment_to_addVideoFragment)
        }

        return root

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.getList()

        viewModel.listLiveData.observe(viewLifecycleOwner,
            { t ->
                if (t != null)
                    adapter.updateItems(t)
            })
    }
}