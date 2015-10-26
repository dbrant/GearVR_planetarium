/* Copyright 2015 Copyright 2015 Dmitry Brant
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


package org.gearvrf.planetarium;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.gearvrf.GVRActivity;

public class MainActivity extends GVRActivity {

    private PlanetariumViewManager viewManager;
    private long lastDownTime;

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        createWebView();
        viewManager = new PlanetariumViewManager(this);
        setScript(viewManager, "gvr_note4.xml");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            lastDownTime = event.getDownTime();
        }

        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            if (event.getEventTime() - lastDownTime < 200) {
                viewManager.onTap();
            }
        }
        return true;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return viewManager.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
    }

    public WebView getWebView() {
        return mWebView;
    }

    public void loadUrl(final String url) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(url);
            }
        });
    }

    private void createWebView() {
        mWebView = new WebView(this);
        mWebView.setInitialScale(300);
        mWebView.measure(768, 1280);
        mWebView.layout(0, 0, 768, 1280);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }

}
