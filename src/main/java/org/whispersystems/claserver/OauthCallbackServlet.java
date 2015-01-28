package org.whispersystems.claserver;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tina on 1/26/15.
 */
public class OauthCallbackServlet extends HttpServlet {
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private Config config = Config.getInstance();

  protected void doGet(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
    String id = request.getParameter("id");
    MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();
    String state = (String) memcacheService.get(id);

    // Make sure the state matches to prevent XSRF
    if (state.equals(request.getParameter("state"))) {
      Map<String, String> params = new HashMap<>();
      params.put("client_id", config.githubOauthClientId);
      params.put("client_secret", config.githubOauthClientSecret);
      params.put("code", request.getParameter("code"));
      params.put("redirect_uri", config.baseUrl + "/oauth?redirect_url=" + request.getParameter("redirect_url"));

      String returnParams = UrlFetcher.post("https://github.com/login/oauth/access_token", params,
              Collections.<String, String>emptyMap());
      String[] pairs = returnParams.split("&");
      for (String pair1 : pairs) {
        String[] pair = pair1.split("=");
        if (pair[0].equals("access_token")) {
          try {
            Entity user = datastore.get(KeyFactory.createKey("User", Long.parseLong(id)));
            user.setProperty("accessToken", pair[1]);
            GithubUserResponse response = new UrlFetcher<GithubUserResponse>().get("https://api.github.com/user?access_token=" + pair[1],
                    Collections.singletonMap("User-Agent", "openwhispersystems"), GithubUserResponse.class);
            user.setProperty("githubUser", response.login);
            datastore.put(user);
            String urlParam = request.getParameter("redirect_url");
            if (urlParam != null && !urlParam.equals("null")) {
              resp.sendRedirect(config.baseUrl + "/validate?redirect_url=" + urlParam);
            } else {
              resp.sendRedirect(config.whisperSystemsUrl + "/cla/success.html");
            }
          } catch (EntityNotFoundException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }
}