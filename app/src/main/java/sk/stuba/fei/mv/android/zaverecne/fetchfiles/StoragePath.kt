package sk.stuba.fei.mv.android.zaverecne.fetchfiles

import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.os.EnvironmentCompat
import java.io.File
import java.util.*

class StoragePath {
    lateinit var getExternalFilesDirs: Array<File>

    /**
     * Constructor for KitKat & above
     * @param getExternalFilesDirs
     */
    constructor(getExternalFilesDirs: Array<File>) {
        this.getExternalFilesDirs = getExternalFilesDirs
    }

    /**
     * Constructor for lower than Kitkat
     *
     */
    constructor() {}//Method 2 for all versions

    // parse output

//        //Below few lines is to remove paths which may not be external memory card, like OTG (feel free to comment them out)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            for (int i = 0; i < results.size(); i++) {
//                if (!results.get(i).toLowerCase().matches(".*[0-9a-f]{4}[-][0-9a-f]{4}")) {
//                    Log.d("Tag", results.get(i) + " might not be extSDcard");
//                    results.remove(i--);
//                }
//            }
//        } else {
//            for (int i = 0; i < results.size(); i++) {
//                if (!results.get(i).toLowerCase().contains("ext") && !results.get(i).toLowerCase().contains("sdcard")) {
//                    Log.d("Tag", results.get(i) + " might not be extSDcard");
//                    results.remove(i--);
//                }
//            }
//        }

    //Get path to the Internal Storage aka ExternalStorageDirectory
    //Method 1 for KitKat & above //get external storages
    val deviceStorages: Array<String
    //Log.d("sdcard", externalDirs[1].getAbsolutePath());
    ?>
        get() {
            val results: MutableList<String> = ArrayList()

            //Method 1 for KitKat & above //get external storages
            val externalDirs = getExternalFilesDirs
            Log.d("sdcard", externalDirs[0].absolutePath)
            //Log.d("sdcard", externalDirs[1].getAbsolutePath());
            for (file in externalDirs) {
                val path = file.path.split("/Android").toTypedArray()[0]
                var addPath = false
                addPath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Environment.isExternalStorageRemovable(file)
                } else {
                    Environment.MEDIA_MOUNTED == EnvironmentCompat.getStorageState(file)
                }
                if (addPath) {
                    results.add(path)
                }
            }
            if (results.isEmpty()) { //Method 2 for all versions
                val out: MutableList<String> = ArrayList()
                val reg = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*".toRegex()
                var s = ""
                try {
                    val process = ProcessBuilder().command("mount")
                        .redirectErrorStream(true).start()
                    process.waitFor()
                    val `is` = process.inputStream
                    val buffer = ByteArray(1024)
                    while (`is`.read(buffer) != -1) {
                        s += String(buffer)
                    }
                    `is`.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // parse output
                val lines = s.split("\n").toTypedArray()
                for (line in lines) {
                    if (!line.toLowerCase(Locale.US).contains("asec")) {
                        if (line.matches(reg)) {
                            val parts = line.split(" ").toTypedArray()
                            for (part in parts) {
                                if (part.startsWith("/")) if (!part.toLowerCase(Locale.US)
                                        .contains("vold")
                                ) out.add(part)
                            }
                        }
                    }
                }
                results.addAll(out)
            }

//        //Below few lines is to remove paths which may not be external memory card, like OTG (feel free to comment them out)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            for (int i = 0; i < results.size(); i++) {
//                if (!results.get(i).toLowerCase().matches(".*[0-9a-f]{4}[-][0-9a-f]{4}")) {
//                    Log.d("Tag", results.get(i) + " might not be extSDcard");
//                    results.remove(i--);
//                }
//            }
//        } else {
//            for (int i = 0; i < results.size(); i++) {
//                if (!results.get(i).toLowerCase().contains("ext") && !results.get(i).toLowerCase().contains("sdcard")) {
//                    Log.d("Tag", results.get(i) + " might not be extSDcard");
//                    results.remove(i--);
//                }
//            }
//        }

            //Get path to the Internal Storage aka ExternalStorageDirectory
            val internalStoragePath = Environment.getExternalStorageDirectory().absolutePath
            results.add(0, internalStoragePath)
            val storageDirectories = arrayOfNulls<String>(results.size)
            for (i in results.indices) storageDirectories[i] = results[i]
            return storageDirectories
        }
}