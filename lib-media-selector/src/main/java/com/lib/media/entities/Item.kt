package com.lib.media.entities

import android.annotation.SuppressLint
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore
import com.lib.media.MimeType

/**
 * 相册媒体实体
 */
class Item : Parcelable {
    val id: Long
    val mimeType: String?
    val contentUri: Uri?
    val size: Long
    val duration: Long

    private constructor(id: Long, mimeType: String, size: Long, duration: Long) {
        this.id = id
        this.mimeType = mimeType
        val contentUri: Uri
        contentUri = if (isImage) {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        } else if (isVideo) {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        } else {
            // ?
            MediaStore.Files.getContentUri("external")
        }
        this.contentUri = ContentUris.withAppendedId(contentUri, id)
        this.size = size
        this.duration = duration
    }

    private constructor(source: Parcel) {
        id = source.readLong()
        mimeType = source.readString()
        contentUri = source.readParcelable(Uri::class.java.classLoader)
        size = source.readLong()
        duration = source.readLong()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(id)
        dest.writeString(mimeType)
        dest.writeParcelable(contentUri, 0)
        dest.writeLong(size)
        dest.writeLong(duration)
    }

    val isCapture: Boolean
        get() = id == ITEM_ID_CAPTURE
    val isImage: Boolean
        get() = MimeType.isImage(mimeType)
    val isGif: Boolean
        get() = MimeType.isGif(mimeType)
    val isVideo: Boolean
        get() = MimeType.isVideo(mimeType)

    override fun equals(obj: Any?): Boolean {
        if (obj !is Item) {
            return false
        }
        val other = obj
        return id == other.id && (mimeType != null && mimeType == other.mimeType || mimeType == null) && other.mimeType == null && (contentUri != null && contentUri == other.contentUri) == null && other.contentUri == null && size == other.size && duration == other.duration
    }

    override fun hashCode(): Int {
        var result = 1
        result = 31 * result + java.lang.Long.valueOf(id).hashCode()
        if (mimeType != null) {
            result = 31 * result + mimeType.hashCode()
        }
        result = 31 * result + contentUri.hashCode()
        result = 31 * result + java.lang.Long.valueOf(size).hashCode()
        result = 31 * result + java.lang.Long.valueOf(duration).hashCode()
        return result
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Item?> = object : Parcelable.Creator<Item?> {
            override fun createFromParcel(source: Parcel): Item? {
                return Item(source)
            }

            override fun newArray(size: Int): Array<Item?> {
                return arrayOfNulls(size)
            }
        }
        const val ITEM_ID_CAPTURE: Long = -1
        const val ITEM_DISPLAY_NAME_CAPTURE = "Capture"

        @SuppressLint("Range")
        fun valueOf(cursor: Cursor): Item {
            return Item(
                cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)),
                cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)),
                cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)),
                cursor.getLong(cursor.getColumnIndex("duration")),
            )
        }
    }
}
