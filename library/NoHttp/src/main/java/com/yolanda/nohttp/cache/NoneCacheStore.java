/*
 * Copyright 2015 Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yolanda.nohttp.cache;

import android.content.Context;

import com.yolanda.nohttp.tools.CacheStore;

public class NoneCacheStore implements CacheStore<CacheEntity> {

    public NoneCacheStore(Context context) {
    }

    public CacheStore<CacheEntity> setEnable(boolean enable) {
        return this;
    }

    @Override
    public CacheEntity get(String key) {
        return null;
    }

    @Override
    public CacheEntity replace(String key, CacheEntity cacheEntity) {
        return cacheEntity;
    }

    @Override
    public boolean remove(String key) {
        return false;
    }

    @Override
    public boolean clear() {
        return false;
    }
}
