package com.omoemurao.videoloader

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

class AddVideoFragment : Fragment() {

    companion object {
        fun newInstance() = AddVideoFragment()
        private const val VIDEO_REQUEST = 100
    }

    private lateinit var viewModel: MainViewModel
    private var buttonAdd: Button? = null
    private var buttonPause: Button? = null
    private var buttonStop: Button? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        val root: View? = inflater.inflate(R.layout.fragment_add_video, container, false)
        buttonAdd = root?.findViewById(R.id.btn_add)
        buttonPause = root?.findViewById(R.id.btn_pause)
        buttonStop = root?.findViewById(R.id.btn_stop)

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        buttonAdd?.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    VIDEO_REQUEST
                )

            }
            val dialog = DialogAddVideo()
            dialog.openGalleryListener = View.OnClickListener {
                viewModel.setVideoData(dialog.getVideoData())
                selectVideoFromGallery()
            }
            dialog.returnListener = View.OnClickListener {

            }
            dialog.show(requireActivity().supportFragmentManager, "dialog1")


        }
        buttonPause?.setOnClickListener {
            viewModel.foregroundLoading()
        }
        buttonStop?.setOnClickListener {
            viewModel.backgroundLoading()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == VIDEO_REQUEST
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("Tag", "Permission granted")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == VIDEO_REQUEST && resultCode == RESULT_OK) {
            if (data?.data != null) {
                viewModel.addVideo(data.data!!)
            }
        }
    }

    private fun selectVideoFromGallery() {
        val intent: Intent =
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            } else {
                Intent(Intent.ACTION_PICK, MediaStore.Video.Media.INTERNAL_CONTENT_URI)
            }

        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra("return-data", true)
        startActivityForResult(intent, VIDEO_REQUEST)
    }
}