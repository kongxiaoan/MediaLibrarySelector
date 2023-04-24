package com.lib.media.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

/**
 *
 * @author: kpa
 * @date: 2023/4/24
 * @description:
 */
object FileUtils {
    private const val SCHEME_CONTENT = "content"

    @SuppressLint("Range")
    fun getPath(resolver: ContentResolver, uri: Uri?): String? {
        if (uri == null) {
            return null
        }
        if (SCHEME_CONTENT == uri.scheme) {
            var cursor: Cursor? = null
            return try {
                cursor = resolver.query(
                    uri,
                    arrayOf(MediaStore.Images.ImageColumns.DATA),
                    null,
                    null,
                    null,
                )
                if (cursor == null || !cursor.moveToFirst()) {
                    null
                } else {
                    cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA))
                }
            } finally {
                cursor?.close()
            }
        }
        return uri.path
    }
}
