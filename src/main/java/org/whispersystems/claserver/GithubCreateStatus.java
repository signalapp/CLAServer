package org.whispersystems.claserver;

/**
 * Created by tina on 1/25/15.
 */
public class GithubCreateStatus {
  public String state;
  public String description;
  public String context;
  public String target_url;

  public GithubCreateStatus(String state, String description, String context, String target_url) {
    this.state = state;
    this.description = description;
    this.context = context;
    this.target_url = target_url;
  }
}
