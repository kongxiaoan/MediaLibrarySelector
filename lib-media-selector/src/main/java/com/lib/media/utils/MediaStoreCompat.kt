package com.lib.media.utils

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import androidx.core.os.EnvironmentCompat
import androidx.fragment.app.FragmentActivity
import com.lib.media.entities.CaptureStrategy
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MediaStoreCompat(activity: FragmentActivity) {
    private val mContext: WeakReference<FragmentActivity>
    private var mCaptureStrategy: CaptureStrategy? = null
    var currentPhotoUri: Uri? = null
        private set
    var currentPhotoPath: String? = null
        private set

    init {
        mContext = WeakReference(activity)
    }

    fun setCaptureStrategy(strategy: CaptureStrategy?) {
        mCaptureStrategy = strategy
    }

    fun dispatchCaptureIntent(myLauncher: ActivityResultLauncher<Uri>) {
        var photoFile: File? = null
        try {
            photoFile = createImageFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (photoFile != null) {
            currentPhotoPath = photoFile.absolutePath
            currentPhotoUri = FileProvider.getUriForFile(
                mContext.get()!!,
                mCaptureStrategy!!.authority,
                photoFile,
            )
            myLauncher.launch(currentPhotoUri)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = String.format("JPEG_%s.jpg", timeStamp)
        var storageDir: File?
        if (mCaptureStrategy!!.isPublic) {
            storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES,
            )
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }
        } else {
            storageDir = mContext.get()!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        }
        if (mCaptureStrategy!!.directory != null) {
            storageDir = File(storageDir, mCaptureStrategy!!.directory)
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }
        }

        // Avoid joining path components manually
        val tempFile = File(storageDir, imageFileName)

        // Handle the situation that user's external storage is not ready
        return if (Environment.MEDIA_MOUNTED != EnvironmentCompat.getStorageState(tempFile)) {
            null
        } else {
            tempFile
        }
    }

    companion object {
        /**
         * Checks whether the device has a camera feature or not.
         *
         * @param context a context to check for camera feature.
         * @return true if the device has a camera feature. false otherwise.
         */
        fun hasCameraFeature(context: Context): Boolean {
            val pm = context.applicationContext.packageManager
            return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)
        }
    }
}
