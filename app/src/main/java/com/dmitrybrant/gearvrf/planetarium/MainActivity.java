/* Copyright 2016-2017 Dmitry Brant
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

package com.dmitrybrant.gearvrf.planetarium;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.gearvrf.GVRActivity;
import org.gearvrf.utility.Log;

public class MainActivity extends GVRActivity implements VRTouchPadGestureDetector.OnTouchPadGestureListener {
    private static final String TAG = Log.tag(MainActivity.class);

    private static final int TAP_INTERVAL = 300;
    private long mLatestTap = 0;

    private PlanetariumMain planetariumMain;
    private WebView mWebView;
    private VRTouchPadGestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        createWebView();
        planetariumMain = new PlanetariumMain(this);
        setMain(planetariumMain);
        mGestureDetector = new VRTouchPadGestureDetector(this);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return planetariumMain.handleKeyEvent(event) || super.onKeyUp(keyCode, event);
    }

    public WebView getWebView() {
        return mWebView;
    }

    public void loadWebPageForObject(final SkyObject obj) {
        loadIntoWebView("Loading...");
        if (obj.type == SkyObject.TYPE_OTHER) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl("file:///android_asset/about.html");
                }
            });
        } else {
            RestBaseClient.getPage(Util.transformObjectName(obj.name), new RestBaseClient.OnGetPageResult() {
                @Override
                public void onSuccess(String pageContents) {
                    loadIntoWebView(pageContents);
                }

                @Override
                public void onError(Throwable e) {
                    loadIntoWebView("Error loading page: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }

    private void loadIntoWebView(final String html) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWebView.loadDataWithBaseURL("https://wikipedia.org", Util.formatAsHtml(html), "text/html", "utf-8", null);
            }
        });
    }

    private void createWebView() {
        mWebView = new WebView(this);
        mWebView.setInitialScale(300);
        mWebView.measure(800, 1280);
        mWebView.layout(0, 0, 800, 1280);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(false);
        mWebView.setBackgroundColor(0xff202020);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onSingleTap(MotionEvent e) {
        if (System.currentTimeMillis() > mLatestTap + TAP_INTERVAL) {
            mLatestTap = System.currentTimeMillis();
            planetariumMain.onTap();
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onSwipe(MotionEvent e, VRTouchPadGestureDetector.SwipeDirection swipeDirection,
                           float velocityX, float velocityY) {
        return false;
    }

    @Override
    public void onScroll(float scrollX, float scrollY) {
        planetariumMain.onScroll(scrollX, scrollY);
        /*
        if (Math.abs(scrollY) > Math.abs(scrollX)) {
            int scrollAmount = (int) scrollY;
            if (mWebView.getScrollY() - scrollY < 0) {
                scrollAmount = mWebView.getScrollY();
            }
            mWebView.scrollBy(0, (int) scrollAmount);
        }
        */
    }

}
