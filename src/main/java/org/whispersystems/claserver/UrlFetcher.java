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

/**
 * Created by tina on 1/26/15.
 */
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
    logger.fine(String.format("Status update response: %s", connection.getResponseCode()));
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
    logger.fine(String.format("Status update response: %s", connection.getResponseCode()));
    return mapper.readValue(mapper.getJsonFactory().createJsonParser(connection.getInputStream()), clazz);
  }
}
