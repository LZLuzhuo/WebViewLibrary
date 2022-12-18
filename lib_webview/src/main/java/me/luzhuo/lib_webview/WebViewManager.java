/* Copyright 2021 Luzhuo. All rights reserved.
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
package me.luzhuo.lib_webview;

import android.content.Intent;
import android.net.Uri;
import android.webkit.WebStorage;

import me.luzhuo.lib_core.app.base.CoreBaseApplication;

public class WebViewManager {
    /**
     * 使用浏览器下载文件
     */
    public static void downloadByBrowser(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse(url));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            CoreBaseApplication.appContext.startActivity(intent);
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * 清理缓存
     */
    public static void clearCache() {
        WebStorage.getInstance().deleteAllData();
    }
}
