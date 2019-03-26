/* Copyright 2015-2019 Dmitry Brant
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

import android.os.AsyncTask;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RestBaseClient {
    public static final String TAG = "RestBaseServer";
    private static final String BASE_URL = "https://en.wikipedia.org/api/rest_v1/page/summary/";

    public interface OnGetPageResult {
        void onSuccess(String pageContents);
        void onError(Throwable e);
    }

    public static void getPage(String name, OnGetPageResult listener) {
        new GetPageTask(listener).execute(name);
    }

    private static class GetPageTask extends AsyncTask<String, Void, String> {
        private final OnGetPageResult listener;

        public GetPageTask(OnGetPageResult listener) {
            this.listener = listener;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                OkHttpClient client = new OkHttpClient();
                String pageName = params[0];
                String url = BASE_URL + pageName;
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();

                JSONObject json = new JSONObject(response.body().string());
                return parsePageContent(json);
            } catch(Exception e) {
                listener.onError(e);
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            if (!TextUtils.isEmpty(result)) {
                listener.onSuccess(result);
            }
        }

        @Override
        protected void onPreExecute() { }

        @Override
        protected void onProgressUpdate(Void... values) { }
    }

    private static String parsePageContent(JSONObject json) throws JSONException {
        if (!json.has("extract_html")) {
            return "Error: content not found.";
        }
        return json.getString("extract_html");
    }

}
