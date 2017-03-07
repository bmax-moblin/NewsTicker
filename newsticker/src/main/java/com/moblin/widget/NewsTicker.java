package com.moblin.widget;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * News Ticker is a widget, that shows scrolling text running
 * from right to left across the screen.
 */
@SuppressWarnings("unused")
public class NewsTicker extends FrameLayout implements View.OnClickListener {
    private static final TimeInterpolator LINEAR = new LinearInterpolator();
    private static final long TRAVEL_SPEED = 10L;
    private List<String> mHeadlines = Collections.emptyList();
    private Set<View> mAnimatingViews = new HashSet<>();
    private HeadlineClickListener mListener;
    private Paint mTextPaint = new Paint();
    private float mTextSize, mSpaceWidth;
    private int mTextColor, mIndex;

    /**
     * Constructor that is called when inflating a view from XML.
     * This is called when a view is being constructed from an XML file,
     * supplying attributes that were specified in the XML file.
     */
    public NewsTicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, getResources());
    }

    /**
     * Sets the headlines.
     * @param headlines - list to strings to display
     */
    public void setHeadlines(List<String> headlines) {
        mHeadlines = (headlines != null) ? headlines : Collections.<String>emptyList();
    }

    /**
     * Sets the listener.
     * @param listener - headline click listener
     */
    public void setListener(HeadlineClickListener listener) {
        mListener = listener;
    }

    /**
     * Sets the text color.
     * @param color - text color (packed int: AARRGGBB)
     */
    public void setTextColor(@ColorInt int color) {
        mTextColor = color;
    }

    /**
     * Sets the text size.
     * @param size - text size in scaled pixels (SP)
     */
    public void setTextSize(int size) {
        mTextSize = (float) size;
    }

    /**
     * Starts scrolling the headlines.
     * Has to be called after the headlines have been set.
     */
    public void run() {
        if (mHeadlines.isEmpty()) {
            throw new IllegalStateException("Headlines not set.");
        }
        launchNext();
    }

    /** View Group methods */

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof TextView) {
                TextView tv = (TextView) child;
                // By default text-view's width is limited to its parent.
                // But we want to pass our children the width of their text.
                String text = tv.getText().toString();
                int w = (int) mTextPaint.measureText(text, 0, text.length());
                int h = tv.getMeasuredHeight();
                tv.layout(0, 0, w, h);
            }
        }
    }

    /** On Click Listener */

    @Override
    public void onClick(View v) {
        Object tag = v.getTag();
        if (tag instanceof Integer && mListener != null) {
            mListener.onClick((Integer)tag);
        }
    }

    /** Private methods */

    private void init(AttributeSet attrs, Resources res) {
        // Get dimensions from resources.
        mTextSize = res.getDimension(R.dimen.news_ticker_text_size);
        mSpaceWidth = res.getDimension(R.dimen.news_ticker_space_width);

        // Setup the paint (for text measurements)
        mTextPaint.setTypeface(Typeface.DEFAULT);
        mTextPaint.setTextSize(mTextSize);

        // Read the text color from XML.
        TypedArray a = getContext().getTheme()
                .obtainStyledAttributes(attrs, R.styleable.NewsTicker, 0, 0);
        mTextColor = a.getColor(R.styleable.NewsTicker_textColor,
                getColorResource(R.color.news_ticker_text));
        a.recycle();

        // Set default background if needed.
        if (getBackground() == null) {
            setBackgroundColor(getColorResource(R.color.news_ticker_bg));
        }
    }

    @ColorInt
    private int getColorResource(@ColorRes int colorResource) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getResources().getColor(colorResource, null);
        } else {
            //noinspection deprecation
            return getResources().getColor(colorResource);
        }
    }

    private void launchNext() {
        int index = nextIndex();
        String text = mHeadlines.get(index);
        TextView tv = makeTextView(text, index);
        tv.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top,
                                       int right, int bottom,
                                       int oldLeft, int oldTop,
                                       int oldRight, int oldBottom) {
                // View's dimensions are available only after the layout pass.
                // Now is the time to start animating it.
                animateEnter((TextView)v);
            }
        });
        addView(tv);
    }

    private TextView makeTextView(String headline, int index) {
        TextView tv = new TextView(getContext());
        tv.setGravity(Gravity.CENTER_VERTICAL);
        tv.setTextColor(mTextColor);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        tv.setText(headline);
        int padding = (int)mTextSize / 2;
        tv.setPadding(0, padding, 0, padding);
        tv.setSingleLine(true);
        tv.setTag(index);
        tv.setOnClickListener(this);
        return tv;
    }

    private int nextIndex() {
        mIndex = (mIndex + 1) % mHeadlines.size();
        return mIndex;
    }

    private void animateEnter(final TextView tv) {
        // Since this methods is called after each layout, which happens
        // more than once, check that the current view is not animating
        // already.
        if (mAnimatingViews.contains(tv)) {
            return;
        }

        // Place the view off screen.
        tv.setTranslationX(getWidth());

        // Calculate the distance, that this headline needs to travel,
        // until the next headline should appear.
        int distance = tv.getWidth() + (int)mSpaceWidth;
        tv.animate()
                .translationXBy(-distance)
                .setDuration(distance * TRAVEL_SPEED)
                .setInterpolator(LINEAR)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        // To prevent starting the same animation more than once,
                        // add this view to the set.
                        if (mAnimatingViews != null) {
                            mAnimatingViews.add(tv);
                        }
                    }
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        launchNext();
                        animateExit(tv);
                    }
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        // Remove this view from the set of animating views.
                        if (mAnimatingViews != null) {
                            mAnimatingViews.remove(tv);
                        }
                    }
                    @Override
                    public void onAnimationRepeat(Animator animation) {
                        // Irrelevant
                    }
                })
                .start();
    }

    private void animateExit(final TextView tv) {
        // Calculate the distance, that this headline needs to travel,
        // until it is completely off screen.
        int distance = getWidth() - (int)mSpaceWidth;
        tv.animate()
                .translationXBy(-distance)
                .setDuration(distance * TRAVEL_SPEED)
                .setInterpolator(LINEAR)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        // No action
                    }
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // This view is not needed anymore.
                        removeView(tv);
                        // Remove this view from the set of animating views.
                        if (mAnimatingViews != null) {
                            mAnimatingViews.remove(tv);
                        }
                    }
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        // Remove this view from the set of animating views.
                        if (mAnimatingViews != null) {
                            mAnimatingViews.remove(tv);
                        }
                    }
                    @Override
                    public void onAnimationRepeat(Animator animation) {
                        // Irrelevant
                    }
                })
                .start();
    }

    /**
     * HeadlineClickListener is notified when a headline is clicked.
     */
    @SuppressWarnings("WeakerAccess")
    public interface HeadlineClickListener {
        void onClick(int index);
    }
}
