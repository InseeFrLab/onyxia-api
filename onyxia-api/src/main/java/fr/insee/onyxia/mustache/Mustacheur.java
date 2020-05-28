package fr.insee.onyxia.mustache;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.mixed.kotlin.Person;

import java.io.StringReader;
import java.io.StringWriter;

public class Mustacheur {

	public static String mustache(String json, Object object) throws Exception {
		Person person = null;
		MustacheFactory mf = new DefaultMustacheFactory();
		Mustache mustache = mf.compile(new StringReader(json),"marathon.json.mustache");
		StringWriter writer = new StringWriter();
		mustache.execute(writer, object).flush();
		return writer.toString();
	}
}