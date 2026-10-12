package dev.secp192k1.views;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;

@SuppressWarnings("unused")
public final class HiddenViews {
    private HiddenViews() {}

    public static void keepGone(View view) {
        if (view == null) return;

        view.setVisibility(View.GONE);
        Guard guard = new Guard(view);
        view.addOnAttachStateChangeListener(guard);
        if (view.isAttachedToWindow()) guard.onViewAttachedToWindow(view);
    }

    public static void keepAncestorGone(View view, int levels, int siblingsBefore) {
        View ancestor = view;
        for (int level = 0; level < levels && ancestor != null; level++) {
            ViewParent parent = ancestor.getParent();
            ancestor = parent instanceof View ? (View) parent : null;
        }
        if (ancestor == null) return;

        keepGone(ancestor);
        if (siblingsBefore == 0 || !(ancestor.getParent() instanceof ViewGroup group)) return;
        int index = group.indexOfChild(ancestor);
        for (int sibling = Math.max(0, index - siblingsBefore); sibling < index; sibling++) {
            keepGone(group.getChildAt(sibling));
        }
    }

    private static final class Guard implements View.OnAttachStateChangeListener, ViewTreeObserver.OnPreDrawListener {
        private final View view;
        private ViewTreeObserver observer;

        Guard(View view) {
            this.view = view;
        }

        @Override
        @SuppressWarnings("NullableProblems")
        public void onViewAttachedToWindow(View attached) {
            if (observer != null) return;

            observer = attached.getViewTreeObserver();
            observer.addOnPreDrawListener(this);
        }

        @Override
        @SuppressWarnings("NullableProblems")
        public void onViewDetachedFromWindow(View detached) {
            if (observer != null && observer.isAlive()) observer.removeOnPreDrawListener(this);
            observer = null;
        }

        @Override
        public boolean onPreDraw() {
            if (view.getVisibility() == View.GONE) return true;
            view.setVisibility(View.GONE);
            return false;
        }
    }
}
