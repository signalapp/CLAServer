package org.whispersystems.claserver;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created by tina on 1/25/15.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class GithubPullEvent {
  @JsonIgnoreProperties(ignoreUnknown=true)
  public static class PullRequest {
    public String url;
    public String html_url;
    public User user;
    public String statuses_url;
  }

  @JsonIgnoreProperties(ignoreUnknown=true)
  public static class User {
    public String login;
  }

  public PullRequest pull_request;
}
