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

import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonIgnoreProperties;
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;

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
