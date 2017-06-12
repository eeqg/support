package com.kycq.library.support;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

public abstract class PermissionUtils {
	/** 权限请求映射 */
	private static final Map<Object, Map<Integer, PermissionUtils>> mPermissionsMap = new WeakHashMap<>();
	
	/** 权限请求对象 */
	protected SoftReference<Object> mRequestReference;
	/** 权限请求代码 */
	protected int mRequestCode;
	/** 权限请求监听器 */
	private OnPermissionListener mOnPermissionListener;
	
	/**
	 * 构造方法
	 */
	private PermissionUtils() {
	}
	
	
	/**
	 * 构建权限请求工具
	 *
	 * @param activity 权限请求对象
	 * @return 权限请求工具
	 */
	public static PermissionUtils build(Activity activity) {
		return build(activity, 0);
	}
	
	/**
	 * 构建权限请求工具
	 *
	 * @param activity    权限请求对象
	 * @param requestCode 权限请求代码
	 * @return 权限请求工具
	 */
	public static PermissionUtils build(Activity activity, int requestCode) {
		return new ActivityPermissionUtils(activity, requestCode);
	}
	
	/**
	 * 构建权限请求工具
	 *
	 * @param fragment 权限请求对象
	 * @return 权限请求工具
	 */
	public static PermissionUtils build(Fragment fragment) {
		return new FragmentV4PermissionUtils(fragment, 0);
	}
	
	/**
	 * 构建权限请求工具
	 *
	 * @param fragment    权限请求对象
	 * @param requestCode 权限请求代码
	 * @return 权限请求工具
	 */
	public static PermissionUtils build(Fragment fragment, int requestCode) {
		return new FragmentV4PermissionUtils(fragment, requestCode);
	}
	
	/**
	 * 构建权限请求工具
	 *
	 * @param fragment 权限请求对象
	 * @return 权限请求工具
	 */
	@TargetApi(Build.VERSION_CODES.M)
	public static PermissionUtils build(android.app.Fragment fragment) {
		return new FragmentPermissionUtils(fragment, 0);
	}
	
	/**
	 * 构建权限请求工具
	 *
	 * @param fragment    权限请求对象
	 * @param requestCode 权限请求代码
	 * @return 权限请求工具
	 */
	@TargetApi(Build.VERSION_CODES.M)
	public static PermissionUtils build(android.app.Fragment fragment, int requestCode) {
		return new FragmentPermissionUtils(fragment, requestCode);
	}
	
	/**
	 * 设置权限请求监听器
	 *
	 * @param listener 权限请求监听器
	 * @return 权限请求工具
	 */
	public PermissionUtils setOnPermissionListener(OnPermissionListener listener) {
		if (listener.mPermissionUtils != null) {
			throw new RuntimeException("OnPermissionListener can't use at different Permissions");
		}
		listener.mPermissionUtils = this;
		
		mOnPermissionListener = listener;
		
		return this;
	}
	
	/**
	 * 请求权限(可能显示请求理由)
	 *
	 * @param permissions 权限列表
	 */
	public void requestPermissions(String... permissions) {
		requestPermissions(true, permissions);
	}
	
	/**
	 * 请求已提示请求理由的权限(不显示请求理由)
	 *
	 * @param permissions 权限列表
	 */
	private void requestRationale(String... permissions) {
		requestPermissions(false, permissions);
	}
	
	/**
	 * 请求权限
	 *
	 * @param isRationale 是否显示请求理由 true显示 false不显示
	 * @param permissions 权限列表
	 */
	private void requestPermissions(boolean isRationale, String... permissions) {
		if (mOnPermissionListener == null) {
			throw new NullPointerException("you must set OnPermissionListener before requestPermissions");
		}
		
		ArrayList<String> deniedPermissions = new ArrayList<>();
		ArrayList<String> rationalePermissions = new ArrayList<>();
		for (String permission : permissions) {
			if (checkSelfPermission(permission)) {
				deniedPermissions.add(permission);
				if (isRationale && shouldShowRequestPermissionRationale(permission)) {
					rationalePermissions.add(permission);
				}
			}
		}
		
		if (deniedPermissions.isEmpty()) {
			mOnPermissionListener.onGranted();
		} else {
			if (rationalePermissions.isEmpty()) {
				Object key = mRequestReference.get();
				if (key == null) {
					return;
				}
				
				Map<Integer, PermissionUtils> map = mPermissionsMap.get(key);
				if (map == null) {
					map = new ArrayMap<>();
					mPermissionsMap.put(key, map);
				}
				map.put(mRequestCode, this);
				requestPermissions(deniedPermissions.toArray(new String[deniedPermissions.size()]), mRequestCode);
			} else {
				mOnPermissionListener.onRationale(rationalePermissions.toArray(new String[rationalePermissions.size()]));
			}
		}
	}
	
	/**
	 * 检查权限是否已授予
	 *
	 * @param permission 权限列表
	 * @return true权限授予 false权限拒绝
	 */
	abstract boolean checkSelfPermission(String permission);
	
	/**
	 * 是否显示权限请求理由
	 *
	 * @param permission 权限列表
	 * @return true显示 false不显示
	 */
	abstract boolean shouldShowRequestPermissionRationale(String permission);
	
	/**
	 * 请求权限
	 *
	 * @param permissions 权限列表
	 * @param requestCode 请求代码
	 */
	abstract void requestPermissions(String[] permissions, int requestCode);
	
	/**
	 * 权限请求结果
	 *
	 * @param activity     权限请求对象
	 * @param requestCode  权限请求代码
	 * @param permissions  权限列表
	 * @param grantResults 请求结果列表
	 * @return true已处理 false未处理
	 */
	public static boolean onRequestPermissionsResult(Activity activity, int requestCode, final String[] permissions, int[] grantResults) {
		return onPermissionsResult(activity, requestCode, permissions, grantResults);
	}
	
	/**
	 * 权限请求结果
	 *
	 * @param fragment     权限请求对象
	 * @param requestCode  权限请求代码
	 * @param permissions  权限列表
	 * @param grantResults 请求结果列表
	 * @return true已处理 false未处理
	 */
	public static boolean onRequestPermissionsResult(Fragment fragment, int requestCode, final String[] permissions, int[] grantResults) {
		return onPermissionsResult(fragment, requestCode, permissions, grantResults);
	}
	
	/**
	 * 权限请求结果
	 *
	 * @param fragment     权限请求对象
	 * @param requestCode  权限请求代码
	 * @param permissions  权限列表
	 * @param grantResults 请求结果列表
	 * @return true已处理 false未处理
	 */
	public static boolean onRequestPermissionsResult(android.app.Fragment fragment, int requestCode, final String[] permissions, int[] grantResults) {
		return onPermissionsResult(fragment, requestCode, permissions, grantResults);
	}
	
	/**
	 * 权限请求结果
	 *
	 * @param object       权限请求对象
	 * @param requestCode  权限请求代码
	 * @param permissions  权限列表
	 * @param grantResults 请求结果列表
	 * @return true已处理 false未处理
	 */
	private static boolean onPermissionsResult(Object object, int requestCode, final String[] permissions, int[] grantResults) {
		Map<Integer, PermissionUtils> map = mPermissionsMap.get(object);
		if (map == null) {
			return false;
		}
		PermissionUtils permissionUtils = map.remove(requestCode);
		if (map.isEmpty()) {
			mPermissionsMap.remove(object);
		}
		if (permissionUtils == null) {
			return false;
		}
		
		int index = 0;
		for (; index < grantResults.length; index++) {
			if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
				break;
			}
		}
		
		if (index == grantResults.length) {
			permissionUtils.mOnPermissionListener.onGranted();
		} else {
			permissionUtils.mOnPermissionListener.onDenied();
		}
		
		return true;
	}
	
	private static class ActivityPermissionUtils extends PermissionUtils {
		
		ActivityPermissionUtils(Activity activity, int requestCode) {
			mRequestReference = new SoftReference<Object>(activity);
			mRequestCode = requestCode;
		}
		
		@Override
		public boolean checkSelfPermission(String permission) {
			Activity activity = (Activity) mRequestReference.get();
			return activity != null && ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED;
		}
		
		@Override
		public boolean shouldShowRequestPermissionRationale(String permission) {
			Activity activity = (Activity) mRequestReference.get();
			return activity != null && ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
		}
		
		@Override
		protected void requestPermissions(String[] permissions, int requestCode) {
			Activity activity = (Activity) mRequestReference.get();
			if (activity != null) {
				ActivityCompat.requestPermissions(activity, permissions, requestCode);
			}
		}
		
	}
	
	private static class FragmentV4PermissionUtils extends PermissionUtils {
		
		FragmentV4PermissionUtils(Fragment fragment, int requestCode) {
			mRequestReference = new SoftReference<Object>(fragment);
			mRequestCode = requestCode;
		}
		
		@Override
		public boolean checkSelfPermission(String permission) {
			Fragment fragment = (Fragment) mRequestReference.get();
			return fragment != null && ContextCompat.checkSelfPermission(fragment.getContext(), permission) == PackageManager.PERMISSION_DENIED;
		}
		
		@Override
		public boolean shouldShowRequestPermissionRationale(String permission) {
			Fragment fragment = (Fragment) mRequestReference.get();
			return fragment != null && fragment.shouldShowRequestPermissionRationale(permission);
		}
		
		@Override
		protected void requestPermissions(String[] permissions, int requestCode) {
			Fragment fragment = (Fragment) mRequestReference.get();
			if (fragment != null) {
				fragment.requestPermissions(permissions, requestCode);
			}
		}
	}
	
	private static class FragmentPermissionUtils extends PermissionUtils {
		
		FragmentPermissionUtils(android.app.Fragment fragment, int requestCode) {
			mRequestReference = new SoftReference<Object>(fragment);
			mRequestCode = requestCode;
		}
		
		@RequiresApi(Build.VERSION_CODES.M)
		@Override
		public boolean checkSelfPermission(String permission) {
			android.app.Fragment fragment = (android.app.Fragment) mRequestReference.get();
			return fragment != null && ContextCompat.checkSelfPermission(fragment.getContext(), permission) == PackageManager.PERMISSION_DENIED;
		}
		
		@RequiresApi(api = Build.VERSION_CODES.M)
		@Override
		public boolean shouldShowRequestPermissionRationale(String permission) {
			android.app.Fragment fragment = (android.app.Fragment) mRequestReference.get();
			return fragment != null && fragment.shouldShowRequestPermissionRationale(permission);
		}
		
		@RequiresApi(Build.VERSION_CODES.M)
		@Override
		protected void requestPermissions(String[] permissions, int requestCode) {
			android.app.Fragment fragment = (android.app.Fragment) mRequestReference.get();
			if (fragment != null) {
				fragment.requestPermissions(permissions, requestCode);
			}
		}
	}
	
	/**
	 * 权限请求监听器
	 */
	public static abstract class OnPermissionListener {
		private PermissionUtils mPermissionUtils;
		
		/**
		 * 设置权限请求工具
		 *
		 * @param permissionUtils 权限请求工具
		 */
		final void setPermissionUtils(PermissionUtils permissionUtils) {
			mPermissionUtils = permissionUtils;
		}
		
		/**
		 * 重新请求权限
		 *
		 * @param permissions 权限列表
		 */
		public void requestPermission(String... permissions) {
			mPermissionUtils.requestRationale(permissions);
		}
		
		/**
		 * 权限授予
		 */
		public abstract void onGranted();
		
		/**
		 * 权限拒绝
		 */
		public abstract void onDenied();
		
		/**
		 * 权限请求理由
		 *
		 * @param permissions 权限列表
		 */
		public abstract void onRationale(String... permissions);
	}
	
}
