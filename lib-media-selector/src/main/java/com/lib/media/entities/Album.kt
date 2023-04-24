package com.lib.media.entities

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.lib.media.R
import com.lib.media.internal.loader.AlbumLoader

/**
 * 专辑 实体
 */
class Album : Parcelable {
    val id: String?
    val coverUri: Uri?
    private val mDisplayName: String?
    var count: Long
        private set

    constructor(id: String?, coverUri: Uri?, albumName: String?, count: Long) {
        this.id = id
        this.coverUri = coverUri
        mDisplayName = albumName
        this.count = count
    }

    private constructor(source: Parcel) {
        id = source.readString()
        coverUri = source.readParcelable(Uri::class.java.classLoader)
        mDisplayName = source.readString()
        count = source.readLong()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeParcelable(coverUri, 0)
        dest.writeString(mDisplayName)
        dest.writeLong(count)
    }

    fun addCaptureCount() {
        count++
    }

    fun getDisplayName(context: Context): String? {
        return if (isAll) {
            context.getString(R.string.album_name_all)
        } else {
            mDisplayName
        }
    }

    val isAll: Boolean
        get() = ALBUM_ID_ALL == id
    val isEmpty: Boolean
        get() = count == 0L

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Album?> = object : Parcelable.Creator<Album?> {
            override fun createFromParcel(source: Parcel): Album? {
                return Album(source)
            }

            override fun newArray(size: Int): Array<Album?> {
                return arrayOfNulls(size)
            }
        }
        val ALBUM_ID_ALL: String = (-1).toString()
        const val ALBUM_NAME_ALL = "All"

        /**
         * Constructs a new [Album] entity from the [Cursor].
         * This method is not responsible for managing cursor resource, such as close, iterate, and so on.
         */
        @SuppressLint("Range")
        fun valueOf(cursor: Cursor): Album {
            val clumn = cursor.getString(cursor.getColumnIndex(AlbumLoader.COLUMN_URI))
            return Album(
                cursor.getString(cursor.getColumnIndex("bucket_id")),
                Uri.parse(clumn ?: ""),
                cursor.getString(cursor.getColumnIndex("bucket_display_name")),
                cursor.getLong(cursor.getColumnIndex(AlbumLoader.COLUMN_COUNT)),
            )
        }
    }
}
