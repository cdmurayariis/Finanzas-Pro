package com.example.aplicacion_financiero.utils;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ProgressBar;

import com.example.aplicacion_financiero.R;
import com.google.android.material.card.MaterialCardView;

public final class UiAnimations {

    private UiAnimations() {
    }

    public static boolean areAnimationsEnabled(Context context) {
        try {
            float scale = Settings.Global.getFloat(context.getContentResolver(),
                    Settings.Global.ANIMATOR_DURATION_SCALE, 1f);
            return scale > 0f;
        } catch (Exception e) {
            return true;
        }
    }

    public static void staggerIn(long initialDelay, long stepDelay, View... views) {
        if (views == null) {
            return;
        }
        Context context = views.length > 0 ? views[0].getContext() : null;
        if (context == null || !areAnimationsEnabled(context)) {
            return;
        }
        long delay = initialDelay;
        for (View view : views) {
            if (view == null) {
                continue;
            }
            view.setAlpha(0f);
            view.setTranslationY(dp(view.getContext(), 26));
            view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(delay)
                    .setDuration(340)
                    .setInterpolator(new DecelerateInterpolator(2f))
                    .start();
            delay += stepDelay;
        }
    }

    public static void fadeScaleIn(View view, long delay) {
        if (view == null) {
            return;
        }
        if (!areAnimationsEnabled(view.getContext())) {
            view.setAlpha(1f);
            view.setScaleX(1f);
            view.setScaleY(1f);
            return;
        }
        view.setAlpha(0f);
        view.setScaleX(0.88f);
        view.setScaleY(0.88f);
        view.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setStartDelay(delay)
                .setDuration(520)
                .setInterpolator(new DecelerateInterpolator(2f))
                .start();
    }

    public static void pressScale(View view) {
        attachPressBounce(view, null);
    }

    public static void attachPressBounce(View target, View icon) {
        attachPressBounce(target, icon, 0.95f, -10f);
    }

    /**
     * Adds a bounce micro-interaction without consuming the click event, so existing
     * click listeners keep working exactly as before.
     */
    public static void attachPressBounce(View target, View icon, float pressedScale,
                                         float iconRotation) {
        if (target == null || !areAnimationsEnabled(target.getContext())) {
            return;
        }
        target.setOnTouchListener((view, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    view.animate().cancel();
                    view.animate().scaleX(pressedScale).scaleY(pressedScale)
                            .setDuration(90)
                            .setInterpolator(new DecelerateInterpolator())
                            .start();
                    if (icon != null) {
                        icon.animate().cancel();
                        icon.animate().rotation(iconRotation)
                                .scaleX(0.86f).scaleY(0.86f)
                                .setDuration(100)
                                .setInterpolator(new DecelerateInterpolator())
                                .start();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    view.animate().cancel();
                    view.animate().scaleX(1f).scaleY(1f)
                            .setDuration(320)
                            .setInterpolator(new OvershootInterpolator(1.4f))
                            .start();
                    if (icon != null) {
                        icon.animate().cancel();
                        icon.animate().rotation(0f)
                                .scaleX(1f).scaleY(1f)
                                .setDuration(380)
                                .setInterpolator(new OvershootInterpolator(2f))
                                .start();
                    }
                    break;
                default:
                    break;
            }
            return false;
        });
    }

    public static void updateCardSelection(MaterialCardView card, View icon, boolean selected,
                                           int defaultStrokeColor, int selectedStrokeColor,
                                           boolean animate) {
        if (card == null) {
            return;
        }
        Context context = card.getContext();
        float targetScale = selected ? 1.012f : 1f;
        float targetElevation = selected ? dp(context, 5f) : 0f;
        int targetStrokeColor = selected ? selectedStrokeColor : defaultStrokeColor;
        int targetStrokeWidth = Math.round(dp(context, selected ? 2f : 1f));

        if (!animate || !areAnimationsEnabled(context)) {
            card.animate().cancel();
            card.setScaleX(targetScale);
            card.setScaleY(targetScale);
            card.setCardElevation(targetElevation);
            card.setStrokeColor(targetStrokeColor);
            card.setStrokeWidth(targetStrokeWidth);
            if (icon != null) {
                icon.animate().cancel();
                icon.setRotation(0f);
                icon.setScaleX(1f);
                icon.setScaleY(1f);
            }
            return;
        }

        card.animate().cancel();
        card.animate().scaleX(targetScale).scaleY(targetScale)
                .setDuration(220)
                .setInterpolator(new DecelerateInterpolator(2f))
                .start();

        ValueAnimator elevationAnimator = ValueAnimator.ofFloat(card.getCardElevation(),
                targetElevation);
        elevationAnimator.setDuration(220);
        elevationAnimator.setInterpolator(new DecelerateInterpolator(2f));
        elevationAnimator.addUpdateListener(animation ->
                card.setCardElevation((float) animation.getAnimatedValue()));
        elevationAnimator.start();

        ValueAnimator strokeAnimator = ValueAnimator.ofArgb(
                selected ? defaultStrokeColor : selectedStrokeColor, targetStrokeColor);
        strokeAnimator.setDuration(220);
        strokeAnimator.setInterpolator(new DecelerateInterpolator(2f));
        strokeAnimator.addUpdateListener(animation ->
                card.setStrokeColor((int) animation.getAnimatedValue()));
        strokeAnimator.start();
        card.setStrokeWidth(targetStrokeWidth);

        if (icon != null) {
            icon.animate().cancel();
            icon.setRotation(0f);
            icon.animate().rotation(selected ? -12f : 12f)
                    .scaleX(selected ? 1.08f : 0.94f)
                    .scaleY(selected ? 1.08f : 0.94f)
                    .setDuration(120)
                    .setInterpolator(new DecelerateInterpolator())
                    .withEndAction(() -> icon.animate().rotation(0f)
                            .scaleX(1f).scaleY(1f)
                            .setDuration(300)
                            .setInterpolator(new OvershootInterpolator(2f))
                            .start())
                    .start();
        }
    }

    public static void shake(View view) {
        if (view == null || !areAnimationsEnabled(view.getContext())) {
            return;
        }
        view.startAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.shake));
    }

    public static void floatLoop(View view, long delay) {
        if (view == null || !areAnimationsEnabled(view.getContext())) {
            return;
        }
        view.postDelayed(() -> {
            ObjectAnimator animator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y,
                    0f, -dp(view.getContext(), 9));
            animator.setDuration(2300);
            animator.setRepeatCount(ObjectAnimator.INFINITE);
            animator.setRepeatMode(ObjectAnimator.REVERSE);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.start();
        }, delay);
    }

    public static void animateProgress(ProgressBar progressBar, int target) {
        if (progressBar == null) {
            return;
        }
        Object running = progressBar.getTag();
        if (running instanceof ObjectAnimator) {
            ((ObjectAnimator) running).cancel();
            progressBar.setTag(null);
        }
        if (!areAnimationsEnabled(progressBar.getContext())) {
            progressBar.setProgress(target);
            return;
        }
        if (progressBar.getProgress() == target) {
            return;
        }
        ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress",
                progressBar.getProgress(), target);
        animator.setDuration(650);
        animator.setInterpolator(new DecelerateInterpolator(2f));
        progressBar.setTag(animator);
        animator.start();
    }

    private static float dp(Context context, float value) {
        return value * context.getResources().getDisplayMetrics().density;
    }
}
