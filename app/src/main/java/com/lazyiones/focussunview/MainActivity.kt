package com.lazyiones.focussunview

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams

class MainActivity : AppCompatActivity() {
    @SuppressLint("ClickableViewAccessibility") override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val focusSunView = findViewById<FocusSunView>(R.id.focus_sun_view)

        findViewById<View>(R.id.click_view).setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    focusSunView.visibility = View.VISIBLE
                    focusSunView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        topMargin = (motionEvent.y - (focusSunView.height / 2f)).toInt()
                        marginStart = (motionEvent.x - (focusSunView.width / 2f)).toInt()
                    }
                    focusSunView.startCountdown()
                }
            }
            return@setOnTouchListener true
        }

        focusSunView.setOnExposureChangeListener(object : FocusSunView.OnExposureChangeListener {
            override fun onExposureChangeListener(exposure: Float) {
                Log.e("FocusSunView", "onExposureChangeListener: -----------> $exposure")
            }
        })
    }
}