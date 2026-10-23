package dev.secp192k1.patches.trainstatus.shared

import app.reseam.patch.CodeScope
import app.reseam.patch.Otherwise
import app.reseam.patch.settings.ChoiceSetting
import app.reseam.patch.settings.valueOf

// equals runs on the constant, so a missing stored value reads as not chosen instead of throwing
fun CodeScope.whenChosen(setting: ChoiceSetting, value: String, block: CodeScope.() -> Unit): Otherwise =
    whenTrue(string(value).callVirtual("java.lang.String", "equals", "(Ljava/lang/Object;)Z", valueOf(setting)), block)
