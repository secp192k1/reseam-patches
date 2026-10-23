@file:JvmName("HiddenViews")
@file:Suppress("unused")

package dev.secp192k1.anydesk.views

import android.view.View
import android.view.ViewTreeObserver
import app.reseam.runtime.Export
import java.util.Collections
import java.util.WeakHashMap

// A view looked up again keeps its first guard, and weak keys let the views be collected
private val guardedViews: MutableSet<View> = Collections.newSetFromMap(WeakHashMap())

@Export
fun keepGone(view: View?) {
    if (view == null) return

    view.visibility = View.GONE
    if (!guardedViews.add(view)) return

    val guard = Guard(view)
    view.addOnAttachStateChangeListener(guard)
    if (view.isAttachedToWindow) guard.onViewAttachedToWindow(view)
}

// The app may show the view again at any time, so it is hidden again before the frame that would draw it
private class Guard(private val view: View) : View.OnAttachStateChangeListener, ViewTreeObserver.OnPreDrawListener {
    private var observer: ViewTreeObserver? = null

    override fun onViewAttachedToWindow(attached: View) {
        if (observer != null) return

        val attachedObserver = attached.viewTreeObserver
        attachedObserver.addOnPreDrawListener(this)
        observer = attachedObserver
    }

    override fun onViewDetachedFromWindow(detached: View) {
        val attachedObserver = observer
        if (attachedObserver != null && attachedObserver.isAlive) attachedObserver.removeOnPreDrawListener(this)
        observer = null
    }

    override fun onPreDraw(): Boolean {
        if (view.visibility == View.GONE) return true

        view.visibility = View.GONE
        return false
    }
}
