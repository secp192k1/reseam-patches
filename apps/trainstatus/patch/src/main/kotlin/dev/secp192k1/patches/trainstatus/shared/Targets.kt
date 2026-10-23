package dev.secp192k1.patches.trainstatus.shared

import app.reseam.patch.klass
import app.reseam.patch.method

// A manifest entry point, so R8 keeps its name
val mainActivity = klass("com.jsepol.trainstatuspt.MainActivity")

val mainActivityCreate = mainActivity.method("onCreate")
