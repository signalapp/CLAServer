package org.whispersystems.claserver;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created by tina on 1/26/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubUserResponse {
  public String login;
  public Long id;
}
