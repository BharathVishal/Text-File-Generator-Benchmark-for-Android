
package com.bharathvishal.textfilegeneratorbenchmark

import android.content.Context
import android.os.Environment
import android.widget.Toast
import java.io.File

object Utilities {
    //returns the path to the internal storage
    fun getInternalStorage(): File {
        return Environment.getExternalStorageDirectory()
    }


    fun deleteFiles(curFile: File) {
        try {
            if (curFile.isDirectory)
                for (child in curFile.listFiles())
                    deleteFiles(child)

            curFile.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun showCustomToast(content: String, con: Context?) {
        try {
            val toast: Toast = Toast.makeText(con, content, Toast.LENGTH_SHORT)
            toast.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}