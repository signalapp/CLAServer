/**
 * Copyright (C) 2015 Open WhisperSystems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.whispersystems.claserver;

import com.google.common.io.CharStreams;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.logging.Logger;

public class UrlFetcher<T> {
  private static Logger logger = Logger.getLogger(UrlFetcher.class.getName());
  private static ObjectMapper mapper = new ObjectMapper();

  public static String post(String urlString, Object data, Map<String, String> headers) throws IOException {
    URL url = new URL(urlString);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setDoOutput(true);
    connection.addRequestProperty("Content-Type", "application/json");
    connection.setRequestMethod("POST");
    for (Map.Entry<String, String> header : headers.entrySet()) {
      connection.setRequestProperty(header.getKey(), header.getValue());
    }
    OutputStream outputStream = connection.getOutputStream();
    mapper.writeValue(outputStream, data);
    outputStream.close();
    logger.info(String.format("Status update response: %s", connection.getResponseCode()));
    return CharStreams.readLines(new InputStreamReader(connection.getInputStream())).get(0);
  }

  public T get(String urlString, Map<String, String> headers, Class<T> clazz) throws IOException {
    URL url = new URL(urlString);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setDoOutput(true);
    connection.addRequestProperty("Content-Type", "application/json");
    for (Map.Entry<String, String> header : headers.entrySet()) {
      connection.setRequestProperty(header.getKey(), header.getValue());
    }
    logger.info(String.format("Status update response: %s", connection.getResponseCode()));
    return mapper.readValue(mapper.getJsonFactory().createJsonParser(connection.getInputStream()), clazz);
  }
}
