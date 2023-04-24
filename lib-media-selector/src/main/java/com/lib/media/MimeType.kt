package com.lib.media

import android.content.ContentResolver
import android.net.Uri
import android.text.TextUtils
import android.webkit.MimeTypeMap
import com.lib.media.utils.FileUtils
import java.util.EnumSet

/**
 * MIME Type enumeration to restrict selectable media on the selection activity. Matisse only supports images and
 * videos.
 *
 *
 * Good example of mime types Android supports:
 * https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/media/java/android/media/MediaFile.java
 */
enum class MimeType(private val mMimeTypeName: String, private val mExtensions: Set<String>) {
    // ============== images ==============
    JPEG(
        "image/jpeg",
        setOf(
            "jpg",
            "jpeg",
        ),
    ),
    PNG(
        "image/png",
        setOf(
            "png",
        ),
    ),
    GIF(
        "image/gif",
        setOf(
            "gif",
        ),
    ),
    BMP(
        "image/x-ms-bmp",
        setOf(
            "bmp",
        ),
    ),
    WEBP(
        "image/webp",
        setOf(
            "webp",
        ),
    ), // ============== videos ==============
    MPEG(
        "video/mpeg",
        setOf(
            "mpeg",
            "mpg",
        ),
    ),
    MP4(
        "video/mp4",
        setOf(
            "mp4",
            "m4v",
        ),
    ),
    QUICKTIME(
        "video/quicktime",
        setOf(
            "mov",
        ),
    ),
    THREEGPP(
        "video/3gpp",
        setOf(
            "3gp",
            "3gpp",
        ),
    ),
    THREEGPP2(
        "video/3gpp2",
        setOf(
            "3g2",
            "3gpp2",
        ),
    ),
    MKV(
        "video/x-matroska",
        setOf(
            "mkv",
        ),
    ),
    WEBM(
        "video/webm",
        setOf(
            "webm",
        ),
    ),
    TS(
        "video/mp2ts",
        setOf(
            "ts",
        ),
    ),
    AVI(
        "video/avi",
        setOf(
            "avi",
        ),
    ),
    ;

    override fun toString(): String {
        return mMimeTypeName
    }

    fun checkType(resolver: ContentResolver, uri: Uri?): Boolean {
        val map = MimeTypeMap.getSingleton()
        if (uri == null) {
            return false
        }
        val type = map.getExtensionFromMimeType(resolver.getType(uri))
        var path: String? = null
        // lazy load the path and prevent resolve for multiple times
        var pathParsed = false
        for (extension in mExtensions) {
            if (extension == type) {
                return true
            }
            if (!pathParsed) {
                // we only resolve the path for one time
                path = FileUtils.getPath(resolver, uri)
                if (!TextUtils.isEmpty(path)) {
                    path = path!!.lowercase()
                }
                pathParsed = true
            }
            if (path != null && path.endsWith(extension)) {
                return true
            }
        }
        return false
    }

    companion object {
        fun ofAll(): Set<MimeType> {
            return EnumSet.allOf(MimeType::class.java)
        }

        fun of(type: MimeType, vararg rest: MimeType?): Set<MimeType> {
            return EnumSet.of(type, *rest)
        }

        fun ofImage(): Set<MimeType> {
            return EnumSet.of(JPEG, PNG, GIF, BMP, WEBP)
        }

        fun ofImage(onlyGif: Boolean): Set<MimeType> {
            return EnumSet.of(GIF)
        }

        fun ofGif(): Set<MimeType> {
            return ofImage(true)
        }

        fun ofVideo(): Set<MimeType> {
            return EnumSet.of(MPEG, MP4, QUICKTIME, THREEGPP, THREEGPP2, MKV, WEBM, TS, AVI)
        }

        fun isImage(mimeType: String?): Boolean {
            return mimeType?.startsWith("image") ?: false
        }

        fun isVideo(mimeType: String?): Boolean {
            return mimeType?.startsWith("video") ?: false
        }

        fun isGif(mimeType: String?): Boolean {
            return if (mimeType == null) false else mimeType == GIF.toString()
        }
    }
}
