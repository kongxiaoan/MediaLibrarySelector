package com.kpa.test.ui

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kpa.test.R
import com.kpa.test.ui.adapter.AlbumMediaAdapter
import com.lib.media.MediaMimeType
import com.lib.media.MediaSelector
import com.lib.media.MimeType
import com.lib.media.entities.CaptureStrategy
import com.lib.media.listener.MediaSelectorResultCallback

class AlbumActivity : AppCompatActivity() {
    private var mAdapter: AlbumMediaAdapter? = null
    private var mRecyclerView: RecyclerView? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album)
        mRecyclerView = findViewById<View>(R.id.recyclerview) as RecyclerView?
        mAdapter = AlbumMediaAdapter(
            getApplicationContext(),
            mRecyclerView!!,
        )
        mRecyclerView?.run {
            setHasFixedSize(true)
            setLayoutManager(GridLayoutManager(getApplicationContext(), 3))
            setAdapter(mAdapter)
        }

        MediaSelector.form(this)
            .chooseMimeType(MimeType.ofAll())
            .showSingleMediaType(true)
            .capture(true)
            .captureBindParams(
                registerForActivityResult(
                    object : ActivityResultContracts.TakePicture() {
                    },
                ) {
                },
                CaptureStrategy(true, "com.kpa.test.fileProvider", "test"),
            )
            .forResult(object : MediaSelectorResultCallback {
                override fun resultCallback(mimeType: MediaMimeType, cursor: Cursor?) {
                    Log.d("MediaLibrarySelector", "type $mimeType")
                    if (mimeType === MediaMimeType.PHOTO_LIST_TYPE) {
                        mAdapter?.swapCursor(cursor)
                    } else {
                        // 分类布局处理
//                        albumAdapter.swapCursor(cursor);
                    }
                }
            })
    }

    companion object {
        fun start(activity: AppCompatActivity) {
            activity.startActivity(Intent(activity, AlbumActivity::class.java))
        }
    }
}
