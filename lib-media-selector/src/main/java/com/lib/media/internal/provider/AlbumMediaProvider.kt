package com.lib.media.internal.provider

import android.database.Cursor
import android.os.Build
import android.os.Bundle
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import com.lib.media.MediaMimeType
import com.lib.media.entities.Album
import com.lib.media.internal.MediaSelectorParams
import com.lib.media.internal.loader.AlbumMediaLoader

/**
 *
 * @author: kpa
 * @date: 2023/4/24
 * @description:
 */
class AlbumMediaProvider : AbBaseProvider() {
    private var mLoaderManager: LoaderManager? = null

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        checkNotNull(mActivity) {
            "activity 不能为空"
        }
        var album = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            args?.getParcelable<Album>(ARGS_ALBUM, Album::class.java)
        } else {
            args?.getParcelable<Album>(ARGS_ALBUM)
        }
        return AlbumMediaLoader.newInstance(
            mActivity,
            album,
            album?.isAll == true && args?.getBoolean(ARGS_ENABLE_CAPTURE, false) == true,
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        MediaSelectorParams.resultCallback?.resultCallback(MediaMimeType.PHOTO_LIST_TYPE, data)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        MediaSelectorParams.resultCallback?.resultCallback(MediaMimeType.PHOTO_LIST_TYPE)
    }

    fun onAlbumSelected(album: Album) {
        var capture = album.isAll and MediaSelectorParams.capture
        if (capture) {
            album.addCaptureCount()
        }
        var bundle = Bundle().apply {
            putParcelable(ARGS_ALBUM, album)
            putBoolean(ARGS_ENABLE_CAPTURE, capture)
        }
        if (mLoaderManager != null) {
            mLoaderManager?.restartLoader(LIST_LOADER_ID, bundle, this)
            return
        }
        mLoaderManager = LoaderManager.getInstance(mActivity)
        mLoaderManager?.initLoader(LIST_LOADER_ID, bundle, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mLoaderManager?.let {
            mLoaderManager?.destroyLoader(TITLE_LOADER_ID)
            mLoaderManager = null
        }
    }
}
