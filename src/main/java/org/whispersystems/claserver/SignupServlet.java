package org.whispersystems.claserver;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.repackaged.org.joda.time.DateTime;
import com.google.appengine.repackaged.org.joda.time.format.DateTimeFormatter;
import com.google.appengine.repackaged.org.joda.time.format.ISODateTimeFormat;
import org.codehaus.jackson.map.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by tina on 1/25/15.
 */
public class SignupServlet extends HttpServlet {
  MustacheFactory mf = new DefaultMustacheFactory(new File("WEB-INF/templates"));
  Mustache mustache = mf.compile("form.mustache");
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private static final ObjectMapper mapper = new ObjectMapper();
  private static List<String> required = Arrays.asList("firstName", "lastName", "email", "address1", "city", "state",
          "zip", "country", "phone");
  private DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
  private Config config = Config.getInstance();

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    Entity user = new Entity("User");
    AddResponse addResponse = new AddResponse();
    Map parameterMap = mapper.readValue(mapper.getJsonFactory().createJsonParser(request.getInputStream()), Map.class);
    List<String> errorFields = validateAndPopulateUser(parameterMap);
    if (errorFields.size() == 0) {
      DateTime dt = new DateTime();
      parameterMap.put("timestamp", fmt.print(dt));
      parameterMap.put("ip", request.getRemoteAddr());
      StringWriter jsonProperties = new StringWriter();
      mapper.writeValue(mapper.getJsonFactory().createJsonGenerator(jsonProperties), parameterMap);
      user.setProperty("details", jsonProperties.toString());
      Key key = datastore.put(user);
      addResponse.setStatus(AddResponse.Status.ADDED);
      String id = String.valueOf(key.getId());
      String state = UUID.randomUUID().toString();
      MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();
      memcacheService.put(id, state);
      StringBuilder url = new StringBuilder("https://github.com/login/oauth/authorize?client_id=");
      String redirectUrl = config.baseUrl + "/oauth?id=" + id + "&redirect_url=" + request.getParameter("redirect_url");
      url.append(config.githubOauthClientId);
      url.append("&redirect_uri=");
      url.append(URLEncoder.encode(redirectUrl, "UTF-8"));
      url.append("&state=");
      url.append(state);
      addResponse.setAuthorizeUrl(url.toString());
    } else {
      addResponse.setStatus(AddResponse.Status.ERROR);
    }
    addResponse.setErrorFields(errorFields);
    response.addHeader("Access-Control-Allow-Origin", config.whisperSystemsUrl);
    response.setContentType("application/json");
    mapper.writeValue(response.getWriter(), addResponse);
    response.getWriter().flush();
  }

  private List<String> validateAndPopulateUser(Map parameterMap) throws IOException {
    List<String> errorFields = new ArrayList<>();
    for (String s : required) {
      String param = (String) parameterMap.get(s);
      if (param == null || param.isEmpty()) {
        errorFields.add(s);
      }
    }

    String signature = (String) parameterMap.get("signature");
    if (signature == null || !signature.equals("I AGREE")) {
      errorFields.add("signature");
    }
    return errorFields;
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
    resp.setContentType("text/html");
    mustache.execute(resp.getWriter(), AuthFormController.createEmptyFormController(mf)).flush();
  }

  @Override
  protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setHeader("Access-Control-Allow-Origin", config.whisperSystemsUrl);
    resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    resp.setHeader("Access-Control-Allow-Headers",  "Origin, X-Requested-With, Content-Type, Accept");
  }
}
