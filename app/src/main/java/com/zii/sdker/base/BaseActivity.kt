package com.zii.sdker.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.KeyboardUtils
import com.zii.sdker.MainActivity
import com.zii.sdker.R

open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (this !is MainActivity) supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        layoutCopyright(view)
    }

    private fun layoutCopyright(view: View?) {
        if (findViewById<View>(R.id.layout_zii_copyright) == null) {
            view?.post {
                val root = window.decorView as FrameLayout
                LayoutInflater.from(this).inflate(R.layout.layout_sdker_copyright, root, true)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        KeyboardUtils.clickBlankArea2HideSoftInput()
        return super.onOptionsItemSelected(item)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            //点击空白隐藏输入框
            if (isShouldHideKeyboard(currentFocus, ev)) {
                KeyboardUtils.hideSoftInput(this)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun isShouldHideKeyboard(view: View?, event: MotionEvent): Boolean {
        if (view is EditText) {
            val location = intArrayOf(0, 0)
            view.getLocationOnScreen(location)
            val left = location[0]
            val top = location[1]
            val bottom = top + view.height
            val right = left + view.width
            return !(event.rawX > left && event.rawX < right && event.rawY > top && event.rawY < bottom);
        }
        return false
    }
}