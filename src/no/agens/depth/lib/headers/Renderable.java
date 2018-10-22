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
package no.agens.depth.lib.headers;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

public class Renderable {
    public float x;

    public void setY(float y) {
        this.y = y;
    }

    public float y;

    public float translationY;
    public float translationX;
    public Bitmap bitmap;

    public float scaleX = 1f;
    public float scaleY = 1f;

    public Renderable(Bitmap bitmap, float x, float y) {
        this.x = x;
        this.y = y;
        this.bitmap = bitmap;
    }

    public void draw(Canvas canvas) {
        canvas.save();
        canvas.drawBitmap(bitmap, x + translationX / 2, y + translationY, null);
        canvas.restore();
    }

    public void drawStretched(Canvas canvas, float parentWidth) {
        canvas.save();
        canvas.drawBitmap(bitmap, null, new RectF(x + translationX / 2, y + translationY, x + translationX / 2 + parentWidth, y + translationY + bitmap.getHeight()), null);
        canvas.restore();
    }

    public void setTranslationY(Float translationY) {
        this.translationY = translationY;
    }

    public float getTranslationY() {
        return translationY;
    }

    public void setTranslationY(float translationY) {
        this.translationY = translationY;
    }

    public float getTranslationX() {
        return translationX;
    }

    public void setTranslationX(float translationX) {
        this.translationX = translationX;
    }

    public void setScale(float scale, float scale1) {

    }

    public void update(float deltaTime, float wind) {

    }

    public void destroy() {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    public void pause() {

    }

    public void resume() {

    }
}