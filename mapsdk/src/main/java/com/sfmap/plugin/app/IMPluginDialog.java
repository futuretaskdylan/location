package com.sfmap.plugin.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.sfmap.plugin.IMPluginManager;
import com.sfmap.plugin.core.ctx.IMPlugin;
import com.sfmap.plugin.core.ctx.Module;
import com.sfmap.plugin.core.install.IMInstaller;
import com.sfmap.plugin.task.TaskManager;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * 可在插件中打开的Dialog
 */
public class IMPluginDialog extends Dialog {

	private final Object lock = new Object();
	private volatile boolean allowInvokeShow = true;
	private final Activity activity;
	private final DialogRootView rootView;

	public IMPluginDialog(Activity activity) {
		super(new PluginContextWrapper(activity));
		this.activity = activity;
		rootView = new DialogRootView(this);
	}

	public IMPluginDialog(Activity activity, int theme) {
		super(new PluginContextWrapper(activity), theme);
		this.activity = activity;
		rootView = new DialogRootView(this);
	}

	public IMPluginDialog(Activity activity, boolean cancelable, OnCancelListener cancelListener) {
		super(new PluginContextWrapper(activity), cancelable, cancelListener);
		this.activity = activity;
		rootView = new DialogRootView(this);
	}

	@Override
	public void setContentView(int layoutResID) {
		rootView.removeAllViews();
		this.getLayoutInflater().inflate(layoutResID, rootView, true);
		super.setContentView(rootView);
	}

	@Override
	public void setContentView(View view) {
		rootView.removeAllViews();
		rootView.addView(view, new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
		super.setContentView(rootView);
	}

	@Override
	public void setContentView(View view, ViewGroup.LayoutParams params) {
		rootView.removeAllViews();
		rootView.addView(view, params);
		super.setContentView(rootView);
	}

	protected void onLayout(boolean changed, int l, int t, int r, int b) {

	}

	/**
	 * 如果在主线程，直接显示。
	 * 如果不在主线程，则post到主线程显示。
	 */
	@Override
	public void show() {
		TaskManager.post(new Runnable() {
			@Override
			public void run() {
				synchronized (lock) {
					if (allowInvokeShow) {
						if (activity.isFinishing()) {
							return;
						}
						try {
							IMPluginDialog.super.show();
							allowInvokeShow = false;
						} catch (Throwable ignored) {
						}
					} else {
						allowInvokeShow = true;
					}
				}
			}
		});
	}

	@Override
	public void dismiss() {
		TaskManager.post(new Runnable() {
			@Override
			public void run() {
				synchronized (lock) {
					if (IMPluginDialog.this.isShowing()) {
						try {
							IMPluginDialog.super.dismiss();
							allowInvokeShow = true;
						} catch (Throwable ignored) {
						}
					} else {
						allowInvokeShow = false;
					}
				}
			}
		});
	}

	private static class DialogRootView extends LinearLayout {

		private IMPluginDialog pluginDialog;

		public DialogRootView(IMPluginDialog pluginDialog) {
			super(pluginDialog.getContext());
			this.pluginDialog = pluginDialog;
			this.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
		}

		@Override
		protected void onLayout(boolean changed, int l, int t, int r, int b) {
			super.onLayout(changed, l, t, r, b);
			pluginDialog.onLayout(changed, l, t, r, b);
		}
	}

	private static class PluginContextWrapper extends ContextWrapper {

		private Context closetContext;
		private Resources resource;
		private ClassLoader classLoader;
		private AssetManager assets;

		public PluginContextWrapper(Context base) {
			super(base);
			closetContext = getClosestContext();
			resource = closetContext.getResources();
			classLoader = closetContext.getClassLoader();
			assets = closetContext.getAssets();
		}

		@Override
		public Context getApplicationContext() {
			return IMPluginManager.getApplication();
		}

		@Override
		public AssetManager getAssets() {
			return assets;
		}

		@Override
		public ClassLoader getClassLoader() {
			return classLoader;
		}

		@Override
		public Resources getResources() {
			return resource;
		}

		@Override
		public Context getBaseContext() {
			return closetContext instanceof ContextWrapper ?
				((ContextWrapper) closetContext).getBaseContext() : closetContext;
		}

		/**
		 * 获取距离最近的Context
		 *
		 * @return
		 */
		private Context getClosestContext() {
			ClassLoader appClassLoader = IMPluginManager.getApplication().getClassLoader();
			if (closetContext == null) {
				StackTraceElement[] stacks = new Throwable().getStackTrace();// 平均耗时0.22毫秒
				// 0: PluginContextWrapper#getClosestContext
				// 1: PluginContextWrapper#<init>
				// 2: PluginDialog#<init>
				for (int i = 3; i < stacks.length; i++) {
					StackTraceElement element = stacks[i];
					String className = element.getClassName();
					try {
						// 找到继承者
						Class<?> cls = IMInstaller.getHost().loadClass(className);
						if (cls != null) {
							if (appClassLoader == cls.getClassLoader()) {
								closetContext = IMInstaller.getHost().getContext();
								break;
							}
							IMPlugin plugin = IMPlugin.getPlugin(cls);
							if (plugin != null && (plugin instanceof Module)) {
								closetContext = plugin.getContext();
								break;
							}
						}
					} catch (Throwable ignored) {
					}
				}
				if (closetContext == null) {
					closetContext = super.getBaseContext();
				}
			}
			return closetContext;
		}
	}
}