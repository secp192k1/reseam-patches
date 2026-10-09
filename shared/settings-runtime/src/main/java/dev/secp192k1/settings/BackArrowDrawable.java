// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later
// SPDX-FileCopyrightText: 2026 secp192k1
// Modified by secp192k1 on 8 October 2026: moved to the package dev.secp192k1.settings,
// so this copy never collides with the official Reseam bundle's settings runtime.
// Modified by secp192k1 on 9 October 2026: scales the glyph when the bounds change instead of on
// every draw.

package dev.secp192k1.settings;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/**
 * Material's "arrow back" glyph, drawn from its path. The framework's own arrow is not a public
 * resource and the host app's is obfuscated, so the runtime cannot rely on finding one.
 */
final class BackArrowDrawable extends Drawable {

    private static final float GRID = 24f;

    private static final RectF GRID_BOUNDS = new RectF(0f, 0f, GRID, GRID);

    private static final Path GLYPH = new Path();

    static {
        GLYPH.moveTo(20f, 11f);
        GLYPH.lineTo(7.83f, 11f);
        GLYPH.lineTo(13.42f, 5.41f);
        GLYPH.lineTo(12f, 4f);
        GLYPH.lineTo(4f, 12f);
        GLYPH.lineTo(12f, 20f);
        GLYPH.lineTo(13.41f, 18.59f);
        GLYPH.lineTo(7.83f, 13f);
        GLYPH.lineTo(20f, 13f);
        GLYPH.close();
    }

    private final int size;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path scaled = new Path();

    BackArrowDrawable(int size) {
        this.size = size;
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected void onBoundsChange(Rect bounds) {
        Matrix matrix = new Matrix();
        matrix.setRectToRect(GRID_BOUNDS, new RectF(bounds), Matrix.ScaleToFit.CENTER);
        GLYPH.transform(matrix, scaled);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawPath(scaled, paint);
    }

    @Override
    public int getIntrinsicWidth() {
        return size;
    }

    @Override
    public int getIntrinsicHeight() {
        return size;
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
