package com.kpa.test.ui.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.kpa.test.R
import com.lib.media.entities.Item

class MediaGrid : SquareFrameLayout, View.OnClickListener {
    private var mThumbnail: ImageView? = null
    private var mCheckView: CheckView? = null
    private var mGifTag: ImageView? = null
    private var mVideoDuration: TextView? = null
    var media: Item? = null
        private set
    private var mPreBindInfo: PreBindInfo? = null
    private var mListener: OnMediaGridClickListener? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.media_grid_content, this, true)
        mThumbnail = findViewById<View>(R.id.media_thumbnail) as ImageView
        mCheckView = findViewById<View>(R.id.check_view) as CheckView
        mGifTag = findViewById<View>(R.id.gif) as ImageView
        mVideoDuration = findViewById<View>(R.id.video_duration) as TextView
        mThumbnail!!.setOnClickListener(this)
        mCheckView!!.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (mListener != null) {
            if (v === mThumbnail) {
                mListener!!.onThumbnailClicked(mThumbnail, media, mPreBindInfo!!.mViewHolder)
            } else if (v === mCheckView) {
                mListener!!.onCheckViewClicked(mCheckView, media, mPreBindInfo!!.mViewHolder)
            }
        }
    }

    fun preBindMedia(info: PreBindInfo?) {
        mPreBindInfo = info
    }

    fun bindMedia(item: Item?) {
        media = item
        setGifTag()
        initCheckView()
        setImage()
        setVideoDuration()
    }

    private fun setGifTag() {
        mGifTag!!.visibility = if (media!!.isGif) VISIBLE else GONE
    }

    private fun initCheckView() {
        mCheckView!!.setCountable(mPreBindInfo!!.mCheckViewCountable)
    }

    fun setCheckEnabled(enabled: Boolean) {
        mCheckView!!.isEnabled = enabled
    }

    fun setCheckedNum(checkedNum: Int) {
        mCheckView!!.setCheckedNum(checkedNum)
    }

    fun setChecked(checked: Boolean) {
        mCheckView!!.setChecked(checked)
    }

    private fun setImage() {
        if (media!!.isGif) {
            mThumbnail?.let {
                Glide.with(context)
                    .asBitmap() // some .jpeg files are actually gif
                    .load(media!!.contentUri)
                    .apply(
                        RequestOptions()
                            .override(mPreBindInfo!!.mResize, mPreBindInfo!!.mResize)
                            .centerCrop(),
                    )
                    .into(it)
            }
        } else {
            mThumbnail?.let {
                Glide.with(context)
                    .asBitmap() // some .jpeg files are actually gif
                    .load(media!!.contentUri)
                    .apply(
                        RequestOptions()
                            .override(mPreBindInfo!!.mResize, mPreBindInfo!!.mResize)
                            .centerCrop(),
                    )
                    .into(it)
            }
        }
    }

    private fun setVideoDuration() {
        if (media!!.isVideo) {
            mVideoDuration!!.visibility = VISIBLE
            mVideoDuration!!.text = DateUtils.formatElapsedTime(media!!.duration / 1000)
        } else {
            mVideoDuration!!.visibility = GONE
        }
    }

    fun setOnMediaGridClickListener(listener: OnMediaGridClickListener?) {
        mListener = listener
    }

    fun removeOnMediaGridClickListener() {
        mListener = null
    }

    interface OnMediaGridClickListener {
        fun onThumbnailClicked(thumbnail: ImageView?, item: Item?, holder: RecyclerView.ViewHolder?)
        fun onCheckViewClicked(checkView: CheckView?, item: Item?, holder: RecyclerView.ViewHolder?)
    }

    class PreBindInfo(
        var mResize: Int,
        var mPlaceholder: Drawable,
        var mCheckViewCountable: Boolean,
        var mViewHolder: RecyclerView.ViewHolder,
    )
}
