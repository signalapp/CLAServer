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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.repackaged.org.apache.commons.codec.binary.Hex;
import com.google.common.io.CharStreams;
import org.codehaus.jackson.map.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;

/**
 * This class validates if the owner of a pull request has signed the CLA and updates the github status accordingly.
 *
 * @author Tina Huang
 */
public class PullRequestValidationServlet extends HttpServlet {
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private Logger logger = Logger.getLogger(SignupServlet.class.getName());
  private ObjectMapper mapper = new ObjectMapper();
  private Config config = Config.getInstance();

  public GithubCreateStatus getStatus(boolean success, GithubPullEvent.PullRequest event) throws UnsupportedEncodingException {
    String url = config.baseUrl + "/validate?redirect_url=" + URLEncoder.encode(event.url, "UTF-8");
    if (success) {
      return new GithubCreateStatus(
              "success",
              "Contributor License Agreement signed",
              "cla-server/validation",
              config.whisperSystemsUrl + "/cla");
    } else {
      return new GithubCreateStatus(
              "failure",
              "Please sign the Contributor License Agreement",
              "cla-server/validation",
              url);
    }
  }

  /**
   * This is the endpoint for the github webhook
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
    String eventType = request.getHeader("X-GitHub-Event");
    if (eventType.equals("pull_request")) {
      String xHubSig = request.getHeader("X-Hub-Signature");
      StringWriter writer = new StringWriter();
      mapper.writeValue(writer, request.getParameterMap());
      String body = CharStreams.toString(request.getReader());
      GithubPullEvent event = mapper.readValue(mapper.getJsonFactory().createJsonParser(body), GithubPullEvent.class);

      try {
        Mac mac = Mac.getInstance("HmacSHA1");
        SecretKeySpec secret = new SecretKeySpec(config.githubSecret.getBytes("UTF-8"), "HmacSHA1");
        mac.init(secret);
        byte[] digest = mac.doFinal(body.getBytes());
        String hmac = String.format("sha1=%s", Hex.encodeHexString(digest));

        if (MessageDigest.isEqual(hmac.getBytes(), xHubSig.getBytes())) {
          updateStatus(config, event.pull_request);
        } else {
          logger.warning("Invalid request signature");
        }
      } catch (NoSuchAlgorithmException | InvalidKeyException e) {
        e.printStackTrace();
      }
    }
  }

  private boolean updateStatus(Config config, GithubPullEvent.PullRequest event) throws IOException {
    Query.FilterPredicate filter = new Query.FilterPredicate("githubUser",
            Query.FilterOperator.EQUAL, event.user.login);
    Query query = new Query("User").setFilter(filter);
    PreparedQuery pq = datastore.prepare(query);
    boolean success = pq.countEntities(FetchOptions.Builder.withLimit(1)) > 0;
    GithubCreateStatus status = getStatus(success, event);
    UrlFetcher.post(event.statuses_url, status, getAuthorization(config));
    return success;
  }

  private Map<String, String> getAuthorization(Config keyStore) {
    Map<String, String> headers = new HashMap<>();
    byte[] authBytes = String.format("%s:x-oauth-basic", keyStore.githubUserToken).getBytes();
    String basicAuth = "Basic " +  Base64.encodeBase64String(authBytes);;
    headers.put("Authorization", basicAuth);
    return headers;
  }

  /**
   * This is the destination for the details link for a pull request that has failed the CLA check.  When
   * clicked, it checks if the user has since signed the CLA, and if not, it redirects the user to the CLA.
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
    String url = request.getParameter("redirect_url");
    Map<String, String> headers = getAuthorization(config);
    headers.put("User-Agent", "openwhispersystems");
    GithubPullEvent.PullRequest event = new UrlFetcher<GithubPullEvent.PullRequest>().get(url, headers,
            GithubPullEvent.PullRequest.class);
    boolean status = updateStatus(config, event);
    if (status) {
      resp.sendRedirect(event.html_url);
    } else {
      resp.sendRedirect(config.whisperSystemsUrl + "/cla?redirect_url=" + url);
    }
  }
}
