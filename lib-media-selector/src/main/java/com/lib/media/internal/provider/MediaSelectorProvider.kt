package com.lib.media.internal.provider

import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import com.lib.media.MimeType
import com.lib.media.entities.Album
import com.lib.media.internal.MediaSelectorParams
import com.lib.media.utils.MediaStoreCompat

/**
 *
 * @author: kpa
 * @date: 2023/4/24
 * @description: 媒体数据加载
 */
internal object MediaSelectorProvider {
    private var albumProvider = AlbumProvider()
    private var albumMediaProvider = AlbumMediaProvider()
    private var mediaStoreCompat: MediaStoreCompat? = null
    private var sps: SharedPreferences? = null
    const val MEDIA_POSITION_KEY = "media_position_key"

    fun startLoadMediaFile() {
        albumProvider.init()
        if (MediaSelectorParams.capture) {
            mediaStoreCompat =
                MediaStoreCompat(MediaSelectorParams.mActivity?.get() as FragmentActivity)
            mediaStoreCompat?.setCaptureStrategy(MediaSelectorParams.strategy)
        }
        sps = MediaSelectorParams.mActivity?.get()?.getSharedPreferences(
            "default",
            Context.MODE_PRIVATE,
        )
    }

    fun updateCurrentSelection(selection: Int, cursor: Cursor) {
        MediaSelectorParams.mCurrentSelection = selection
        cursor.moveToPosition(selection)
        var album = Album.valueOf(cursor)
        onAlbumSelected(album)
    }

    fun onlyShowImages(): Boolean {
        return MediaSelectorParams.showSingleMediaType && MimeType.ofImage()
            .containsAll(MediaSelectorParams.mimeTypeSet)
    }

    fun onlyShowVideos(): Boolean {
        return MediaSelectorParams.showSingleMediaType && MimeType.ofVideo()
            .containsAll(MediaSelectorParams.mimeTypeSet)
    }

    fun onlyShowGif(): Boolean {
        return MediaSelectorParams.showSingleMediaType && MimeType.ofGif()
            .equals(MediaSelectorParams.mimeTypeSet)
    }

    fun onAlbumSelected(album: Album) {
        albumMediaProvider.onAlbumSelected(album)
    }

    fun onDestroy() {
        albumProvider.onDestroy()
        albumMediaProvider.onDestroy()
        sps?.getInt(MEDIA_POSITION_KEY, 0)
    }

    fun onSaveCurrentPosition() {
        sps?.edit()?.putInt(MEDIA_POSITION_KEY, MediaSelectorParams.mCurrentSelection)
    }

    fun onRestoreCurrentPosition() {
        sps?.getInt(MEDIA_POSITION_KEY, MediaSelectorParams.mCurrentSelection)
        albumProvider.init()
    }

    fun takePictures() {
        mediaStoreCompat?.run {
            checkNotNull(MediaSelectorParams.myLauncher)
            MediaSelectorParams.myLauncher?.let { dispatchCaptureIntent(it) }
        }
    }

    fun getCurrentPhotoUri(): Uri? {
        return mediaStoreCompat?.currentPhotoUri
    }
}
