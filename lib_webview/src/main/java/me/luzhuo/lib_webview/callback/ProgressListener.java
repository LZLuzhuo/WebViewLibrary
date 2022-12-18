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
package me.luzhuo.lib_webview.callback;

/**
 * Description: 进度的回调
 * @Author: Luzhuo
 * @Creation Date: 2021/11/27 2:25
 * @Copyright: Copyright 2021 Luzhuo. All rights reserved.
 **/
public interface ProgressListener {
    /**
     * 开始加载
     */
    public void start();

    /**
     * 加载中
     * @param progress [0, 100]
     */
    public void progress(int progress);

    /**
     * 加载结束
     * @param isEnd true:完全结束, false:为progress达到80%的时候调用
     */
    public void end(boolean isEnd);
}
