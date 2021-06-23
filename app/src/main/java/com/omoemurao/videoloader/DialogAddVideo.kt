package com.omoemurao.videoloader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment


class DialogAddVideo : DialogFragment() {


    var returnListener: View.OnClickListener? = null
    var openGalleryListener: View.OnClickListener? = null

    private val videoData: MutableMap<String, String> = mutableMapOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        val v: View = inflater.inflate(R.layout.dialog_addvideo, container, false)
        val titleEditText: EditText = v.findViewById(R.id.edit_title)
        val descEditText: EditText = v.findViewById(R.id.edit_desc)
        titleEditText.doAfterTextChanged { videoData["title"] = it.toString() }
        descEditText.doAfterTextChanged { videoData["description"] = it.toString() }

        v.findViewById<Button>(R.id.btn_return).setOnClickListener {
            dismiss()
            if (returnListener != null) {
                returnListener!!.onClick(view)
            }
        }

        v.findViewById<Button>(R.id.btn_gallery).setOnClickListener {
            if (videoData["title"] != null && videoData["description"] != null) {
                dismiss()
                if (openGalleryListener != null) {
                    openGalleryListener!!.onClick(view)
                }
            } else {
                titleEditText
                    .setBackgroundColor(ResourcesCompat.getColor(it.resources,
                        R.color.error,
                        it.context.theme))
                descEditText
                    .setBackgroundColor(ResourcesCompat.getColor(it.resources,
                        R.color.error,
                        it.context.theme))
            }
        }
        return v
    }

    fun getVideoData(): Map<String, String> {
        return videoData
    }

}