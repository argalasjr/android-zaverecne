package sk.stuba.fei.mv.android.zaverecne.fetchfiles

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import java.io.File
import java.util.*

object FetchFiles {

    private fun fetchFilesByRoot(root: File, fileType: String): ArrayList<File> {
        val arrayList = ArrayList<File>()
        val files = root.listFiles()!!
        if (files.size != 0) {
            for (file in files) {
                if (file.isDirectory) {
                    arrayList.addAll(fetchFilesByRoot(file, fileType))
                } else {
                    if (file.name.endsWith(fileType)) {
                        arrayList.add(file)
                    }
                }
            }
        }
        return arrayList
    }

    @SuppressLint("DefaultLocale")
    private fun getTime(miliseconds: Int): String {
        val ms = miliseconds % 1000
        val rem = miliseconds / 1000
        val hr = rem / 3600
        val remHr = rem % 3600
        val mn = remHr / 60
        val sec = remHr % 60
        return if (hr == 0) {
            String.format("%02d", mn) + ':' + String.format("%02d", sec)
        } else String.format("%02d", hr) + ':' + String.format(
                "%02d",
                mn
        ) + ':' + String.format("%02d", sec)
    }

    @SuppressLint("DefaultLocale")
    public fun getFileSize(file: File, unit: String): Double {

        // Get length of file in bytes
        val fileSizeInBytes = file.length()
        // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
        val fileSizeInKB = fileSizeInBytes / 1024
        //  Convert the KB to MegaBytes (1 MB = 1024 KBytes)
        val fileSizeInMB = fileSizeInKB.toDouble() / 1024.toDouble()
        return when (unit) {
            "MB" -> {
                fileSizeInMB
            }
            "KB" -> {
                Double.fromBits(fileSizeInKB)
            }
            else -> {
                fileSizeInMB
            }
        }
    }

    private fun getDateCreated(position: Int, files: List<File>): Date {
        return Date(files[position].lastModified())
    }


    fun getRealPathFromURI(context: Context, contentURI: Uri): String? {
        val result: String?
        val cursor: Cursor? = context.contentResolver.query(
            contentURI, null, null, null, null
        )
        if (cursor == null) {
            result = contentURI.path
        } else {
            cursor.moveToFirst()
            val idx: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }
        return result
    }

}