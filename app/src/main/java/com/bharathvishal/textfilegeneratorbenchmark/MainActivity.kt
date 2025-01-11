/**
 *
 * Copyright 2018-2025 Bharath Vishal G.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **/

package com.bharathvishal.textfilegeneratorbenchmark


import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowInsets
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.color.DynamicColors

class MainActivity : AppCompatActivity() {

    private var activityContextMain: Context? = null
    private var fragmentLayout: LinearLayout? = null

    //Fragment back button handling
    private var doubleBackToExitPressedOnce: Boolean = false
    private val mRunnableBackButton = Runnable { doubleBackToExitPressedOnce = false }


    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                enableEdgeToEdge()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbarmain)
        setSupportActionBar(toolbar)

        fragmentLayout = findViewById(R.id.linearlayoutfragmentlayout)

        doubleBackToExitPressedOnce = false

        activityContextMain = this@MainActivity

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                val viewTempAppBar = findViewById<View>(R.id.appbarmain)
                viewTempAppBar.setOnApplyWindowInsetsListener { view, insets ->
                    val statusBarInsets = insets.getInsets(WindowInsets.Type.statusBars())

                    val nightModeFlags: Int =  view.resources
                        .configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                    val isDarkMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES
                    val isDynamicTheme = DynamicColors.isDynamicColorAvailable()
                    // Adjust padding to avoid overlap
                    view.setPadding(0, statusBarInsets.top, 0, 0)
                    insets
                }


                val tempL: View = findViewById<View>(R.id.linearlayoutfragmentlayout)
                ViewCompat.setOnApplyWindowInsetsListener(tempL) { view, windowInsets ->
                    val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemGestures())
                    // Apply the insets as padding to the view. Here, set all the dimensions
                    // as appropriate to your layout. You can also update the view's margin if
                    // more appropriate.
                    tempL.updatePadding(0, 0, 0, insets.bottom)

                    // Return CONSUMED if you don't want the window insets to keep passing down
                    // to descendant views.
                    WindowInsetsCompat.CONSUMED
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

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


        onBackPressedDispatcher.addCallback(
            this, // lifecycle owner
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    //part to handle back press
                    //executed when stack_top=-1 and activity recreated value is No
                    Handler(Looper.getMainLooper()).postDelayed({
                        mRunnableBackButton
                    }, 1500)
                    // 1.50 seconds in ms


                    if (doubleBackToExitPressedOnce) {
                        finish()
                    } else {
                        showCustomToast("Please press BACK again to exit", activityContextMain!!)
                    }
                    doubleBackToExitPressedOnce = true
                }
            })
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
