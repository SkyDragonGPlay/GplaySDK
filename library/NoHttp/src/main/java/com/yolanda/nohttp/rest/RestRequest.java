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
package com.yolanda.nohttp.rest;

import com.yolanda.nohttp.RequestMethod;

/**
 * <p>
 * The realization method of the parameters.
 * </p>
 * Created in Oct 20, 2015 4:24:27 PM.
 *
 * @param <T> a generics, regulated the analytic results of the Request.It should be with the {@link Response}, {@link OnResponseListener}.
 * @author Yan Zhenjie.
 */
public abstract class RestRequest<T> extends ParseRequest<T> implements Request<T> {

    /**
     * The callback mark.
     */
    private String what;
    /**
     * The request of the listener.
     */
    private OnResponseListener<T> responseListener;

    /**
     * Create a request, RequestMethod is {@link RequestMethod#GET}.
     *
     * @param url request address, like: {@code http://www.yanzhenjie.com}.
     */
    public RestRequest(String url) {
        this(url, RequestMethod.GET);
    }

    /**
     * Create a request
     *
     * @param url           request address, like: {@code http://www.yanzhenjie.com}.
     * @param requestMethod request method, like {@link RequestMethod#GET}, {@link RequestMethod#POST}.
     */
    public RestRequest(String url, RequestMethod requestMethod) {
        super(url, requestMethod);
    }

    @Override
    public void onPreResponse(String what, OnResponseListener<T> responseListener) {
        this.what = what;
        this.responseListener = responseListener;
    }

    @Override
    public String what() {
        return what;
    }

    @Override
    public OnResponseListener<T> responseListener() {
        return responseListener;
    }
}