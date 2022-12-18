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

import android.webkit.WebResourceResponse;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.luzhuo.lib_core.app.base.CoreBaseApplication;
import me.luzhuo.lib_core.data.hashcode.HashManager;
import me.luzhuo.lib_file.FileManager;

/**
 * Description: WebView 的缓存管理
 * @Author: Luzhuo
 * @Creation Date: 2021/11/27 2:24
 * @Copyright: Copyright 2021 Luzhuo. All rights reserved.
 **/
public class WebViewCacheManager {
    final Map<String/*key*/, String/*url*/> urlCaches = new ConcurrentHashMap<>();
    private final ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    private static WebViewCacheManager instance;
    private File cacheFilePath;

    private WebViewCacheManager() {
        cacheFilePath = new FileManager(CoreBaseApplication.appContext).getCacheDirectory();
    }

    public static WebViewCacheManager getInstance(){
        if (instance == null){
            synchronized (WebViewCacheManager.class){
                if (instance == null) instance = new WebViewCacheManager();
            }
        }
        return instance;
    }

    @Nullable
    public WebResourceResponse shouldInterceptRequest(String url) {
        // 0. 过滤不要缓存的
        WebViewCacheType fileType = getFileExt(url);
        if (fileType == null) return null; // 不需要缓存, 直接返回null

        // 1. 计算文件key
        String fileKey = fileType.fileBitTypeString + HashManager.getInstance().getMD5(url);

        // 2. 检查本地是否存在该文件
        File cacheFile = new File(cacheFilePath.getAbsolutePath() + File.separator + "webViewCache", fileKey + fileType.fileExt);
        if (cacheFile.exists()) {
            try {
                 return new WebResourceResponse(fileType.mimeType, "utf-8", new FileInputStream(cacheFile));
            } catch (Exception e) {
                return null;
            }
        }

        // 3. 如果存在该文件, 则返回该文件路径, 如果不存在, 则添加到 urlCaches 下载
        if (!urlCaches.containsKey(fileKey)) {
            // 把要下载的url记下, 避免重复下载
            urlCaches.put(fileKey, url);
            cachedThreadPool.execute(new WebViewCacheRunnable(cacheFile, fileKey, url));
        }
        return null;
    }

    /**
     * 不需要的缓存文件, 返回null
     */
    @Nullable
    private WebViewCacheType getFileExt(String url) {
        for (WebViewCacheType type : WebViewCacheType.values()) {
            if (url.endsWith(type.fileExt)) return type;
        }
        return null;
    }
}
