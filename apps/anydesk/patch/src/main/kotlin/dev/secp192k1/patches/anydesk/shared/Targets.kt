package dev.secp192k1.patches.anydesk.shared

import app.reseam.patch.klass

// Native code calls into this class and its callbacks by name, so R8 keeps those names
val nativeBridge = klass("com.anydesk.jni.JniAdExt")

// nav_main names this fragment, so R8 keeps the class name
val mainFragment = klass("com.anydesk.anydeskandroid.gui.fragment.MainFragment")
