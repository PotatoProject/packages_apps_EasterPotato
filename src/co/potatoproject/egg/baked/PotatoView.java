package co.potatoproject.egg.baked;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.VectorDrawable;
import android.util.AttributeSet;
import android.util.FloatProperty;
import android.view.View;
import android.widget.Toast;

import java.util.Random;

import no.agens.depth.Smoke;
import co.potatoproject.egg.R;
import no.agens.depth.lib.tween.FrameRateCounter;

public class PotatoView extends View implements View.OnClickListener {

    private static final boolean DBG = true;
    private static final Paint DEBUG_PAINT = DBG ? new Paint() : null;

    static {
        if (DEBUG_PAINT != null) {
            DEBUG_PAINT.setTextSize(20);
            DEBUG_PAINT.setColor(Color.RED);
        }
    }

    private static final float WIND_STRENGTH = 20f;
    private static final FloatProperty<PotatoView> PROGRESS = new FloatProperty<PotatoView>("progress") {
        @Override
        public void setValue(PotatoView potato, float progress) {
            potato.setProgress(progress);
        }

        @Override
        public Float get(PotatoView potato) {
            return potato.progress;
        }
    };
    private static final ArgbEvaluator EVALUATOR = new ArgbEvaluator();

    private static final Random RANDOM = new Random();

    private static final long DURATION_TO_RAW = 500;
    private static final int DURATION_TO_BAKED_MIN = 12000;
    private static final int DURATION_TO_BURNED_MIN = 15000;
    private static final int DURATION_RANDOM_RANGE = DURATION_TO_BURNED_MIN / 3;

    private static final int STATE_RAW = 0;
    private static final int STATE_BAKED = 1;
    private static final int STATE_BURNED = 2;

    private static final float TOLERANCE = 0.15f;

    private int state = STATE_RAW;
    private int nextState = STATE_RAW;
    private Smoke smoke;
    private Animator animator;
    private float progress = 0f;
    private boolean userClicked = false;

    private int potatoColor;

    private int potatoRawColor;
    private int potatoBakedColor;
    private int potatoBurnedColor;
    private int previousColor = -1;

    private VectorDrawable potatoDrawable;
    private float potatoX;
    private float potatoY;

    public PotatoView(Context context) {
        super(context);
    }

    public PotatoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PotatoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (smoke == null && getWidth() != 0)
            init();
    }

    private void init() {
        Bitmap sBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.smoke);
        potatoDrawable = (VectorDrawable) getResources().getDrawable(R.drawable.ic_potato);
        float size = getMeasuredHeight() * 0.3f;
        potatoX = getMeasuredWidth() / 2 - size / 2;
        potatoY = getMeasuredHeight() - size;
        potatoDrawable.setBounds(0, 0, (int) size, (int) size);
        float density = getResources().getDisplayMetrics().density;
        smoke = new Smoke(sBitmap, getMeasuredWidth() / 2, getMeasuredHeight() * 0.68f, getMeasuredHeight() * 0.68f, 80 * density, 8, density);
        setOnClickListener(this);
        potatoRawColor = getContext().getColor(R.color.potato_raw);
        potatoBakedColor = getContext().getColor(R.color.potato_baked);
        potatoBurnedColor = getContext().getColor(R.color.potato_burned);
        setProgress(0f);
        startCooking();
    }

    @Override
    public void onClick(View v) {
        if (!userClicked) {
            userClicked = true;
            if (animator != null) {
                animator.cancel();
            }
            if (progress < STATE_BAKED - TOLERANCE) {
                Toast.makeText(getContext(), "Raw af", Toast.LENGTH_LONG).show();
            } else if (progress > STATE_BAKED + TOLERANCE) {
                Toast.makeText(getContext(), "Burned af", Toast.LENGTH_LONG).show();
            } else if (progress == STATE_BAKED) {
                Toast.makeText(getContext(), "This is statistically impossible", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "Nice af", Toast.LENGTH_LONG).show();
            }
            // Start over
            animateToState(STATE_RAW, DURATION_TO_RAW);
        }
    }

    private void startCooking() {
        if (!userClicked && state != STATE_RAW) {
            animateToState(STATE_RAW, DURATION_TO_RAW);
        }
        userClicked = false;
        animateToState(STATE_BAKED, DURATION_TO_BAKED_MIN + RANDOM.nextInt(DURATION_RANDOM_RANGE));
    }

    private void animateToState(int toState, long duration) {
        if (userClicked && toState != STATE_RAW) {
            return;
        }
        nextState = toState;
        int fromState = state;
        animator = ObjectAnimator.ofFloat(this, PROGRESS, progress, (float) toState);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                invalidate();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                state = toState;
                if (animator != null) {
                    animator = null;
                    onStateChanged(fromState, toState);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animator = null;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animator.setDuration(duration).start();
    }

    private void setProgress(float progress) {
        this.progress = progress;

        smoke.setAlpha(Math.max(0f, (progress - 0.2f) / 2));

        int fromColor = potatoRawColor;
        switch (state) {
            case STATE_BAKED:
                fromColor = potatoBakedColor;
                break;
            case STATE_BURNED:
                fromColor = potatoBurnedColor;
                break;
        }
        boolean reverse = false;
        int toColor = potatoBakedColor;
        switch (nextState) {
            case STATE_BURNED:
                toColor = potatoBurnedColor;
                break;
            case STATE_RAW:
                toColor = potatoRawColor;
                reverse = true;
                break;
        }
        if (progress > 1)
            progress -= 1;
        potatoColor = interpolateColor(fromColor, toColor, progress);

        invalidate();
    }

    // used to take colors mix according to proportion
    private int interpolateColor(final int a, final int b,
                                 final float proportion) {
        final float[] hsva = new float[3];
        final float[] hsvb = new float[3];
        Color.colorToHSV(a, hsva);
        Color.colorToHSV(b, hsvb);
        for (int i = 0; i < 3; i++) {
            hsvb[i] = interpolate(hsva[i], hsvb[i], proportion);
        }
        return Color.HSVToColor(hsvb);
    }

    private float interpolate(final float a, final float b,
                              final float proportion) {
        return (a + ((b - a) * proportion));
    }

    private void onStateChanged(int fromState, int toState) {
        switch (toState) {
            case STATE_RAW:
                // Let's start over, shall we?
                startCooking();
                break;
            case STATE_BAKED:
                if (fromState != STATE_RAW) {
                    throw new IllegalStateException("Transition to STATE_BAKED is only possible from STATE_RAW");
                }
                animateToState(STATE_BURNED, DURATION_TO_BURNED_MIN + RANDOM.nextInt(DURATION_RANDOM_RANGE));
                break;
            case STATE_BURNED:
                if (fromState != STATE_BAKED) {
                    throw new IllegalStateException("Transition to STATE_BURNED is only possible from STATE_BAKED");
                }
                Toast.makeText(getContext(), "Wew your potato was burned", Toast.LENGTH_LONG).show();
                animateToState(STATE_RAW, DURATION_TO_RAW);
                break;
            default:
                throw new IllegalStateException("Illegal target state " + toState);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        smoke.destroy();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        boolean updated = false;

        float deltaTime = FrameRateCounter.timeStep();
        if (progress > STATE_RAW) {
            smoke.draw(canvas);
            smoke.update(deltaTime, WIND_STRENGTH);
            updated = true;
        }

        if (previousColor != potatoColor) {
            previousColor = potatoColor;
            potatoDrawable.setColorFilter(potatoColor, PorterDuff.Mode.SRC_IN);
            updated = true;
        }

        if (DBG) {
            canvas.drawText(String.valueOf(progress), 20f, 30f, DEBUG_PAINT);
        }

        if (updated) {
            canvas.translate(potatoX, potatoY);
            potatoDrawable.draw(canvas);
        }


        if (updated) {
            invalidate();
        }
    }
}
