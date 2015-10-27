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
            listener.onSuccess(result);
        }

        @Override
        protected void onPreExecute() { }

        @Override
        protected void onProgressUpdate(Void... values) { }
    }

    private static String createPageContent(String pageName, JSONObject json) throws JSONException {
        String contentStr = "";
        if (!json.has("sections")) {
            return "Error: no sections returned.";
        }
        String displayTitle = pageName;
        if (json.has("displaytitle")) {
            displayTitle = json.getString("displaytitle");
        }
        JSONArray sections = json.getJSONArray("sections");
        JSONObject firstSection = (JSONObject) sections.get(0);
        if (!firstSection.has("items")) {
            return "Error: no items in section.";
        }
        JSONArray items = firstSection.getJSONArray("items");
        JSONObject pobj = null;
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = (JSONObject) items.get(i);
            if (item.getString("type").equals("p")) {
                pobj = item;
                break;
            }
        }
        if (pobj == null) {
            return "Error: no p item found.";
        }

        contentStr += "<h2>" + displayTitle + "</h2>";
        contentStr += pobj.getString("text");
        return contentStr;
    }

}
