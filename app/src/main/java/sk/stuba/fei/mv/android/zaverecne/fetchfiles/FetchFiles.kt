package sk.stuba.fei.mv.android.zaverecne.fetchfiles

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import sk.stuba.fei.mv.android.zaverecne.beans.Album
import sk.stuba.fei.mv.android.zaverecne.beans.RowItem
import java.io.File
import java.util.*

object FetchFiles {

    fun getFiles(root: File, fileType: String): List<Album> {
        val albums = ArrayList<Album>()
        var songs = ArrayList<File>()
        songs = loadFiles(root, fileType)
        val rowItems: MutableList<RowItem> = ArrayList()
        if (!songs.isEmpty()) {
            val songNames = arrayOfNulls<String>(songs.size)
            val descriptions = arrayOfNulls<String>(songs.size)
            val dateCreated = arrayOfNulls<Date>(songs.size)
            for (i in songs.indices) {
                var addedToAlbum = false
                //files.add(songs.get(i));
                songNames[i] = songs[i].name.replace(fileType, "")
                descriptions[i] = ""
                dateCreated[i] = getDateCreated(i, songs)
                val item = songNames[i]?.let { descriptions[i]?.let { it1 ->
                    dateCreated[i]?.let { it2 ->
                        RowItem(it,
                            it1, songs[i], it2
                        )
                    }
                } }
                val unit = "MB"

                if (item != null) {
                    item.size = getFileSize(songs[i], unit)
                    item.parent = songs[i].parentFile
                    rowItems.add(item)
                }

                if (i == 0) {
                    val album = Album()
                    if (item != null) {
                        album.file = item.parent
                        album.rowItems.add(item)
                        album.name = item.parent.name
                    }

                    albums.add(album)
                } else {
                    for (album in albums) {
                        if (item != null) {
                            if (album.file == item.parent) {
                                album.rowItems.add(item)
                                addedToAlbum = true
                                break
                            }
                        }
                    }
                    if (!addedToAlbum) {
                        val album = Album()
                        if (item != null) {
                            album.file = item.parent
                            album.rowItems.add(item)
                            album.name = item.parent.name
                        }

                        albums.add(album)
                    }
                }
            }
            for (album in albums) {
                Collections.sort(album.rowItems) { row1, row2 -> row2.dateCreated.compareTo(row1.dateCreated) }
            }
        }
        return albums
    }

    private fun loadFiles(root: File, fileType: String): ArrayList<File> {
        val arrayList = ArrayList<File>()
        val files = root.listFiles()!!
        if (files.size != 0) {
            for (file in files) {
                if (file.isDirectory) {
                    arrayList.addAll(loadFiles(file, fileType))
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

    fun getRealPathFromURI(activity: Activity?, contentUri: Uri?): String? {
        var cursor: Cursor = activity!!.contentResolver.query(contentUri!!, null, null, null, null)!!
        cursor.moveToFirst()
        var document_id = cursor.getString(0)
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1)
        cursor.close()
        cursor = activity!!.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID + " = ? ", arrayOf(document_id), null)!!
        cursor.moveToFirst()
        val path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
        cursor.close()
        return path
    }

    // UPDATED!
    fun getPath(activity: Activity?, uri: Uri?): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor: Cursor = activity!!.contentResolver.query(uri!!, projection, null, null, null)!!
        return if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            val column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(column_index)
        } else null
    }


}