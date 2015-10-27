/* Copyright 2015 Dmitry Brant
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

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RestBaseClient {
    public static final String TAG = "RestBaseServer";
    private static final String BASE_URL = "https://rest.wikimedia.org/en.wikipedia.org/v1/page/mobile-text/";

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
                return createPageContent(pageName, json);
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

    private static String createPageContent(String pageName, JSONObject json) throws JSONException {
        String contentStr = "";
        if (!json.has("sections")) {
            return "Error: content not found.";
        }
        String displayTitle = pageName;
        if (json.has("displaytitle")) {
            displayTitle = json.getString("displaytitle");
        }
        JSONArray sections = json.getJSONArray("sections");
        JSONObject firstSection = (JSONObject) sections.get(0);
        if (!firstSection.has("items")) {
            return "Error: content not found.";
        }

        contentStr += "<h2>" + displayTitle + "</h2>";

        JSONArray items = firstSection.getJSONArray("items");
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = (JSONObject) items.get(i);
            if (item.getString("type").equals("p")) {
                contentStr += "<p>";
                contentStr += item.getString("text");
                contentStr += "</p>";
            }
        }
        return contentStr;
    }

}
