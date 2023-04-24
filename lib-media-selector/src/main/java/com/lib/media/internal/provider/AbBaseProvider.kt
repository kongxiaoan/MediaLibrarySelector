package com.lib.media.internal.provider

import android.database.Cursor
import androidx.fragment.app.FragmentActivity
import androidx.loader.app.LoaderManager
import com.lib.media.internal.MediaSelectorParams

/**
 *
 * @author: kpa
 * @date: 2023/4/24
 * @description:
 */
abstract class AbBaseProvider : LoaderManager.LoaderCallbacks<Cursor> {
    companion object {
        const val TITLE_LOADER_ID = 1
        const val LIST_LOADER_ID = 2
        const val ARGS_ALBUM = "args_album"
        const val ARGS_ENABLE_CAPTURE = "args_enable_capture"
    }

    public var mActivity = MediaSelectorParams.mActivity?.get() as FragmentActivity
    public var mLoadFinished = false
    open fun onDestroy() {
        MediaSelectorParams.mCurrentSelection = 0
    }
}
