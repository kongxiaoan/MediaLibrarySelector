package com.lib.media.internal.provider

import android.database.Cursor
import android.os.Bundle
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import com.lib.media.MediaMimeType
import com.lib.media.entities.Album
import com.lib.media.internal.MediaSelectorParams
import com.lib.media.internal.loader.AlbumLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 *
 * @author: kpa
 * @date: 2023/4/24
 * @description: 专辑加载器
 */
class AlbumProvider : AbBaseProvider() {
    companion object {
        const val TITLE_LOADER_ID = 1
    }

    private var mLoaderManager: LoaderManager? = null
    fun init() {
        if (mLoaderManager != null) {
            mLoaderManager?.restartLoader(TITLE_LOADER_ID, null, this)
            return
        }
        mLoaderManager =
            LoaderManager.getInstance(mActivity)
        mLoaderManager?.initLoader(TITLE_LOADER_ID, null, this)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        checkNotNull(mActivity) {
            "activity 不能为空"
        }
        mLoadFinished = false
        return AlbumLoader.newInstance(mActivity)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        MediaSelectorParams.resultCallback?.resultCallback(MediaMimeType.PHOTO_ALBUM_TYPE)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        checkNotNull(mActivity) {
            "activity 不能为空"
        }
        if (!mLoadFinished && data != null) {
            mLoadFinished = true
            MediaSelectorParams.resultCallback?.resultCallback(MediaMimeType.PHOTO_ALBUM_TYPE, data)
            MainScope().launch(Dispatchers.Main) {
                data?.moveToPosition(MediaSelectorParams.mCurrentSelection)
                var album = Album.valueOf(data)
                MediaSelectorProvider.onAlbumSelected(album)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mLoaderManager?.let {
            mLoaderManager?.destroyLoader(AbBaseProvider.TITLE_LOADER_ID)
            mLoaderManager = null
        }
    }
}
