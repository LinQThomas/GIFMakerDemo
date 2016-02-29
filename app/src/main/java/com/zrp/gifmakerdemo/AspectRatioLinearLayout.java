package com.zrp.gifmakerdemo;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * 宽高比，自动锁定宽度和高度的比例。
 * <p>
 * <AspectRatioLinearLayout
 * xmlns:app="http://schemas.android.com/apk/res-auto"
 * android:layout_width="match_parent"
 * android:layout_height="wrap_content"
 * app:heightWeight="176"
 * app:widthWeight="144">
 * <ImageView></ImageView>
 * </AspectRatioLinearLayout>
 * </p>
 */
public class AspectRatioLinearLayout extends LinearLayout {

    private boolean lockWidth = true;
    private float widthWeight = 1;
    private float heightWeight = 1;

    public AspectRatioLinearLayout(Context context) {
        super(context);
    }

    public AspectRatioLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        initData(context, attrs, 0);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public AspectRatioLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initData(context, attrs, defStyle);
    }

    public void initData(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AspectRatio, defStyle, 0);

        lockWidth = a.getBoolean(R.styleable.AspectRatio_lockWidth, true);
        widthWeight = a.getInteger(R.styleable.AspectRatio_widthWeight, 1);
        heightWeight = a.getInteger(R.styleable.AspectRatio_heightWeight, 1);

        widthWeight = widthWeight < 1 ? 1 : widthWeight;
        heightWeight = heightWeight < 1 ? 1 : heightWeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));

        if (lockWidth) {
            int lockSize = getMeasuredWidth();
            heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) (lockSize * heightWeight / widthWeight), MeasureSpec.EXACTLY);
        } else {
            int lockSize = getMeasuredHeight();
            widthMeasureSpec = MeasureSpec.makeMeasureSpec((int) (lockSize * widthWeight / heightWeight), MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
