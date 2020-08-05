@file:Suppress("DEPRECATION")

package com.bharathvishal.textfilegeneratorbenchmark

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.*
import java.io.File
import java.io.RandomAccessFile
import java.lang.ref.WeakReference
import java.util.*


@Suppress("USELESS_CAST", "UNUSED_VARIABLE")
class TextFileGeneratorFragment : Fragment(), CoroutineScope by MainScope() {
    private var contextCur: Context? = null
    private var benchMarkProgress: ProgressDialog? = null
    private var isGenerateFilesRunning: Boolean = false
    private var runnerGenerateFiles1: AsyncTaskRunnerGenerate? = null
    private var runnerGenerateFiles2: AsyncTaskRunnerGenerate? = null
    private var runnerGenerateFiles3: AsyncTaskRunnerGenerate? = null
    private var runnerGenerateFiles4: AsyncTaskRunnerGenerate? = null

    private var curGeneratedFilesCount: Int = 0
    private var filesAlreadyPresent: Int = 0
    var totalCount: Int = 10000
    val tagTASK = "Async1"
    var useSingleTaskToGenerate: Boolean = true
    var totalTimeForGeneratingFiles: Long = 0

    @Volatile
    var curAsyncTaskExecuted = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAdded) {
            contextCur = activity as Context?

            benchMarkProgress = ProgressDialog(contextCur)
            benchMarkProgress?.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            benchMarkProgress?.setCancelable(true)
            benchMarkProgress?.setTitle("Generating Text Files")

            generate_files_button?.setOnClickListener {
                if (isStoragePermissionGranted(contextCur!!)) {
                    curGeneratedFilesCount = 0

                    totalCount = try {
                        Integer.parseInt(noOfFilesToGenerateEditText.text.toString())
                    } catch (e: java.lang.Exception) {
                        0
                    }

                    Log.d(tagTASK, "Current value : $totalCount")

                    useSingleTaskToGenerate = threadTaskTypeSwitch.isChecked

                    if (totalCount in 1..50000) {
                        //Proceed and launch async task
                        if (useSingleTaskToGenerate) {
                            runnerGenerateFiles1 =
                                contextCur?.let { AsyncTaskRunnerGenerate(it, 1, totalCount) }
                            runnerGenerateFiles1?.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)
                        } else {
                            /*
                            Algorithm
                            Say total count of files is 15
                            num1=3 (totalfilecount/4)
                            num2=6 (num1 * 2)
                            num3=9 (num1 * 3)

                            Tasks start count:
                            Async Task 1 : 1 to 3   (1 to num1)
                            Async Task 2 : 4 to 6   (num1+1 to num2)
                            Async Task 3 : 7 to 9  (num2+1 to num3)
                            Async Task 4 : 10 to 15 (num3+1 to totalFileCount)*/

                            val startNum1 = totalCount / 4
                            val startNum2 = startNum1 * 2
                            val startNum3 = startNum1 * 3

                            Log.d(tagTASK, "Ranges : $startNum1 $startNum2 $startNum3");

                            curAsyncTaskExecuted = 0

                            //Task 1
                            runnerGenerateFiles1 =
                                contextCur?.let { AsyncTaskRunnerGenerate(it, 1, startNum1) }
                            runnerGenerateFiles1?.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

                            //Task 2
                            runnerGenerateFiles2 =
                                contextCur?.let {
                                    AsyncTaskRunnerGenerate(
                                        it,
                                        startNum1 + 1,
                                        startNum2
                                    )
                                }
                            runnerGenerateFiles2?.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)


                            //Task 3
                            runnerGenerateFiles3 =
                                contextCur?.let {
                                    AsyncTaskRunnerGenerate(
                                        it,
                                        startNum2 + 1,
                                        startNum3
                                    )
                                }
                            runnerGenerateFiles3?.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)


                            //Task 4
                            runnerGenerateFiles4 =
                                contextCur?.let {
                                    AsyncTaskRunnerGenerate(
                                        it,
                                        startNum3 + 1,
                                        totalCount
                                    )
                                }
                            runnerGenerateFiles4?.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                        }
                    } else
                        Utilities.showCustomToast("Enter a value between 0 and 50001", contextCur)
                } else
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ), 101
                    )
            }


            cleanup_files?.setOnClickListener {
                if (isStoragePermissionGranted(contextCur!!)) {
                    //Proceed and launch coroutine
                    contextCur?.let { taskRunnerCleanup(it) }
                } else
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ), 101
                    )
            }
        }
    }


    @Suppress("SameParameterValue")
    private fun getRandomString(sizeOfRandomString: Int): String {
        val random = Random()
        val sb = StringBuilder(sizeOfRandomString)
        for (i in 0 until sizeOfRandomString)
            sb.append(Constants.allowedCharacters[random.nextInt(Constants.allowedCharacters.length)])
        return sb.toString()
    }


    //Async task to generate files
    private inner class AsyncTaskRunnerGenerate(
        con: Context,
        startNum1: Int,
        endNum1: Int
    ) :
        AsyncTask<Void, Int, Void>() {
        private val contextRef: WeakReference<Context> = WeakReference(con)
        private var startNum = startNum1
        val t1: Long = System.currentTimeMillis()
        var t2: Long = 0
        var endNum: Int = endNum1
        var loopexecutionTimes = 0

        @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
        override fun doInBackground(vararg voids: Void): Void? {
            val context1 = contextRef.get()
            val count = totalCount
            val i1 = 0
            var c = 0
            var resPub = 0
            var tempPercent: Float
            var progressupdateString: String

            Log.d(tagTASK, "Started task with startnum :$startNum")
            try {
                var internalFile =
                    File(Utilities.getInternalStorage().path + "/FileBenchmark/")

                if (!internalFile.exists()) {
                    //create the directory first
                    val dirToCreate =
                        File(Utilities.getInternalStorage().path + "/FileBenchmark/")
                    if (!dirToCreate.exists())
                        dirToCreate.mkdirs()
                    internalFile = dirToCreate
                }

                try {
                    filesAlreadyPresent = internalFile.list()?.size!!
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                Log.d(tagTASK, "num of files already present: $filesAlreadyPresent")

                var tempFile: File
                var tempStr: String
                val min = 0
                var f: RandomAccessFile
                var random: Int
                val max = 8192

                //startNum = filesAlreadyPresent
                Log.d(tagTASK, "cur task range : $startNum $endNum")

                if (internalFile.exists()) {
                    for (t1 in startNum..endNum) {
                        //Percent calculation
                        ++c
                        tempPercent = c / count.toFloat()
                        resPub = (tempPercent * 100).toInt()

                        ++curGeneratedFilesCount

                        ++filesAlreadyPresent

                        try {
                            filesAlreadyPresent = internalFile.list()?.size!!
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }


                        if (filesAlreadyPresent >= totalCount) {
                            cancelTask()
                            break
                        }

                        //progressupdateString = "$resPub% , $c/$count"
                        //progressupdateString = "$curGeneratedFilesCount"

                        publishProgress(filesAlreadyPresent)

                        if (isCancelled)
                            break

                        //Generate files here
                        try {
                            tempStr = getRandomString(5) + ".txt"
                            tempFile = File(internalFile, tempStr)
                            f = RandomAccessFile(tempFile, "rw")
                            random = Random().nextInt(max - min + 1) + min
                            f.setLength(random.toLong())
                            f.close()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            t2 = System.currentTimeMillis() - t1
            totalTimeForGeneratingFiles += t2
            loopexecutionTimes = c
            return null
        }


        override fun onCancelled(result: Void?) {
            super.onCancelled(result)
            val context1 = contextRef.get()

            try {
                if (benchMarkProgress != null) {
                    if (benchMarkProgress?.isShowing!!)
                        benchMarkProgress?.dismiss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            isGenerateFilesRunning = false

            val tempinternalFile =
                File(Utilities.getInternalStorage().path + "/FileBenchmark/")
            val tfilesAlreadyPresent = tempinternalFile.list()?.size!!

            ++curAsyncTaskExecuted

            Log.d(tagTASK, "cur async task being executed OnCancelled : $curAsyncTaskExecuted")


            if (tfilesAlreadyPresent >= totalCount) {
                try {
                    Utilities.showCustomToast("Generated files successfully!", context1)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                try {
                    val alertDialog: AlertDialog = AlertDialog.Builder(context1).create()
                    alertDialog.setTitle("Benchmark Results")

                    if (useSingleTaskToGenerate) {
                        alertDialog.setMessage("Total time to generate $totalCount files : $totalTimeForGeneratingFiles ms\nUsed single task to generate files ")
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })

                        if (!alertDialog.isShowing)
                            alertDialog.show()
                    } else {
                        val timeToGenerate = totalTimeForGeneratingFiles / 4
                        alertDialog.setMessage("Total time to generate $totalCount files : $timeToGenerate ms\nUsed four parallel tasks to generate files ")

                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })

                        if (!alertDialog.isShowing && curAsyncTaskExecuted == 4)
                            alertDialog.show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        @SuppressLint("SetTextI18n")
        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            val context1 = contextRef.get()

            try {
                if (benchMarkProgress != null) {
                    if (benchMarkProgress?.isShowing!!)
                        benchMarkProgress?.dismiss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            ++curAsyncTaskExecuted

            Log.d(tagTASK, "cur async task being executed OnPostExecute : $curAsyncTaskExecuted")

            val tempinternalFile =
                File(Utilities.getInternalStorage().path + "/FileBenchmark/")
            val tfilesAlreadyPresent = tempinternalFile.list()?.size!!


            if (tfilesAlreadyPresent >= totalCount) {
                try {
                    Utilities.showCustomToast("Generated files successfully!", context1)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                try {
                    val alertDialog: AlertDialog = AlertDialog.Builder(context1).create()
                    alertDialog.setTitle("Benchmark Results")

                    if (useSingleTaskToGenerate) {
                        alertDialog.setMessage("Total time to generate $totalCount files : $totalTimeForGeneratingFiles ms\nUsed single task to generate files ")

                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })

                        if (!alertDialog.isShowing)
                            alertDialog.show()
                    } else {
                        val timeToGenerate = totalTimeForGeneratingFiles / 4
                        alertDialog.setMessage("Total time to generate $totalCount files : $timeToGenerate ms\nUsed four parallel tasks to generate files ")

                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })

                        if (!alertDialog.isShowing && curAsyncTaskExecuted == 4)
                            alertDialog.show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        fun cancelTask() {
            cancel(true)
        }

        override fun onProgressUpdate(vararg progress: Int?) {
            super.onProgressUpdate(*progress)
            benchMarkProgress?.setMessage("Generating files, Please wait \nProgress : $filesAlreadyPresent/$totalCount")
        }

        override fun onPreExecute() {
            val context1 = contextRef.get()

            benchMarkProgress?.setMessage("Generating Text Files")
            benchMarkProgress?.isIndeterminate = false
            benchMarkProgress?.setCancelable(true)
            benchMarkProgress?.setCanceledOnTouchOutside(false)
            benchMarkProgress?.setProgressStyle(ProgressDialog.STYLE_SPINNER)

            isGenerateFilesRunning = true

            benchMarkProgress?.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel") { dialog, _ ->
                if (isGenerateFilesRunning) {
                    cancel(true)
                    dialog.dismiss()
                    Utilities.showCustomToast("You have stopped the file generation", context1)
                }
            }

            benchMarkProgress?.setOnCancelListener { dialog ->
                Log.d("bench1", "oncancel invoked")
                if (isGenerateFilesRunning) {
                    cancel(true)
                    dialog.dismiss()
                    Utilities.showCustomToast("You have aborted the benchmark", context1)
                }
            }

            try {
                if (!activity!!.isFinishing)
                    benchMarkProgress?.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    //Coroutine to cleanup files
    private fun taskRunnerCleanup(con: Context) {
        //Coroutine
        val contextRef: WeakReference<Context> = WeakReference(con)

        launch(Dispatchers.Default) {
            val context1 = contextRef.get()

            val internalFile =
                File(Utilities.getInternalStorage().path + "/FileBenchmark/")

            //Delete the folder if it exists previously
            if (internalFile.exists() && internalFile.path.toString() == "/storage/emulated/0/FileBenchmark")
                Utilities.deleteFiles(internalFile)

            //UI Thread
            withContext(Dispatchers.Main) {
                try {
                    Utilities.showCustomToast("Deleted all generated files", contextCur)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun isStoragePermissionGranted(con: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(
                    con,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PermissionChecker.PERMISSION_GRANTED
            ) {
                Log.v("Permission", "Permission is granted")
                true
            } else {
                Log.v("Permission", "Permission is revoked")
                false
            }
        } else {
            Log.v("Permission", "Permission is granted")
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Utilities.showCustomToast("Storage permission granted", contextCur)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        try {
            runnerGenerateFiles1?.cancelTask()
            runnerGenerateFiles2?.cancelTask()
            runnerGenerateFiles3?.cancelTask()
            runnerGenerateFiles4?.cancelTask()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}