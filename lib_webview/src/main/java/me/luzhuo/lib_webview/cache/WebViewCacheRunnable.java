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
package me.luzhuo.lib_webview.cache;

import java.io.File;

import me.luzhuo.lib_okhttp.OKHttpManager;

/**
 * Description: 缓存文件下载线程
 * @Author: Luzhuo
 * @Creation Date: 2021/11/27 2:24
 * @Copyright: Copyright 2021 Luzhuo. All rights reserved.
 **/
public class WebViewCacheRunnable implements Runnable {
    private File cachePath;
    private String fileKey;
    private String url;

    public WebViewCacheRunnable(File cachePath, String fileKey, String url) {
        if (cachePath.exists() && cachePath.isFile()) return;
        this.cachePath = cachePath;
        this.fileKey = fileKey;
        this.url = url;
    }

    @Override
    public void run() {
        try {
            new OKHttpManager().downloadFile(url, cachePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
