package com.zii.sdk.helper.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.ToastUtils
import com.zii.sdk.helper.databinding.ViewInputEditorBinding

class InputEditor(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

    private var binding: ViewInputEditorBinding =
        ViewInputEditorBinding.inflate(LayoutInflater.from(context), this)

    init {
        orientation = LinearLayout.VERTICAL
        binding.btnClear.setOnClickListener {
            binding.edtInput.text.clear()
        }
        binding.btnCopy.setOnClickListener {
            ClipboardUtils.copyText(binding.edtInput.text.toString())
        }
        binding.btnPast.setOnClickListener {
            val text = ClipboardUtils.getText()
            if (text.isNullOrEmpty()) {
                ToastUtils.showLong("无复制内容~")
            } else binding.edtInput.setText(text)
        }
    }

    fun getEdtInput(): EditText {
        return binding.edtInput
    }

    fun getBtnDo(): Button {
        return binding.btnDo
    }
}