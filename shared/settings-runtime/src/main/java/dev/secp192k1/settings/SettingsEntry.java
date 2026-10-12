// SPDX-FileCopyrightText: 2026 secp192k1
// SPDX-License-Identifier: AGPL-3.0-or-later

package dev.secp192k1.settings;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import app.reseam.runtime.Reseam;

@SuppressWarnings("unused")
public final class SettingsEntry {
    private static final String TITLE = "Reseam Settings";
    private static final String CHEVRON = "›";

    // One theme lookup serves the whole row; the ids are ascending, the order styled attribute lookups use.
    private static final int[] THEME_ATTRIBUTES = {android.R.attr.textColorPrimary, android.R.attr.textColorSecondary, android.R.attr.listDivider, android.R.attr.selectableItemBackground,};
    private static final int PRIMARY_TEXT = 0;
    private static final int SECONDARY_TEXT = 1;
    private static final int DIVIDER = 2;
    private static final int RIPPLE = 3;

    private SettingsEntry() {
    }

    // Apps draw their settings lists in ways a patch cannot add to, such as Compose, so the row goes above the whole list.
    public static View addSettingsRow(View settings) {
        Context context = settings.getContext();
        float density = context.getResources().getDisplayMetrics().density;
        TypedArray theme = context.obtainStyledAttributes(THEME_ATTRIBUTES);
        try {
            LinearLayout screen = new LinearLayout(context);
            screen.setOrientation(LinearLayout.VERTICAL);
            screen.addView(row(context, theme, density), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            Drawable divider = theme.getDrawable(DIVIDER);
            if (divider != null) {
                View line = new View(context);
                line.setBackground(divider);
                int height = Math.max(divider.getIntrinsicHeight(), 1);
                screen.addView(line, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
            }
            screen.addView(settings, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
            return screen;
        } finally {
            theme.recycle();
        }
    }

    // For a row the patch added to the app's own settings layout, so it looks like the app's other rows.
    public static void openOnClick(View row) {
        if (row != null) row.setOnClickListener(view -> Reseam.openSettings(SettingsCache.BUNDLE));
    }

    private static View row(Context context, TypedArray theme, float density) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(Math.round(56 * density));
        int padding = Math.round(16 * density);
        row.setPadding(padding, 0, padding, 0);
        Drawable ripple = theme.getDrawable(RIPPLE);
        if (ripple != null) row.setBackground(ripple);
        row.setOnClickListener(view -> Reseam.openSettings(SettingsCache.BUNDLE));

        TextView title = new TextView(context);
        title.setText(TITLE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        ColorStateList primary = theme.getColorStateList(PRIMARY_TEXT);
        if (primary != null) title.setTextColor(primary);
        row.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView chevron = new TextView(context);
        chevron.setText(CHEVRON);
        chevron.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        ColorStateList secondary = theme.getColorStateList(SECONDARY_TEXT);
        if (secondary != null) chevron.setTextColor(secondary);
        row.addView(chevron);
        return row;
    }
}
