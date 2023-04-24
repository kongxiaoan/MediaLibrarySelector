package com.lib.media.internal.loader

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.database.MergeCursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.loader.content.CursorLoader
import com.lib.media.MimeType
import com.lib.media.entities.Album
import com.lib.media.internal.provider.MediaSelectorProvider

/**
 *
 * @author: kpa
 * @date: 2023/4/24
 * @description: 数据加载器
 */
class AlbumLoader : CursorLoader {

    companion object {
        private val COLUMN_BUCKET_ID = "bucket_id"
        private val COLUMN_BUCKET_DISPLAY_NAME = "bucket_display_name"
        val COLUMN_URI = "uri"
        val COLUMN_COUNT = "count"
        private val QUERY_URI = MediaStore.Files.getContentUri("external")

        private val COLUMNS = arrayOf(
            MediaStore.Files.FileColumns._ID,
            COLUMN_BUCKET_ID,
            COLUMN_BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            COLUMN_URI,
            COLUMN_COUNT,
        )

        private val PROJECTION = arrayOf(
            MediaStore.Files.FileColumns._ID,
            COLUMN_BUCKET_ID,
            COLUMN_BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            "COUNT(*) AS $COLUMN_COUNT",
        )

        private val PROJECTION_29 = arrayOf(
            MediaStore.Files.FileColumns._ID,
            COLUMN_BUCKET_ID,
            COLUMN_BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
        )

        // === params for showSingleMediaType: false ===
        private val SELECTION = (
            "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" +
                " OR " +
                MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)" +
                " AND " + MediaStore.MediaColumns.SIZE + ">0" +
                ") GROUP BY (bucket_id"
            )
        private val SELECTION_29 = (
            "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" +
                " OR " +
                MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)" +
                " AND " + MediaStore.MediaColumns.SIZE + ">0"
            )
        private val SELECTION_ARGS = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
        )
        // =============================================

        // =============================================
        // === params for showSingleMediaType: true ===
        private val SELECTION_FOR_SINGLE_MEDIA_TYPE = (
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" +
                " AND " + MediaStore.MediaColumns.SIZE + ">0" +
                ") GROUP BY (bucket_id"
            )
        private val SELECTION_FOR_SINGLE_MEDIA_TYPE_29 = (
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" +
                " AND " + MediaStore.MediaColumns.SIZE + ">0"
            )

        private fun getSelectionArgsForSingleMediaType(mediaType: Int): Array<String> {
            return arrayOf(mediaType.toString())
        }
        // =============================================

        // =============================================
        // === params for showSingleMediaType: true ===
        private val SELECTION_FOR_SINGLE_MEDIA_GIF_TYPE =
            (
                MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" +
                    " AND " + MediaStore.MediaColumns.SIZE + ">0" +
                    " AND " + MediaStore.MediaColumns.MIME_TYPE + "=?" +
                    ") GROUP BY (bucket_id"
                )
        private val SELECTION_FOR_SINGLE_MEDIA_GIF_TYPE_29 =
            (
                MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" +
                    " AND " + MediaStore.MediaColumns.SIZE + ">0" +
                    " AND " + MediaStore.MediaColumns.MIME_TYPE + "=?"
                )

        private fun getSelectionArgsForSingleMediaGifType(mediaType: Int): Array<String> {
            return arrayOf(mediaType.toString(), "image/gif")
        }
        // =============================================

        // =============================================
        private val BUCKET_ORDER_BY = "datetaken DESC"

        /**
         * @return 是否是 Android 10 （Q） 之前的版本
         */
        private fun beforeAndroidTen(): Boolean {
            return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
        }

        fun newInstance(context: Context): CursorLoader {
            val selection: String
            val selectionArgs: Array<String>
            if (MediaSelectorProvider.onlyShowGif()) {
                selection =
                    if (beforeAndroidTen()) SELECTION_FOR_SINGLE_MEDIA_GIF_TYPE else SELECTION_FOR_SINGLE_MEDIA_GIF_TYPE_29
                selectionArgs = getSelectionArgsForSingleMediaGifType(
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE,
                )
            } else if (MediaSelectorProvider.onlyShowImages()) {
                selection =
                    if (beforeAndroidTen()) SELECTION_FOR_SINGLE_MEDIA_TYPE else SELECTION_FOR_SINGLE_MEDIA_TYPE_29
                selectionArgs = getSelectionArgsForSingleMediaType(
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE,
                )
            } else if (MediaSelectorProvider.onlyShowVideos()) {
                selection =
                    if (beforeAndroidTen()) SELECTION_FOR_SINGLE_MEDIA_TYPE else SELECTION_FOR_SINGLE_MEDIA_TYPE_29
                selectionArgs = getSelectionArgsForSingleMediaType(
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO,
                )
            } else {
                selection = if (beforeAndroidTen()) SELECTION else SELECTION_29
                selectionArgs = SELECTION_ARGS
            }
            return AlbumLoader(context, selection, selectionArgs)
        }
    }

    constructor(context: Context, selection: String, selectionArgs: Array<String>) :
        super(
            context,
            QUERY_URI,
            if (beforeAndroidTen()) PROJECTION else PROJECTION_29,
            selection,
            selectionArgs,
            BUCKET_ORDER_BY,
        )

    @SuppressLint("Range")
    override fun loadInBackground(): Cursor? {
        val albums = super.loadInBackground()
        val allAlbum = MatrixCursor(COLUMNS)
        return if (beforeAndroidTen()) {
            var totalCount = 0
            var allAlbumCoverUri: Uri? = null
            val otherAlbums = MatrixCursor(COLUMNS)
            if (albums != null) {
                while (albums.moveToNext()) {
                    val fileId = albums.getLong(
                        albums.getColumnIndex(MediaStore.Files.FileColumns._ID),
                    )
                    val bucketId = albums.getLong(
                        albums.getColumnIndex(COLUMN_BUCKET_ID),
                    )
                    val bucketDisplayName = albums.getString(
                        albums.getColumnIndex(COLUMN_BUCKET_DISPLAY_NAME),
                    )
                    val mimeType = albums.getString(
                        albums.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE),
                    )
                    val uri = getUri(albums)
                    val count = albums.getInt(albums.getColumnIndex(COLUMN_COUNT))
                    otherAlbums.addRow(
                        arrayOf(
                            java.lang.Long.toString(fileId),
                            java.lang.Long.toString(bucketId),
                            bucketDisplayName,
                            mimeType,
                            uri.toString(),
                            count.toString(),
                        ),
                    )
                    totalCount += count
                }
                if (albums.moveToFirst()) {
                    allAlbumCoverUri = getUri(albums)
                }
            }
            allAlbum.addRow(
                arrayOf<String?>(
                    Album.ALBUM_ID_ALL,
                    Album.ALBUM_ID_ALL,
                    Album.ALBUM_NAME_ALL,
                    null,
                    allAlbumCoverUri?.toString(),
                    totalCount.toString(),
                ),
            )
            MergeCursor(arrayOf<Cursor>(allAlbum, otherAlbums))
        } else {
            var totalCount = 0
            var allAlbumCoverUri: Uri? = null

            // Pseudo GROUP BY
            val countMap: MutableMap<Long, Long> = HashMap()
            if (albums != null) {
                while (albums.moveToNext()) {
                    val bucketId = albums.getLong(albums.getColumnIndex(COLUMN_BUCKET_ID))
                    var count = countMap[bucketId]
                    if (count == null) {
                        count = 1L
                    } else {
                        count++
                    }
                    countMap[bucketId] = count
                }
            }
            val otherAlbums = MatrixCursor(COLUMNS)
            if (albums != null) {
                if (albums.moveToFirst()) {
                    allAlbumCoverUri = getUri(albums)
                    val done: MutableSet<Long> = HashSet()
                    do {
                        val bucketId = albums.getLong(albums.getColumnIndex(COLUMN_BUCKET_ID))
                        if (done.contains(bucketId)) {
                            continue
                        }
                        val fileId = albums.getLong(
                            albums.getColumnIndex(MediaStore.Files.FileColumns._ID),
                        )
                        val bucketDisplayName = albums.getString(
                            albums.getColumnIndex(COLUMN_BUCKET_DISPLAY_NAME),
                        )
                        val mimeType = albums.getString(
                            albums.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE),
                        )
                        val uri = getUri(albums)
                        val count = countMap[bucketId]!!
                        otherAlbums.addRow(
                            arrayOf(
                                java.lang.Long.toString(fileId),
                                java.lang.Long.toString(bucketId),
                                bucketDisplayName,
                                mimeType,
                                uri.toString(),
                                count.toString(),
                            ),
                        )
                        done.add(bucketId)
                        totalCount += count.toInt()
                    } while (albums.moveToNext())
                }
            }
            allAlbum.addRow(
                arrayOf<String?>(
                    Album.ALBUM_ID_ALL,
                    Album.ALBUM_ID_ALL,
                    Album.ALBUM_NAME_ALL,
                    null,
                    allAlbumCoverUri?.toString(),
                    totalCount.toString(),
                ),
            )
            MergeCursor(arrayOf<Cursor>(allAlbum, otherAlbums))
        }
    }

    @SuppressLint("Range")
    private fun getUri(cursor: Cursor): Uri {
        val id =
            cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID))
        val mimeType = cursor.getString(
            cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE),
        )
        val contentUri: Uri
        contentUri = if (MimeType.isImage(mimeType)) {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        } else if (MimeType.isVideo(mimeType)) {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        } else {
            // ?
            MediaStore.Files.getContentUri("external")
        }
        return ContentUris.withAppendedId(contentUri, id)
    }

    override fun onContentChanged() {
        // FIXME a dirty way to fix loading multiple times
    }
}
