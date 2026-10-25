@file:JvmName("SettingsEntry")

package dev.secp192k1.twitch.settingsentry

import android.content.Context
import android.content.res.TypedArray
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import app.reseam.runtime.Export
import kotlin.math.roundToInt

private const val CHEVRON = "›"

// One theme lookup serves the whole row, and styled attribute lookups need the ids in ascending order
private val themeAttributes = intArrayOf(
    android.R.attr.textColorPrimary,
    android.R.attr.textColorSecondary,
    android.R.attr.listDivider,
    android.R.attr.selectableItemBackground,
)
private const val PRIMARY_TEXT = 0
private const val SECONDARY_TEXT = 1
private const val DIVIDER = 2
private const val RIPPLE = 3

// The patch replaces this body with a call that opens the bundle's settings
@Export
fun openSettings() {}

// Twitch draws its settings list in Compose, which a patch cannot add a row to, so the row goes above the whole list
@Suppress("unused")
@Export
fun addSettingsRow(settings: View?, title: String): View? {
    if (settings == null) return null

    val context = settings.context
    val density = context.resources.displayMetrics.density
    val styledAttributes = context.obtainStyledAttributes(themeAttributes)
    try {
        val screen = LinearLayout(context)
        screen.orientation = LinearLayout.VERTICAL
        screen.addView(row(context, styledAttributes, density, title), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        val divider = styledAttributes.getDrawable(DIVIDER)
        if (divider != null) {
            val line = View(context)
            line.background = divider
            screen.addView(line, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, maxOf(divider.intrinsicHeight, 1)))
        }

        screen.addView(settings, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        return screen
    } finally {
        styledAttributes.recycle()
    }
}

private fun row(context: Context, styledAttributes: TypedArray, density: Float, title: String): View {
    val row = LinearLayout(context)
    row.orientation = LinearLayout.HORIZONTAL
    row.gravity = Gravity.CENTER_VERTICAL
    row.minimumHeight = (56 * density).roundToInt()
    val padding = (16 * density).roundToInt()
    row.setPadding(padding, 0, padding, 0)
    val ripple = styledAttributes.getDrawable(RIPPLE)
    if (ripple != null) row.background = ripple
    row.setOnClickListener(OpenSettingsOnClick)

    val titleView = TextView(context)
    titleView.text = title
    titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
    val primary = styledAttributes.getColorStateList(PRIMARY_TEXT)
    if (primary != null) titleView.setTextColor(primary)
    row.addView(titleView, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

    val chevron = TextView(context)
    chevron.text = CHEVRON
    chevron.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
    val secondary = styledAttributes.getColorStateList(SECONDARY_TEXT)
    if (secondary != null) chevron.setTextColor(secondary)
    row.addView(chevron)
    return row
}

private object OpenSettingsOnClick : View.OnClickListener {
    override fun onClick(view: View) {
        openSettings()
    }
}
