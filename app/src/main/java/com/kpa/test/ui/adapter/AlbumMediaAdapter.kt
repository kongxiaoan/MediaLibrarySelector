package com.kpa.test.ui.adapter

import android.content.Context
import android.database.Cursor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kpa.test.R
import com.kpa.test.ui.widget.MediaGrid
import com.lib.media.MediaSelector.takePictures
import com.lib.media.entities.Item

class AlbumMediaAdapter(val context: Context, recyclerView: RecyclerView) :
    RecyclerViewCursorAdapter<RecyclerView.ViewHolder>(null) {

    private val mRecyclerView: RecyclerView
    private var mImageResize = 0

    init {
        mRecyclerView = recyclerView
    }

    override fun getItemViewType(position: Int, cursor: Cursor): Int {
        val capture: Boolean = Item.valueOf(cursor).isCapture
        return if (capture) VIEW_TYPE_CAPTURE else VIEW_TYPE_MEDIA
    }

    @Override
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_CAPTURE) {
            val v: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.photo_capture_item, parent, false)
            val holder = CaptureViewHolder(v)
            holder.itemView.setOnClickListener { takePictures() }
            holder
        } else {
            val v: View =
                LayoutInflater.from(parent.context).inflate(R.layout.media_grid_item, parent, false)
            MediaViewHolder(v)
        }
    }

    private class MediaViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val mMediaGrid: MediaGrid

        init {
            mMediaGrid = itemView as MediaGrid
        }
    }

    @Override
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, cursor: Cursor) {
        if (holder is CaptureViewHolder) {
            val captureViewHolder = holder
            val drawables = captureViewHolder.mHint.compoundDrawables
            for (i in drawables.indices) {
                val drawable = drawables[i]
                if (drawable != null) {
                    val state = drawable.constantState ?: continue
                    val newDrawable = state.newDrawable().mutate()
                    newDrawable.bounds = drawable.bounds
                    drawables[i] = newDrawable
                }
            }
            captureViewHolder.mHint.setCompoundDrawables(
                drawables[0],
                drawables[1],
                drawables[2],
                drawables[3],
            )
        } else {
            val mediaViewHolder = holder as MediaViewHolder
            val item: Item = Item.valueOf(cursor)
            Log.d("MediaViewHolder", "uri = ${item.contentUri}")
            mediaViewHolder.mMediaGrid.preBindMedia(
                MediaGrid.PreBindInfo(
                    getImageResize(mediaViewHolder.mMediaGrid.getContext()),
                    context.resources.getDrawable(R.drawable.ic_launcher_background),
                    false,
                    holder,
                ),
            )
            mediaViewHolder.mMediaGrid.bindMedia(item)
            //            mediaViewHolder.mMediaGrid.setOnMediaGridClickListener(this);
//            setCheckStatus(item, mediaViewHolder.mMediaGrid);
        }
    }

    private fun getImageResize(context: Context): Int {
        if (mImageResize == 0) {
            val lm = mRecyclerView.layoutManager
            val spanCount = (lm as GridLayoutManager?)!!.spanCount
            val screenWidth = context.resources.displayMetrics.widthPixels
            val availableWidth = screenWidth - context.resources.getDimensionPixelSize(
                R.dimen.media_grid_spacing,
            ) * (spanCount - 1)
            mImageResize = availableWidth / spanCount
            mImageResize = (mImageResize * 0.5f).toInt()
        }
        return mImageResize
    }

    private class CaptureViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val mHint: TextView

        init {
            mHint = itemView.findViewById<View>(R.id.hint) as TextView
        }
    }

    companion object {
        private const val VIEW_TYPE_CAPTURE = 0x01
        private const val VIEW_TYPE_MEDIA = 0x02
    }
}
