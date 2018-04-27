package com.devbatch.ecommerce.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;


public class SadeeqAnimation {

    /**
     * This animation fades the view in by animating its alpha property from 0 to 1.
     */
    public static class FadeInAnimation extends Animation {

        TimeInterpolator interpolator;
        long duration;
        AnimationListener listener;

        /**
         * This animation fades the view in by animating its alpha property from 0
         * to 1.
         *
         * @param view The view to be animated.
         */
        public FadeInAnimation(View view) {
            if (view == null) {
                return;
            }
            this.view = view;
            interpolator = new AccelerateDecelerateInterpolator();
            duration = DURATION_LONG;
            listener = null;
        }

        @Override
        public void animate() {
            getAnimatorSet().start();
        }


        public AnimatorSet getAnimatorSet() {
            view.setAlpha(0f);
            view.setVisibility(View.VISIBLE);

            AnimatorSet fadeSet = new AnimatorSet();
            fadeSet.play(ObjectAnimator.ofFloat(view, View.ALPHA, 1f));
            fadeSet.setInterpolator(interpolator);
            fadeSet.setDuration(duration);
            fadeSet.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (getListener() != null) {
                        getListener().onAnimationEnd(FadeInAnimation.this);
                    }
                }
            });
            return fadeSet;
        }

        /**
         * @return The interpolator of the entire animation.
         */
        public TimeInterpolator getInterpolator() {
            return interpolator;
        }

        /**
         * @param interpolator The interpolator of the entire animation to set.
         */
        public FadeInAnimation setInterpolator(TimeInterpolator interpolator) {
            this.interpolator = interpolator;
            return this;
        }

        /**
         * @return The duration of the entire animation.
         */
        public long getDuration() {
            return duration;
        }

        /**
         * @return The duration of the entire animation to set.
         */
        public FadeInAnimation setDuration(long duration) {
            this.duration = duration;
            return this;
        }

        /**
         * @return The listener for the end of the animation.
         */
        public AnimationListener getListener() {
            return listener;
        }

        /**
         * @return The listener to set for the end of the animation.
         */
        public FadeInAnimation setListener(AnimationListener listener) {
            this.listener = listener;
            return this;
        }

    }

    private static abstract class Animation {

        // constants
        public static final int DIRECTION_LEFT = 1;
        public static final int DIRECTION_RIGHT = 2;
        public static final int DIRECTION_UP = 3;
        public static final int DIRECTION_DOWN = 4;

        public static final int DURATION_DEFAULT = 300; // 300 ms
        public static final int DURATION_SHORT = 100;    // 100 ms
        public static final int DURATION_LONG = 500;    // 500 ms

        View view;

        /**
         * This method animates the properties of the view specific to the Animation
         * object.
         */
        public abstract void animate();

    }

    public static class FadeOutAnimation extends Animation {

        TimeInterpolator interpolator;
        long duration;
        AnimationListener listener;


        public FadeOutAnimation(View view) {
            if (view == null) {
                return;
            }
            this.view = view;
            interpolator = new AccelerateDecelerateInterpolator();
            duration = DURATION_LONG;
            listener = null;
        }

        @Override
        public void animate() {
            getAnimatorSet().start();
        }

        public AnimatorSet getAnimatorSet() {
            final float originalAlpha = view.getAlpha();
            AnimatorSet fadeSet = new AnimatorSet();
            fadeSet.play(ObjectAnimator.ofFloat(view, View.ALPHA, 0f));
            fadeSet.setInterpolator(interpolator);
            fadeSet.setDuration(duration);
            fadeSet.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                    view.setAlpha(originalAlpha);
                    if (getListener() != null) {
                        getListener().onAnimationEnd(FadeOutAnimation.this);
                    }
                }
            });
            return fadeSet;
        }

        /**
         * @return The interpolator of the entire animation.
         */
        public TimeInterpolator getInterpolator() {
            return interpolator;
        }

        /**
         * @param interpolator The interpolator of the entire animation to set.
         */
        public FadeOutAnimation setInterpolator(TimeInterpolator interpolator) {
            this.interpolator = interpolator;
            return this;
        }

        /**
         * @return The duration of the entire animation.
         */
        public long getDuration() {
            return duration;
        }

        /**
         * @param duration The duration of the entire animation to set.
         */
        public FadeOutAnimation setDuration(long duration) {
            this.duration = duration;
            return this;
        }

        /**
         * @return The listener for the end of the animation.
         */
        public AnimationListener getListener() {
            return listener;
        }

        /**
         * @param listener The listener to set for the end of the animation.
         */
        public FadeOutAnimation setListener(AnimationListener listener) {
            this.listener = listener;
            return this;
        }

    }


    /**
     * This interface is a custom listener to determine the end of an animation.
     */
    public interface AnimationListener {

        /**
         * This method is called when the animation ends.
         *
         * @param animation The Animation object.
         */
        void onAnimationEnd(Animation animation);
    }

    public static void applyBubbleAnimation(View view) {
        ScaleAnimation scale = new ScaleAnimation(0.85f, 1, 0.85f, 1, ScaleAnimation.RELATIVE_TO_SELF, .5f, ScaleAnimation.RELATIVE_TO_SELF, .5f);
        scale.setDuration(2000);
        scale.setInterpolator(new LinearInterpolator());
        scale.setRepeatCount(ScaleAnimation.INFINITE);
        scale.setRepeatMode(ScaleAnimation.REVERSE);
        view.startAnimation(scale);
    }

    public static void applyBubbleAnimation(View view, float from, float to) {
        ScaleAnimation scale = new ScaleAnimation(from, to, from, to, ScaleAnimation.RELATIVE_TO_SELF, .5f, ScaleAnimation.RELATIVE_TO_SELF, .5f);
        scale.setDuration(1500);
        scale.setInterpolator(new LinearInterpolator());
        scale.setRepeatCount(ScaleAnimation.INFINITE);
        scale.setRepeatMode(ScaleAnimation.REVERSE);
        view.startAnimation(scale);
    }
}
