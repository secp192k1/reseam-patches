package dev.secp192k1.patches.theme

import app.reseam.patch.ExtClass
import app.reseam.patch.MethodTarget
import app.reseam.patch.Type
import app.reseam.patch.after
import app.reseam.patch.before
import app.reseam.patch.settings.ChoiceSetting
import dev.secp192k1.patches.settings.valueOf

private object AppTheme : ExtClass("dev.secp192k1.theme.AppTheme") {
    val theme by static(returns = Type.String)
    val apply by static(Type.Context)
    val refresh by static("android.app.Activity")
}

fun installAppTheme(setting: ChoiceSetting, applicationStart: MethodTarget, firstScreens: List<MethodTarget>) {
    AppTheme.theme.implement { returnValue(valueOf(setting)) }
    applicationStart.before { call(AppTheme.apply, thisObject) }
    firstScreens.forEach { screen -> screen.after { call(AppTheme.refresh, thisObject) } }
}
