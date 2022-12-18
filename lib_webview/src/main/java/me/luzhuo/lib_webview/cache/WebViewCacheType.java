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

/**
 * Description: WebView 需要缓存的文件类型
 * @Author: Luzhuo
 * @Creation Date: 2021/11/27 2:23
 * @Copyright: Copyright 2021 Luzhuo. All rights reserved.
 **/
public enum WebViewCacheType {
    ICO(".ico", 0, "0000", "image/x-icon"),
    JS(".js", 1, "0001", "application/javascript"),
    CSS(".css", 2, "0010", "text/css"),
    JPG(".jpg", 3, "0011", "image/jpeg"),
    PNG(".png", 4, "0100", "image/png"),
    SVG(".svg", 5, "0101", "image/svg+xml"),
    MP4(".mp4", 6, "0110", "video/mp4"),
    JPEG(".jpeg", 7, "0111", "image/jpeg");

    /**
     * 文件后缀
     */
    public String fileExt;
    /**
     * 文件的类型标识, int类型
     */
    public int fileBitTypeInt;
    /**
     * 文件的类型标识, string类型
     */
    public String fileBitTypeString;
    /**
     * 文件的mimeType
     */
    public String mimeType;

    private WebViewCacheType(String fileExt, int fileBitTypeInt, String fileBitTypeString, String mimeType) {
        this.fileExt = fileExt;
        this.fileBitTypeInt = fileBitTypeInt;
        this.fileBitTypeString = fileBitTypeString;
        this.mimeType = mimeType;
    }
}
