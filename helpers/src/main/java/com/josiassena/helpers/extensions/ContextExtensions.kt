package com.josiassena.helpers.extensions

import android.content.Context
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.view.View

/**
 * @author Josias Sena
 */
fun Context.showLongSnackBar(view: View, @StringRes message: Int) {
    Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
}

fun Context.showLongSnackBar(view: View, message: String) {
    Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
}