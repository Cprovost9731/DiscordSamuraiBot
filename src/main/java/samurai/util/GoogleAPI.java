/*    Copyright 2017 Ton Ly
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package samurai.util;

import com.typesafe.config.ConfigFactory;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author TonTL
 * @version 4/26/2017
 */
public class GoogleAPI {

    private static final String GOOGLE_URL;
    private static final String KEY;
    private static final String ENGINE;
    private static int calls;

    static {
        KEY = ConfigFactory.load().getString("api.google");
        ENGINE = ConfigFactory.load().getString("api.google_engine");
        GOOGLE_URL = "https://www.googleapis.com/customsearch/v1?cx=%s&key=%s&num=%d&q=%s";
    }


    public static JSONObject retrieveSearchResults(String query, int count) {
        query = query.replace(" ", "%20");
        calls++;
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(new URL(String.format(GOOGLE_URL, ENGINE, KEY, count, query)).openStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            return new JSONObject(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
