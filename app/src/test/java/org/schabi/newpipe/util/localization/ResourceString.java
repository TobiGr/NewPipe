package org.schabi.newpipe.util.localization;

import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceString {
    public final String name;
    public final String text;
    public final boolean isFormatted;
    public final boolean isTranslatable;
    public final List<String> formatter;


    public ResourceString(final Element el) {
        this.name = el.attr("name");
        this.text = el.text();
        if (el.hasAttr("translatable")) {
            this.isTranslatable = !el.attr("translatable").equalsIgnoreCase("false");
        } else {
            this.isTranslatable = true;
        }
        // check if text is formatted
        //this.isFormatted = text.matches("%(\\d\\$)?([ds])+?");
        List<String> matches = new ArrayList<>();
        Matcher m = Pattern.compile("%(\\d\\$)?([ds])+?").matcher(text);

        while (m.find()) {
            matches.add(m.group());
        }
        this.isFormatted = !matches.isEmpty();
        this.formatter = matches;
    }

}
