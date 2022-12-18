/* Copyright 2016 Luzhuo. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.luzhuo.webviewdemo.lib_base;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;

import me.luzhuo.lib_core.app.base.CoreBaseActivity;
import me.luzhuo.webviewdemo.R;

/**
 * =================================================
 * <p>
 * Author: Luzhuo
 * <p>
 * Version: 1.0
 * <p>
 * Creation Date: 2016/11/6 13:57
 * <p>
 * Description:
 * <p>
 * Revision History:
 * <p>
 * Copyright: Copyright 2016 Luzhuo. All rights reserved.
 * <p>
 * =================================================
 **/
public class BaseActivity extends CoreBaseActivity {
	protected EnterAnimation enterAnimation;

	@SuppressLint("MissingSuperCall")
	@Override
	protected void onCreate(Bundle bundle) {
		this.onCreate(bundle, EnterAnimation.Default);
	}

	protected void onCreate(Bundle bundle, EnterAnimation enterAnimation) {
		super.onCreate(bundle);
		new StatusBarManager().defaultState(this);

		// Activity 进入的动画
		this.enterAnimation = enterAnimation;
		if (enterAnimation != EnterAnimation.Default) overridePendingTransition(enterAnimation.firstEnterActivityAnimation, enterAnimation.firstExitActivityAnimation);
	}

	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (newConfig.fontScale != 1) getResources();
	}

	@Override
	public Resources getResources() {
		final Resources resources = super.getResources();
		if (resources.getConfiguration().fontScale != 1) {
			Configuration newConfig = new Configuration();
			newConfig.setToDefaults();
			resources.updateConfiguration(newConfig, resources.getDisplayMetrics());
		}
		return resources;
	}


	@Override
	public void finish() {
		super.finish();
		// 退出动画
		if (enterAnimation != EnterAnimation.Default) overridePendingTransition(enterAnimation.secondEnterActivityAnimation, enterAnimation.secondExitActivityAnimation);
	}

	public enum EnterAnimation {
		/**
		 * 进入第二页: 从下往上弹出
		 * 退出第二页: 从上往下回收
		 */
		Bottom2Top(R.anim.base_activity_enter_bottom2top, R.anim.base_activity_normal, R.anim.base_activity_normal, R.anim.base_activity_exit_bottom2top),
		/**
		 * 按照系统的默认效果执行
		 */
		Default(0, 0, 0, 0);

		public int firstEnterActivityAnimation;
		public int firstExitActivityAnimation;
		public int secondEnterActivityAnimation;
		public int secondExitActivityAnimation;

		private EnterAnimation(int firstEnterActivityAnimation, int firstExitActivityAnimation, int secondEnterActivityAnimation, int secondExitActivityAnimation) {
			this.firstEnterActivityAnimation = firstEnterActivityAnimation;
			this.firstExitActivityAnimation = firstExitActivityAnimation;
			this.secondEnterActivityAnimation = secondEnterActivityAnimation;
			this.secondExitActivityAnimation = secondExitActivityAnimation;
		}
	}
}
