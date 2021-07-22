package com.bharathvishal.textfilegeneratorbenchmark


import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Looper.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class MainActivity : AppCompatActivity() {

    private var activityContextMain: Context? = null
    private var fragmentLayout: LinearLayout? = null

    //Fragment back button handling
    private var doubleBackToExitPressedOnce: Boolean = false
    private val mRunnableBackButton = Runnable { doubleBackToExitPressedOnce = false }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbarmain)
        setSupportActionBar(toolbar)

        fragmentLayout = findViewById(R.id.linearlayoutfragmentlayout)

        doubleBackToExitPressedOnce = false

        activityContextMain = this@MainActivity

        try {
            val mgr = supportFragmentManager
            val trans = mgr.beginTransaction()
            trans.replace(
                R.id.mainActivityFragmentFramelayout,
                TextFileGeneratorFragment(),
                "Frag_Replace_Fragment"
            )
            trans.disallowAddToBackStack()
            trans.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onBackPressed() {
        //part to handle back press
        //executed when stack_top=-1 and activity recreated value is No
        Handler(Looper.getMainLooper()).postDelayed({
            mRunnableBackButton
        }, 1500)
        // 1.50 seconds in ms


        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        } else {
            showCustomToast("Please press BACK again to exit", activityContextMain!!)
        }
        doubleBackToExitPressedOnce = true
    }


    @Suppress("SameParameterValue")
    private fun showCustomToast(content: String, con: Context?) {
        try {
            val toast: Toast = Toast.makeText(con, content, Toast.LENGTH_SHORT)
            if (!isFinishing)
                toast.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
