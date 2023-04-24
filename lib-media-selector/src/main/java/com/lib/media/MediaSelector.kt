package com.lib.media

import android.database.Cursor
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.lib.media.entities.CaptureStrategy
import com.lib.media.internal.MediaSelectorParams
import com.lib.media.internal.provider.MediaSelectorProvider
import com.lib.media.listener.MediaSelectorResultCallback

/**
 *
 * @author: kpa
 * @date: 2023/4/24
 * @description: 媒体选择器
 */
object MediaSelector {

    private val mMediaSelectorParams = MediaSelectorParams.getCleanInstance()

    fun form(activity: FragmentActivity) = apply {
        init(activity)
    }

    fun form(fragment: Fragment) = apply {
        init(fragment.requireActivity())
    }

    /**
     * 选择媒体类型
     */
    fun chooseMimeType(mimeType: Set<MimeType>) = apply {
        mMediaSelectorParams.mimeTypeSet = mimeType
    }

    /**
     * 开启拍照 isAll 类型时展示
     *
     * @return
     */
    fun capture(capture: Boolean) = apply {
        mMediaSelectorParams.capture = capture
    }

    /**
     * 开启拍照 isAll 类型时展示
     * capture true 时必须调用
     *
     * @return
     */
    fun captureBindParams(
        myLauncher: ActivityResultLauncher<Uri>,
        strategy: CaptureStrategy,
    ) = apply {
        mMediaSelectorParams.myLauncher = myLauncher
        mMediaSelectorParams.strategy = strategy
        return this
    }

    fun showSingleMediaType(showSingleMediaType: Boolean) = apply {
        mMediaSelectorParams.showSingleMediaType = showSingleMediaType
        return this
    }

    fun forResult(callback: MediaSelectorResultCallback) {
        mMediaSelectorParams.resultCallback = callback
        start()
    }

    /**
     * 更新选中
     */
    fun updateSelection(selection: Int, cursor: Cursor) {
        MediaSelectorProvider.updateCurrentSelection(selection, cursor)
    }

    /**
     * 拍照
     */
    fun takePictures() {
        MediaSelectorProvider.takePictures()
    }

    private fun start() {
        MediaSelectorProvider.startLoadMediaFile()
    }

    /**
     * 获取拍照后的地址Uri
     */
    fun getCurrentPhotoUri(): Uri? {
        return MediaSelectorProvider.getCurrentPhotoUri()
    }

    private fun init(activity: FragmentActivity) {
        mMediaSelectorParams.bind(activity)
        activity.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> {
                        MediaSelectorProvider.onSaveCurrentPosition()
                    }

                    Lifecycle.Event.ON_RESUME -> {
                        MediaSelectorProvider.onRestoreCurrentPosition()
                    }

                    Lifecycle.Event.ON_DESTROY -> {
                        MediaSelectorProvider.onDestroy()
                    }

                    else -> {
                    }
                }
            }
        })
    }
}
