package org.whispersystems.claserver;

import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonIgnoreProperties;
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;

/**
 * Created by tina on 1/25/15.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class AddResponse {
  public static enum Status {
    ADDED,
    ERROR
  }

  private Status status;
  private List<String> errorFields;
  private String authorizeUrl;

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public List<String> getErrorFields() {
    return errorFields;
  }

  public void setErrorFields(List<String> errorFields) {
    this.errorFields = errorFields;
  }

  public String getAuthorizeUrl() {
    return authorizeUrl;
  }

  public void setAuthorizeUrl(String authorizeUrl) {
    this.authorizeUrl = authorizeUrl;
  }
}
