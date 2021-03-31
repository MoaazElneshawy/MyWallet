package com.moaazelneshawy.mywallet.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.View.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.jetbrains.anko.sdk27.coroutines.onEditorAction


/**
Created by Moaaz Elneshawy
on 30,March,2021
 **/

fun refactorDatePickerFormat(year: Int, monthOfYear: Int, dayOfMonth: Int): String {
    var month: String = ""
    var day: String = ""
    if (dayOfMonth.toString().length == 1) {
        day = "0$dayOfMonth"
    } else {
        day = dayOfMonth.toString()
    }
    if ((monthOfYear + 1).toString().length == 1) {
        month = "0" + (monthOfYear + 1).toString()
    } else {
        month = (monthOfYear + 1).toString()
    }
    return "$year-${month}-$day"
}

/**
 * Set view visibility visible
 */
fun View.visible() {
    visibility = VISIBLE
}

/**
 * Set view visibility invisible
 */
fun View.invisible() {
    visibility = INVISIBLE
}

/**
 * Set view visibility gone
 */
fun View.gone() {
    visibility = GONE
}


/**
 * Set LinearLayoutManager to RecyclerView
 * @param orientation (VERTICAL or HORIZONTAL)
 */
fun RecyclerView.linearLayoutManager(
    @RecyclerView.Orientation orientation: Int = VERTICAL,
    reversLayout: Boolean = false
) {
    layoutManager = LinearLayoutManager(this.context, orientation, reversLayout)
}

fun TextInputEditText.getTextValue(): String {
    return this.text.toString().trim()
}

fun TextInputEditText.clearText() {
    this.setText("")
}

fun TextInputLayout.clearError() {
    this.error = null
}

fun TextInputLayout.setErrorMessage(errorMessage: String) {
    this.error = errorMessage
}

fun View.snackbar(message: String) {
    Snackbar.make(this, message, BaseTransientBottomBar.LENGTH_SHORT).show()
}

fun TextInputEditText.setActionOnSearch(action: () -> Unit) {
    this.onEditorAction { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            action.invoke()
        }
    }
}

fun hideKeyboardFrom(context: Context, view: View) {
    val imm: InputMethodManager =
        context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

