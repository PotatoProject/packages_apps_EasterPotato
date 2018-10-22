/*
 *  Copyright (c) 2016 Agens AS (http://agens.no/).
 *  All rights reserved.
 *
 *  This code is licensed under the MIT License.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files(the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions :
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package no.agens.depth;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import no.agens.depth.lib.headers.Renderable;

public class Smoke extends Renderable {
    private static final float WIND_SENSITIVITY = 7f;
    private float height, width;
    private int numberOfTurns;
    private float density;
    private final float[] drawingVerts = new float[TOTAL_SLICES_COUNT * 2];
    private final float[] staticVerts = new float[TOTAL_SLICES_COUNT * 2];
    private static final int HORIZONTAL_SLICES = 1;
    private static final int VERTICAL_SLICES = 80;
    private static final int TOTAL_SLICES_COUNT = (HORIZONTAL_SLICES + 1) * (VERTICAL_SLICES + 1);


    private void createVerts() {

        float xDimesion = (float) bitmap.getWidth();
        float yDimesion = (float) bitmap.getHeight();

        int index = 0;

        for (int y = 0; y <= VERTICAL_SLICES; y++) {
            float fy = yDimesion * y / VERTICAL_SLICES;
            for (int x = 0; x <= HORIZONTAL_SLICES; x++) {
                float fx = xDimesion * x / HORIZONTAL_SLICES;
                setXY(drawingVerts, index, fx, fy);
                setXY(staticVerts, index, fx, fy);
                index += 1;
            }
        }
    }

    private void setXY(float[] array, int index, float x, float y) {
        array[index * 2 + 0] = x;
        array[index * 2 + 1] = y;
    }

    public Smoke(Bitmap bitmap, float x, float y, float height, float width, int numberOfTurns, float density) {
        super(bitmap, x, y);
        this.height = height;
        this.width = width;
        this.numberOfTurns = numberOfTurns;
        paint.setStyle(Paint.Style.STROKE);
        this.density = density;
        createVerts();
        createPath();

        pathPointOffsetAnim = ValueAnimator.ofFloat(0, ((bitmap.getHeight() / (float) numberOfTurns) * 2f) / bitmap.getHeight()).setDuration(1500);
        pathPointOffsetAnim.setRepeatCount(ValueAnimator.INFINITE);
        pathPointOffsetAnim.setRepeatMode(ValueAnimator.RESTART);
        pathPointOffsetAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                pathPointOffset = (float) animation.getAnimatedValue();
            }
        });
        pathPointOffsetAnim.setInterpolator(new LinearInterpolator());
        pathPointOffsetAnim.start();

        createPath();
    }

    private ValueAnimator pathPointOffsetAnim;

    public void destroy() {
        super.destroy();
        pathPointOffsetAnim.cancel();
    }

    @Override
    public void pause() {
        super.pause();
        pathPointOffsetAnim.pause();
    }

    @Override
    public void resume() {
        super.resume();
        pathPointOffsetAnim.resume();
    }

    private Path smokePath = new Path();
    private Paint paint = new Paint();
    private float pathPointOffset = 1;

    @Override
    public void draw(Canvas canvas) {


        //  alphaCanvas.drawPath(smokePath, paint);
        canvas.drawBitmapMesh(bitmap, HORIZONTAL_SLICES, VERTICAL_SLICES, drawingVerts, 0, null, 0, paint);
    }

    public void setY(float y) {
        this.y = y;
        createPath();
    }

    @Override
    public void update(float deltaTime, float wind) {
        matchVertsToPath(wind);
    }

    public void setAlpha(float alpha) {
        paint.setAlpha((int) (alpha * 255));
    }

    public float getAlpha() {
        return paint.getAlpha() / 255;
    }

    private void createPath() {
        smokePath.reset();
        smokePath.moveTo(x, y);

        int step = (int) (height / numberOfTurns);
        boolean goLeft = true;
        for (int i = 0; i < numberOfTurns; i++) {
            if (goLeft)
                smokePath.cubicTo(x, y - step * i, x + width, y - step * i - step / 2, x, y - step * i - step);
            else
                smokePath.cubicTo(x, y - step * i, x - width, y - step * i - step / 2, x, y - step * i - step);

            goLeft = !goLeft;
        }

    }

    private float[] coords = new float[2];
    private float[] coords2 = new float[2];

    private void matchVertsToPath(float wind) {
        PathMeasure pm = new PathMeasure(smokePath, false);

        for (int i = 0; i < staticVerts.length / 2; i++) {

            float yIndexValue = staticVerts[i * 2 + 1];
            float xIndexValue = staticVerts[i * 2];

            float percentOffsetY = (0.000001f + yIndexValue) / bitmap.getHeight();
            float percentOffsetY2 = (0.000001f + yIndexValue) / (bitmap.getHeight() + ((bitmap.getHeight() / numberOfTurns) * 4f));
            percentOffsetY2 += pathPointOffset;
            pm.getPosTan(pm.getLength() * (1f - percentOffsetY), coords, null);
            pm.getPosTan(pm.getLength() * (1f - percentOffsetY2), coords2, null);

            if (xIndexValue == 0) {
                float desiredXCoord = coords2[0] - (bitmap.getWidth()) / 2;
                desiredXCoord -= (desiredXCoord - x) * percentOffsetY;
                desiredXCoord += (wind / 3f) * density + ((wind * WIND_SENSITIVITY) * (1f - smokeExponentionWindStuff.getInterpolation(percentOffsetY)));
                setXY(drawingVerts, i, desiredXCoord, coords[1]);
            } else {
                float desiredXCoord = coords2[0] + (bitmap.getWidth()) / 2;
                desiredXCoord -= (desiredXCoord - x) * percentOffsetY;
                desiredXCoord += (wind / 3f) * density + ((wind * WIND_SENSITIVITY) * (1f - smokeExponentionWindStuff.getInterpolation(percentOffsetY)));
                setXY(drawingVerts, i, desiredXCoord, coords[1]);

            }
        }
    }

    private DecelerateInterpolator smokeExponentionWindStuff = new DecelerateInterpolator(1f);
}