package com.lib.media.listener

import android.database.Cursor
import com.lib.media.MediaMimeType

/**
 *
 * @author: kpa
 * @date: 2023/4/24
 * @description: 选择结果回调
 */
interface MediaSelectorResultCallback {
    /**
     * 结果回调
     * @param mimeType PHOTO_ALBUM_TYPE 分类专辑
     *                 PHOTO_LIST_TYPE 专辑列表
     * @param cursor 数据源
     */
    fun resultCallback(mimeType: MediaMimeType, cursor: Cursor? = null)
}
