package org.whispersystems.claserver;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Created by tina on 1/27/15.
 */
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
