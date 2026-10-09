package dev.secp192k1.patches.anydesk

import app.reseam.patch.klass

internal const val PACKAGE = "com.anydesk.anydeskandroid"
internal val nativeBridge = klass("com.anydesk.jni.JniAdExt")
internal val mainFragment = klass("$PACKAGE.gui.fragment.MainFragment")
