package org.schabi.newpipe.res;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.schabi.newpipe.RouterActivity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

import io.reactivex.annotations.NonNull;

public class StringTest {

    /**
     * Test for duplicate Strings in strings.xml to avoid dubble work for translators
     * and reduce APK size.
     */
    @Test
    public void duplicateStrings() throws Exception {
        // get all <string> tags from strings.xml
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse("src/main/res/values/strings.xml");
        // TODO add plurals and string-array
        NodeList configs = doc.getElementsByTagName("string");

        // and split them to names and values
        List<String> values = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (int i = 0; i < configs.getLength(); i++) {
            Element string = (Element) configs.item(i);
            values.add(string.getTextContent());
            names.add(string.getAttribute("name"));
        }

        final Set<String> singleStrings = new HashSet<String>();
        final Set<String> set1 = new HashSet<String>();

        // check for duplicate values with HashSet
        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            if (!set1.add(value)) {
                singleStrings.add(value);
            }
        }
        //assertEquals(0, singleStrings.size());
        //assertEquals(new HashSet<String>(), singleStrings);
        assertTrue("Duplicate strings in res/values/strings.xml: (String value: String names)\n"
                + getNamesOfDuplicates(names, values, new ArrayList<>(singleStrings)),
                new HashSet<String>() == singleStrings);

    }

    /**
     * Get String which contains all duplicate strings and their names 
     * @param names
     * @param values
     * @param singleStrings
     * @return
     */
    private static String getNamesOfDuplicates(List<String> names, List<String> values, List<String> singleStrings) {
        StringBuilder ret = new StringBuilder();
        for(String s : singleStrings) {
            ret.append(s + ": ");
            for (int i = 0; i < names.size(); i++) {
                if (s.equals(values.get(i))) {
                    ret.append(names.get(i) + ", ");
                }
            }
            // remove ", " from last duplicate name
            ret.delete(ret.length() - 2, ret.length());
            ret.append("\n");
        }
        return ret.toString();
    }
    
    /**
     * Get the strings.xml file as Document
     */
    private static Document getStrings() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse("src/main/res/values/strings.xml");
        return doc;
    }
    
    
    /**
     * Check for unused strings.
     * Android studio's version of this check does not detect strings in .xml files
     */
    @Test
    public void checkForUnusedStrings() throws Exception {
        Document doc = getStrings();
        NodeList configs = doc.getElementsByTagName("string");

        // get names of strings
        // TODO add other strings like stringArray etc.
        List<String> names = new ArrayList<>();
        for (int i = 0; i < configs.getLength(); i++) {
            Element string = (Element) configs.item(i);
            names.add(string.getAttribute("name"));
        }

        // get all files in this project which might contain a string
        File[] f = {new File("src/main")};
        Set<File> files = getFiles(f);
        int filesCount = 0;
        int namesCount = names.size();
        for(File file : files) {
            filesCount++;
            // Test every line
            try(BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while((line = br.readLine()) != null) {
                    // test every unused name
                    for(int i = 0; i < names.size(); i++) {
                        if (line.contains("R.string." + names.get(i))
                            || line.contains("@string/" + names.get(i)))
                            names.remove(i);
                    }
                }
            }
        }

        assertTrue("Tested " + filesCount + " files for " + namesCount + " strings and found "
                + names.size() +  " unused string" + (names.size() > 1 ? "s" : "")
                + " in resources:\n" + listToString(names), names.size() == 0);
    }
    
    private static Set<File> getFiles(@NonNull File[] f) {
        Set<File> files = new HashSet<>();
        for (int i = 0; i < f.length; i++) {
            if (f[i].exists() && !f[i].isDirectory()) {
                // only test files which can contain a string
                // skip translation files because they won't
                if (!f[i].toString().contains("strings.xml")
                    && (f[i].toString().contains(".xml") || f[i].toString().contains(".java"))) {
                    files.add(f[i]);
                }
            } else if (f[i].isDirectory()) {
                Set<File> nestedFiles = getFiles(f[i].listFiles());
                files.addAll(nestedFiles);
            }
        }
        return files;
    }

    private static String listToString(@NonNull List<String> l) {
        StringBuilder ret = new StringBuilder();
        for(String s : l) {
            ret.append("    " + s + "\n");
        }
        return ret.toString();
    }
    
    @Test
    public void styleChecker() {
        // TODO run styleChecker for all important files.

    }

    @Test
    public void improveCodeStyle() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse("build/reports/checkstyle/checkstyle.xml");

        NodeList files = doc.getElementsByTagName("file");
        int fixes = 0;
        for(int i = 0; i < files.getLength(); i++) {
            File file = new File(((Element)(files.item(i))).getAttribute("name"));
            NodeList errors = files.item(i).getChildNodes();
            /*for(int n = 0; n < errors.getLength(); n++) {
                System.out.println("?"+errors.item(n).toString()+"!");
            }
            System.out.println(((Element)(files.item(i))).getAttribute("name") + ": " + errors.getLength());*/
            System.out.println("File: "+((Element)(files.item(i))).getAttribute("name"));
            int removedLines = 0;
            error: for(int j = 0; j < errors.getLength(); j++) {
                if(errors.item(j).getNodeType() != Node.ELEMENT_NODE) continue error;
                Element error = (Element) errors.item(j);
                int line = Integer.parseInt(error.getAttribute("line"));
                String columnString = error.getAttribute("column");
                int column;
                if (columnString.equals("")) column = -1;
                else column = Integer.parseInt(error.getAttribute("column"));

                String message = error.getAttribute("message");
                switch (message) {
                    case "Nach 'if' folgt kein Leerzeichen.":
                    case "Nach '{' folgt kein Leerzeichen.":
                        addSpace(file, line - removedLines, column);
                        continue  error;
                    default:
                        System.out.println("---- " + message);
                        fixes--;
                        break;
                }

                String source = error.getAttribute("source");
                switch (source) {
                    case "com.puppycrawl.tools.checkstyle.checks.imports.UnusedImportsCheck":
                        removeLine(file, line - removedLines);
                        removedLines++;
                        fixes++;
                }
                fixes++;
            }
        }
        System.out.println(fixes + " problems fixed");
    }

    @Test
    public void testSpace() {
        addSpace(new File("E:/Tobias/Documents/NewPipe - Fork/app/src/main/java/org/schabi/newpipe/RouterActivity.java"),256,23);
    }

    private void addSpace(File file, int lineNumber, int column) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int ln = 0;
            while ((line = br.readLine()) != null) {
                ln++;
                if (ln != lineNumber) {
                    lines.add(line);
                } else {
                    lines.add(line.substring(0, column - 1) + " " + line.substring(column - 1));
                }
            }
            br.close();
            Files.write(file.toPath(), lines, Charset.forName("UTF-8"));

        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void removeLine(File file, int lineNumber) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int ln = 0;
            while ((line = br.readLine()) != null) {
                ln++;
                if (ln != lineNumber) {
                    lines.add(line);
                }
            }
            br.close();
            Files.write(file.toPath(), lines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}