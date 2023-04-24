package com.lib.media.internal.loader

import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.database.MergeCursor
import android.provider.MediaStore
import androidx.loader.content.CursorLoader
import com.lib.media.entities.Album
import com.lib.media.entities.Item
import com.lib.media.internal.provider.MediaSelectorProvider
import com.lib.media.utils.MediaStoreCompat

/**
 *
 * @author: kpa
 * @date: 2023/4/24
 * @description:
 */
class AlbumMediaLoader : CursorLoader {
    private var mEnableCapture = false

    constructor(
        context: Context,
        selection: String?,
        selectionArgs: Array<out String>?,
        capture: Boolean,
    ) : super(context, QUERY_URI, PROJECTION, selection, selectionArgs, ORDER_BY) {
        mEnableCapture = capture
    }

    companion object {
        private val QUERY_URI = MediaStore.Files.getContentUri("external")
        private val PROJECTION = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.SIZE,
            "duration",
        )

        // ===============================================================
        // === params for album ALL && showSingleMediaType: true && MineType=="image/gif"
        private const val SELECTION_ALL_FOR_GIF = (
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" +
                " AND " +
                MediaStore.MediaColumns.MIME_TYPE + "=?" +
                " AND " + MediaStore.MediaColumns.SIZE + ">0"
            )

        private fun getSelectionArgsForGifType(mediaType: Int): Array<String> {
            return arrayOf(mediaType.toString(), "image/gif")
        }

        // ===========================================================
        // === params for album ALL && showSingleMediaType: true ===
        private const val SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE =
            (
                MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" +
                    " AND " + MediaStore.MediaColumns.SIZE + ">0"
                )

        private fun getSelectionArgsForSingleMediaType(mediaType: Int): Array<String> {
            return arrayOf(mediaType.toString())
        }
        // =========================================================

        // === params for album ALL && showSingleMediaType: false ===
        private const val SELECTION_ALL = (
            "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" +
                " OR " +
                MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)" +
                " AND " + MediaStore.MediaColumns.SIZE + ">0"
            )
        private val SELECTION_ALL_ARGS = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
        )

        private fun getSelectionAlbumArgsForGifType(
            mediaType: Int,
            albumId: String,
        ): Array<String> {
            return arrayOf(mediaType.toString(), albumId, "image/gif")
        }

        // ===============================================================
        // === params for ordinary album && showSingleMediaType: true  && MineType=="image/gif" ===
        private const val SELECTION_ALBUM_FOR_GIF = (
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" +
                " AND " +
                " bucket_id=?" +
                " AND " +
                MediaStore.MediaColumns.MIME_TYPE + "=?" +
                " AND " + MediaStore.MediaColumns.SIZE + ">0"
            )

        // ===============================================================
        private const val SELECTION_ALBUM_FOR_SINGLE_MEDIA_TYPE =
            (
                MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" +
                    " AND " +
                    " bucket_id=?" +
                    " AND " + MediaStore.MediaColumns.SIZE + ">0"
                )

        private fun getSelectionAlbumArgsForSingleMediaType(
            mediaType: Int,
            albumId: String,
        ): Array<String> {
            return arrayOf(mediaType.toString(), albumId)
        }

        // =========================================================
        private const val SELECTION_ALBUM = (
            "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" +
                " OR " +
                MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)" +
                " AND " +
                " bucket_id=?" +
                " AND " + MediaStore.MediaColumns.SIZE + ">0"
            )

        private fun getSelectionAlbumArgs(albumId: String): Array<String> {
            return arrayOf(
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
                albumId,
            )
        }

        // ===========================================================
        private const val ORDER_BY = MediaStore.Images.Media.DATE_TAKEN + " DESC"

        public fun newInstance(context: Context, album: Album?, capture: Boolean): CursorLoader {
            var selection: String = ""
            var selectionArgs: Array<String> = emptyArray()
            var enableCapture: Boolean = false
            if (album?.isAll == true) {
                if (MediaSelectorProvider.onlyShowGif()) {
                    selection = SELECTION_ALL_FOR_GIF
                    selectionArgs =
                        getSelectionArgsForGifType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
                } else if (MediaSelectorProvider.onlyShowImages()) {
                    selection = SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE
                    selectionArgs =
                        getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
                } else if (MediaSelectorProvider.onlyShowVideos()) {
                    selection = SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE
                    selectionArgs = getSelectionArgsForSingleMediaType(
                        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO,
                    )
                } else {
                    selection = SELECTION_ALL
                    selectionArgs = SELECTION_ALL_ARGS
                }
                enableCapture = capture
            } else {
                if (MediaSelectorProvider.onlyShowGif()) {
                    selection = SELECTION_ALBUM_FOR_GIF
                    selectionArgs = getSelectionAlbumArgsForGifType(
                        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE,
                        album?.id ?: "",
                    )
                } else if (MediaSelectorProvider.onlyShowImages()) {
                    selection = SELECTION_ALBUM_FOR_SINGLE_MEDIA_TYPE
                    selectionArgs = getSelectionAlbumArgsForSingleMediaType(
                        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE,
                        album?.id ?: "",
                    )
                } else if (MediaSelectorProvider.onlyShowVideos()) {
                    selection = SELECTION_ALBUM_FOR_SINGLE_MEDIA_TYPE
                    selectionArgs = getSelectionAlbumArgsForSingleMediaType(
                        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO,
                        album?.id ?: "",
                    )
                } else {
                    selection = SELECTION_ALBUM
                    selectionArgs = getSelectionAlbumArgs(album?.id ?: "")
                }
                enableCapture = false
            }

            return AlbumMediaLoader(context, selection, selectionArgs, enableCapture)
        }
    }

    override fun loadInBackground(): Cursor? {
        val result = super.loadInBackground()
        if (!mEnableCapture || !MediaStoreCompat.hasCameraFeature(context)) {
            return result
        }
        val dummy = MatrixCursor(PROJECTION)
        dummy.addRow(arrayOf<Any>(Item.ITEM_ID_CAPTURE, Item.ITEM_DISPLAY_NAME_CAPTURE, "", 0, 0))
        return MergeCursor(arrayOf(dummy, result))
    }
}
