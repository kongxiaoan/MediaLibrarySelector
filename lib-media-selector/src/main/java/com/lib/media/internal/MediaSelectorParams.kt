package com.lib.media.internal

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.FragmentActivity
import com.lib.media.MimeType
import com.lib.media.entities.CaptureStrategy
import com.lib.media.listener.MediaSelectorResultCallback
import java.lang.ref.WeakReference

/**
 *
 * @author: kpa
 * @date: 2023/4/24
 * @description: 媒体选择库参数
 */
internal object MediaSelectorParams {
    var mActivity: WeakReference<FragmentActivity>? = null

    var mCurrentSelection = 0

    /**
     * 媒体类型
     */
    var mimeTypeSet: Set<MimeType> = emptySet()

    /**
     * 控制是否现实单独类型
     */
    var showSingleMediaType = false

    /**
     * 是否启动拍照
     */
    var capture = false

    /**
     * 拍照参数
     */
    var strategy: CaptureStrategy? = null

    /**
     * 用于拍照结果回调，启动相机
     */
    var myLauncher: ActivityResultLauncher<Uri>? = null

    /**
     * 结果回调
     */
    var resultCallback: MediaSelectorResultCallback? = null

    fun getCleanInstance() = apply {
        mimeTypeSet = emptySet()
        mActivity = null
        capture = false
        strategy = null
        myLauncher = null
        showSingleMediaType = false
        resultCallback = null
        mCurrentSelection = 0
    }

    fun bind(activity: FragmentActivity) {
        mActivity = WeakReference(activity)
    }
}
