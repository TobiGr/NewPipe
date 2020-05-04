package org.schabi.newpipe.util.localization;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class ResourceStringsTest {
    @Rule
    public ErrorCollector collector = new ErrorCollector();

    private static HashMap<String, ResourceString> englishStrings = new HashMap<>();
    private static HashMap<String, HashMap<String, ResourceString>> localizations = new HashMap<>();

    private static File resDir = new File("src/main/res/");

    @BeforeClass
    public static void setUp() throws IOException {

        englishStrings = parseLocale("values");
        assertFalse(englishStrings.isEmpty());

        String[] stringResourceDirs = resDir.list((dir, name) -> name.matches(
                // get all values directories which are named like a local
                // values-land contains info for landscape orientation and is therefore excluded
                "values-((?!land)[a-zA-Z+\\-]+)"
                ));


        for (String dirName: stringResourceDirs) {
            try {
                HashMap<String, ResourceString> locale = parseLocale(dirName);
                assertFalse("Locale is empty:" + dirName, locale.isEmpty()
                        && !dirName.equals("values-cv") && !dirName.equals("values-pr"));
                localizations.put(dirName, locale);
            } catch (Exception e) {
                fail("Could not parse localization in '" + dirName + "'");
                e.printStackTrace();
            }
        }

        System.out.println(localizations.size());
    }

    @Test
    public void testFormatter() {
        localizations.forEach((localization, translations) ->
            translations.forEach((name, resource) -> {
                ResourceString englishResource = englishStrings.get(name);
                collector.checkThat("Unused translation in " + localization + ":" + name,
                        englishResource, is(notNullValue()));
                if (englishResource == null) {
                    return;
                }
                collector.checkThat(
                        "Translation in " + localization
                                + " has attribute translatable=\"false\": " + name,
                        resource.isTranslatable, is(true));

                collector.checkThat(
                        "English resource string has attribute translatable=\"false\": " + name,
                        englishResource.isTranslatable, is(true));

                if (englishResource.isFormatted) {

                    for (String f: englishResource.formatter) {
                        boolean hasFormatter = false;
                        for (int i = 0; i < resource.formatter.size(); i++) {
                            if (f.equals(resource.formatter.get(i))) {
                                hasFormatter = true;
                               resource.formatter.remove(i);
                               break;
                            }
                        }

                        collector.checkThat(
                                "Translation in " + localization
                                        + " is not correctly formatted: " + name,
                                hasFormatter, is(true));
                    }
                }
            })
        );
    }

    private static HashMap<String, ResourceString> parseLocale(final String dirName)
            throws IOException {
        final File f = new File(resDir.getAbsolutePath() + "/" + dirName + "/strings.xml");
        final String html = new String(Files.readAllBytes(Paths.get(f.getAbsolutePath())));
        Document doc = Jsoup.parse(html, "", Parser.xmlParser());
        HashMap<String, ResourceString> strings = new HashMap<>(doc.getAllElements().size());
        Elements els = doc.select("resources string");
        for (Element e : els) {
            // TODO: handle plurals
            strings.put(e.attr("name"), new ResourceString(e));
        }
        return strings;
    }
}
