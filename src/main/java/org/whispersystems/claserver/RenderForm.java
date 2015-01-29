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

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

public class RenderForm {
  public static void main(String[] args) throws IOException {
    MustacheFactory mf = new DefaultMustacheFactory(new File("src/main/webapp/WEB-INF/templates"));
    Mustache mustache = mf.compile("form_inner.mustache");
    StringWriter writer = new StringWriter();
    AuthFormController controller = AuthFormController.createEmptyFormController(mf);
    controller.baseUrl = "https://open-whisper-cla.appspot.com/cla-server";
    mustache.execute(writer, controller);
    writer.flush();
    System.out.println(writer.toString());
  }
}
