package dev.secp192k1.views;

import android.view.View;
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
