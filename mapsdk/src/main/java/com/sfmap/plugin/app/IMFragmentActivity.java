package com.sfmap.plugin.app;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import com.sfmap.plugin.core.ctx.ContextProxy;
import com.sfmap.plugin.core.ctx.IMPlugin;
import com.sfmap.plugin.core.ctx.Module;

/**
 *
 *
 */
public class IMFragmentActivity extends FragmentActivity {

	private ContextProxy contextProxy = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		IMPageHelper.initTitleBar(this, contextProxy);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void attachBaseContext(Context newBase) {
		this.contextProxy = new ContextProxy(newBase, this);
		super.attachBaseContext(this.contextProxy);
		IMPageHelper.setLastActivity(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		IMPageHelper.setLastActivity(this);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public void applyOverrideConfiguration(Configuration overrideConfiguration) {
		this.contextProxy.applyOverrideConfiguration(overrideConfiguration);
		super.applyOverrideConfiguration(overrideConfiguration);
	}

	private boolean firstSetTheme = true;

	@Override
	public void setTheme(int resid) {
		if (firstSetTheme) {
			firstSetTheme = false;
			if (IMPlugin.getPlugin(this) instanceof Module) {
				resid = IMPageHelper.getThemeResId(this, contextProxy);
			}
		}
		if (resid > 0) {
			super.setTheme(resid);
		}
	}

	@Override
	public void startActivity(final Intent intent) {
		IMPageHelper.startActivity(intent, new Runnable() {
			@Override
			public void run() {
				IMFragmentActivity.super.startActivity(intent);
			}
		});
	}

	@Override
	public void startActivityForResult(final Intent intent, final int requestCode) {
		IMPageHelper.startActivity(intent, new Runnable() {
			@Override
			public void run() {
				IMFragmentActivity.super.startActivityForResult(intent, requestCode);
			}
		});
	}

	@Override
	public void startActivityFromFragment(final Fragment fragment, final Intent intent, final int requestCode) {
		IMPageHelper.startActivity(intent, new Runnable() {
			@Override
			public void run() {
				IMFragmentActivity.super.startActivityFromFragment(fragment, intent, requestCode);
			}
		});
	}

}
