package android.support.v7.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.TintableBackgroundView;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.RelativeLayout;

@SuppressWarnings("RestrictedApi")
@SuppressLint("AppCompatCustomView")
public class AppCompatRelativeLayout extends RelativeLayout implements TintableBackgroundView {
	private final AppCompatBackgroundHelper mBackgroundTintHelper;
	
	public AppCompatRelativeLayout(Context context) {
		this(context, null);
	}
	
	public AppCompatRelativeLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public AppCompatRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(TintContextWrapper.wrap(context), attrs, defStyleAttr);
		mBackgroundTintHelper = new AppCompatBackgroundHelper(this);
		mBackgroundTintHelper.loadFromAttributes(attrs, defStyleAttr);
	}
	
	@Override
	public void setBackgroundResource(@DrawableRes int resId) {
		super.setBackgroundResource(resId);
		if (mBackgroundTintHelper != null) {
			mBackgroundTintHelper.onSetBackgroundResource(resId);
		}
	}
	
	@Override
	public void setBackgroundDrawable(Drawable background) {
		super.setBackgroundDrawable(background);
		if (mBackgroundTintHelper != null) {
			mBackgroundTintHelper.onSetBackgroundDrawable(background);
		}
	}
	
	@Override
	public void setSupportBackgroundTintList(@Nullable ColorStateList tint) {
		if (mBackgroundTintHelper != null) {
			mBackgroundTintHelper.setSupportBackgroundTintList(tint);
		}
	}
	
	@Override
	@Nullable
	public ColorStateList getSupportBackgroundTintList() {
		return mBackgroundTintHelper != null ? mBackgroundTintHelper.getSupportBackgroundTintList() : null;
	}
	
	@Override
	public void setSupportBackgroundTintMode(@Nullable PorterDuff.Mode tintMode) {
		if (mBackgroundTintHelper != null) {
			mBackgroundTintHelper.setSupportBackgroundTintMode(tintMode);
		}
	}
	
	@Override
	@Nullable
	public PorterDuff.Mode getSupportBackgroundTintMode() {
		return mBackgroundTintHelper != null ? mBackgroundTintHelper.getSupportBackgroundTintMode() : null;
	}
	
	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		if (mBackgroundTintHelper != null) {
			mBackgroundTintHelper.applySupportBackgroundTint();
		}
	}
	
	@Override
	public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
		super.onInitializeAccessibilityEvent(event);
		event.setClassName(RelativeLayout.class.getName());
	}
	
	@RequiresApi(14)
	@TargetApi(14)
	@Override
	public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(RelativeLayout.class.getName());
	}
}
