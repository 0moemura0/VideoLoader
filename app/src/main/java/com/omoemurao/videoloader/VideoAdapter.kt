package com.omoemurao.videoloader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VideoAdapter : RecyclerView.Adapter<VideoAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.title)
        private val description: TextView = itemView.findViewById(R.id.description)
        private val addingDate: TextView = itemView.findViewById(R.id.adding_date)

        fun bind(video: Video) {
            title.text = video.title
            description.text = video.description
            addingDate.text = video.views.toString()
        }

    }

    private var mDataList: MutableList<Video> = ArrayList()

    var hasItems = false


    fun setList(dataList: List<Video>) {
        mDataList.addAll(dataList)
        hasItems = true
        notifyDataSetChanged()
    }

    fun updateItems(itemsList: List<Video>) {
        mDataList.clear()
        setList(itemsList)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataList[position])

    }

    override fun getItemCount(): Int {
        return mDataList.count()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video_list, parent, false))
    }
}
