package com.kycq.library.support;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

public class MaterialProgressDrawable extends Drawable implements Animatable {
	private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
	private static final Interpolator MATERIAL_INTERPOLATOR = new FastOutSlowInInterpolator();
	
	private static final float DURATION_OFFSET = 0.5f;
	private static final int ANIMATION_DURATION = 1332;
	
	private static final int MAX_CANVAS_DEGREES = 1080;
	private static final int DURATION_CANVAS_DEGREES = 216;
	
	private static final int MAX_ARC_DEGREES = 360;
	private static final int DURATION_ARC_DEGREES = 90;
	
	private static final float COLOR_START_DELAY_OFFSET = 0.75f;
	
	private View mViewParent;
	private int mSize;
	
	private int[] mColors = new int[]{
			Color.WHITE
	};
	private int mColorIndex = 0;
	private int mCurrentColor = mColors[mColorIndex];
	
	private int mInitCanvasDegrees;
	private int mCurrentCanvasDegrees;
	
	private Paint mArcPaint = new Paint();
	private float mStrokeWidth;
	private float mStrokeInset;
	private int mPadding;
	private RectF mTempBounds = new RectF();
	
	private int mMaxArcAngle = 288;
	private int mMinArcAngle = 10;
	
	private Animation mAnimation;
	
	private int mInitArcHeaderAngle;
	private int mCurrentArcHeaderAngle;
	private int mInitArcFooterAngle;
	private int mCurrentArcFooterAngle;
	private int mInitArcDegrees;
	private int mCurrentArcDegrees;
	
	private boolean mFinish = false;
	private float mFinishArcHeaderRatio;
	private int mFinishInitArcHeaderAngle;
	private float mFinishArcFooterRatio;
	private int mFinishInitArcFooterAngle;
	private int mFinishInitArcDegrees;
	private int mFinishInitCanvasDegrees;
	
	public MaterialProgressDrawable(View viewParent) {
		mViewParent = viewParent;
		
		initArc();
		initAnimation();
	}
	
	private void initArc() {
		Resources resources = mViewParent.getContext().getResources();
		float density = resources.getDisplayMetrics().density;
		
		mSize = (int) (56 * density);
		
		mArcPaint.setAntiAlias(true);
		mArcPaint.setStrokeCap(Paint.Cap.SQUARE);
		mArcPaint.setStyle(Paint.Style.STROKE);
		
		mStrokeWidth = 5f * density;
		mArcPaint.setStrokeWidth(mStrokeWidth);
		setStrokeInset();
		
		mInitArcHeaderAngle = mMinArcAngle;
		mCurrentArcHeaderAngle = mMinArcAngle;
	}
	
	private void setStrokeInset() {
		mStrokeInset = (float) Math.ceil(mStrokeWidth / 2.0f) + mPadding;
	}
	
	private void initAnimation() {
		Animation animation = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				if (mFinish) {
					int offsetAngle = mMaxArcAngle - mMinArcAngle;
					if (mFinishArcHeaderRatio != 0) {
						mCurrentArcHeaderAngle = (int) (mFinishInitArcHeaderAngle + mFinishArcHeaderRatio * offsetAngle * MATERIAL_INTERPOLATOR.getInterpolation(interpolatedTime));
						
						mCurrentArcDegrees = (int) (mFinishInitArcDegrees + (DURATION_ARC_DEGREES / 2 - mFinishInitArcDegrees + mInitArcDegrees) * interpolatedTime);
						mCurrentCanvasDegrees = (int) (mFinishInitCanvasDegrees + (DURATION_CANVAS_DEGREES / 2 - mFinishInitCanvasDegrees + mInitCanvasDegrees) * interpolatedTime);
					}
					if (mFinishArcFooterRatio != 0) {
						mCurrentArcFooterAngle = (int) (mFinishInitArcFooterAngle + mFinishArcFooterRatio * offsetAngle * MATERIAL_INTERPOLATOR.getInterpolation(interpolatedTime));
						
						mCurrentArcDegrees = (int) (mFinishInitArcDegrees + (DURATION_ARC_DEGREES - mFinishInitArcDegrees + mInitArcDegrees) * interpolatedTime);
						mCurrentCanvasDegrees = (int) (mFinishInitCanvasDegrees + (DURATION_CANVAS_DEGREES - mFinishInitCanvasDegrees + mInitCanvasDegrees) * interpolatedTime);
					}
				} else {
					int offsetAngle = mMaxArcAngle - mMinArcAngle;
					int initArcHeaderAngle = mInitArcHeaderAngle;
					int initArcFooterAngle = mInitArcFooterAngle;
					
					if (interpolatedTime <= DURATION_OFFSET) {
						float scaledTime = interpolatedTime / DURATION_OFFSET;
						mCurrentArcHeaderAngle = (int) (initArcHeaderAngle + offsetAngle * MATERIAL_INTERPOLATOR.getInterpolation(scaledTime));
					} else if (interpolatedTime > DURATION_OFFSET) {
						mCurrentArcHeaderAngle = initArcHeaderAngle + offsetAngle;
					}
					
					if (interpolatedTime > DURATION_OFFSET) {
						float scaledTime = (interpolatedTime - DURATION_OFFSET) / (1.0f - DURATION_OFFSET);
						mCurrentArcFooterAngle = (int) (initArcFooterAngle + offsetAngle * MATERIAL_INTERPOLATOR.getInterpolation(scaledTime));
					}
					
					mCurrentArcDegrees = (int) (mInitArcDegrees + DURATION_ARC_DEGREES * interpolatedTime);
					mCurrentCanvasDegrees = (int) (mInitCanvasDegrees + DURATION_CANVAS_DEGREES * interpolatedTime);
				}
				
				updateColor(interpolatedTime);
				
				invalidateSelf();
			}
		};
		animation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				// NO-OP
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				// NO-OP
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				setupAnimation();
				setColorIndex(getNextColorIndex());
			}
		});
		
		animation.setRepeatCount(Animation.INFINITE);
		animation.setRepeatMode(Animation.RESTART);
		animation.setInterpolator(LINEAR_INTERPOLATOR);
		
		mAnimation = animation;
	}
	
	public void setSize(int size) {
		Resources resources = mViewParent.getContext().getResources();
		float density = resources.getDisplayMetrics().density;
		
		mSize = (int) (size * density);
		invalidateSelf();
	}
	
	public void setStrokeWidth(float strokeWidth) {
		Resources resources = mViewParent.getContext().getResources();
		float density = resources.getDisplayMetrics().density;
		
		mStrokeWidth = strokeWidth * density;
		mArcPaint.setStrokeWidth(mStrokeWidth);
		setStrokeInset();
		invalidateSelf();
	}
	
	public void setPadding(int padding) {
		Resources resources = mViewParent.getContext().getResources();
		float density = resources.getDisplayMetrics().density;
		
		mPadding = (int) (padding * density);
		setStrokeInset();
		invalidateSelf();
	}
	
	public void setColors(@ColorInt int[] colors) {
		mColors = colors;
		setColorIndex(0);
	}
	
	@Override
	public void start() {
		if (isRunning()) {
			return;
		}
		
		mAnimation.reset();
		setupAnimation();
		mViewParent.startAnimation(mAnimation);
	}
	
	@Override
	public void stop() {
		mViewParent.clearAnimation();
	}
	
	@Override
	public boolean isRunning() {
		return mViewParent.getAnimation() != null && mAnimation.hasStarted() && !mAnimation.hasEnded();
	}
	
	private void setupAnimation() {
		int offsetAngle = mMaxArcAngle - mMinArcAngle;
		if (mCurrentArcHeaderAngle - mInitArcHeaderAngle != 0
				&& mCurrentArcHeaderAngle - mInitArcHeaderAngle != offsetAngle) {
			mFinish = true;
			
			mFinishArcHeaderRatio = 1f * (mInitArcHeaderAngle + offsetAngle - mCurrentArcHeaderAngle) / offsetAngle;
			mFinishInitArcHeaderAngle = mCurrentArcHeaderAngle;
			
			mFinishArcFooterRatio = 0;
			
			mFinishInitArcDegrees = mCurrentArcDegrees;
			mFinishInitCanvasDegrees = mCurrentCanvasDegrees;
			
			mAnimation.setDuration((long) (ANIMATION_DURATION / 2 * mFinishArcHeaderRatio));
		} else if (mCurrentArcHeaderAngle - mCurrentArcFooterAngle != mMinArcAngle) {
			mFinish = true;
			
			mFinishArcHeaderRatio = 0;
			
			mFinishArcFooterRatio = 1f * (mCurrentArcHeaderAngle - mCurrentArcFooterAngle - mMinArcAngle) / offsetAngle;
			mFinishInitArcFooterAngle = mCurrentArcFooterAngle;
			
			mFinishInitArcDegrees = mCurrentArcDegrees;
			mFinishInitCanvasDegrees = mCurrentCanvasDegrees;
			
			mAnimation.setDuration((long) (ANIMATION_DURATION / 2 * mFinishArcFooterRatio));
		} else {
			mFinish = false;
			
			mFinishArcHeaderRatio = 0;
			mFinishInitArcHeaderAngle = 0;
			
			mFinishArcFooterRatio = 0;
			mFinishInitArcFooterAngle = 0;
			
			mFinishInitArcDegrees = 0;
			mFinishInitCanvasDegrees = 0;
			
			mAnimation.setDuration(ANIMATION_DURATION);
			storeOriginals();
		}
	}
	
	private void updateColor(float interpolatedTime) {
		if (interpolatedTime > COLOR_START_DELAY_OFFSET) {
			mCurrentColor = evaluateColorChange(
					(interpolatedTime - COLOR_START_DELAY_OFFSET) / (1.0f - COLOR_START_DELAY_OFFSET),
					mColors[mColorIndex],
					getNextColor()
			);
		}
	}
	
	private int getNextColorIndex() {
		return (mColorIndex + 1) % (mColors.length);
	}
	
	private int getNextColor() {
		return mColors[(mColorIndex + 1) % (mColors.length)];
	}
	
	private void setColorIndex(int colorIndex) {
		mColorIndex = colorIndex;
		mCurrentColor = mColors[colorIndex];
	}
	
	private void storeOriginals() {
		mCurrentCanvasDegrees = (mCurrentCanvasDegrees + MAX_CANVAS_DEGREES) % MAX_CANVAS_DEGREES;
		mInitCanvasDegrees = mCurrentCanvasDegrees;
		
		mCurrentArcDegrees = (mCurrentArcDegrees + MAX_ARC_DEGREES) % MAX_ARC_DEGREES;
		mInitArcDegrees = mCurrentArcDegrees;
		
		mCurrentArcHeaderAngle = (mCurrentArcHeaderAngle + 360) % 360;
		mCurrentArcFooterAngle = (mCurrentArcFooterAngle + 360) % 360;
		if (mCurrentArcHeaderAngle < mCurrentArcFooterAngle) {
			mCurrentArcHeaderAngle += 360;
		}
		mInitArcHeaderAngle = mCurrentArcHeaderAngle;
		mInitArcFooterAngle = mCurrentArcFooterAngle;
	}
	
	@Override
	public void draw(@NonNull Canvas canvas) {
		Rect canvasBounds = getBounds();
		
		RectF arcBounds = mTempBounds;
		
		int saveCount = canvas.save();
		
		canvas.rotate(mCurrentCanvasDegrees, canvasBounds.exactCenterX(), canvasBounds.exactCenterY());
		
		arcBounds.set(canvasBounds);
		arcBounds.inset(mStrokeInset, mStrokeInset);
		
		mArcPaint.setColor(mCurrentColor);
		float starAngle = mCurrentArcHeaderAngle + mCurrentArcDegrees;
		float endAngle = mCurrentArcFooterAngle + mCurrentArcDegrees;
		float sweepAngle = endAngle - starAngle;
		canvas.drawArc(arcBounds, starAngle, sweepAngle, false, mArcPaint);
		
		canvas.restoreToCount(saveCount);
	}
	
	@Override
	public int getIntrinsicWidth() {
		return mSize;
	}
	
	@Override
	public int getIntrinsicHeight() {
		return mSize;
	}
	
	@Override
	public void setAlpha(int alpha) {
		
	}
	
	@Override
	public void setColorFilter(ColorFilter colorFilter) {
		mArcPaint.setColorFilter(colorFilter);
		invalidateSelf();
	}
	
	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}
	
	private int evaluateColorChange(float fraction, int startValue, int endValue) {
		int startA = (startValue >> 24) & 0xff;
		int startR = (startValue >> 16) & 0xff;
		int startG = (startValue >> 8) & 0xff;
		int startB = startValue & 0xff;
		
		int endA = (endValue >> 24) & 0xff;
		int endR = (endValue >> 16) & 0xff;
		int endG = (endValue >> 8) & 0xff;
		int endB = endValue & 0xff;
		
		return ((startA + (int) (fraction * (endA - startA))) << 24)
				| ((startR + (int) (fraction * (endR - startR))) << 16)
				| ((startG + (int) (fraction * (endG - startG))) << 8)
				| ((startB + (int) (fraction * (endB - startB))));
	}
	
}
