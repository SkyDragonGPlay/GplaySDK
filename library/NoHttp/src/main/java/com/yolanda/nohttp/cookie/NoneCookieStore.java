package com.yolanda.nohttp.cookie;

import android.content.Context;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * Created in Dec 17, 2015 7:20:52 PM.
 *
 * @author Yan Zhenjie.
 */
public class NoneCookieStore implements CookieStore {

    /**
     * @param context {@link Context}.
     */
    public NoneCookieStore(Context context) {

    }

    public CookieStore setCookieStoreListener(CookieStoreListener cookieStoreListener) {
        return this;
    }

    public CookieStore setEnable(boolean enable) {
        return this;
    }

    @Override
    public void add(URI uri, HttpCookie cookie) {

    }

    @Override
    public List<HttpCookie> get(URI uri) {
        return Collections.emptyList();
    }

    @Override
    public List<HttpCookie> getCookies() {
        return Collections.emptyList();
    }

    @Override
    public List<URI> getURIs() {
        return Collections.emptyList();
    }

    @Override
    public boolean remove(URI uri, HttpCookie httpCookie) {
        return true;
    }

    @Override
    public boolean removeAll() {
        return true;
    }

    public interface CookieStoreListener {

        void onSaveCookie(URI uri, HttpCookie cookie);


        void onRemoveCookie(URI uri, HttpCookie cookie);

    }
}
