package za.co.bsg.cleaner;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This application parses all the .java, .ui.xml files in an application searching for all explicite styles and
 * then parses the given stylesheet and outputs a trimmed down style sheet with no unused styles
 */
public class Main {

    public static final String[] CLASS_METHOD_NAMES = new String[]{".setStyleName", ".addStyleName"};
    final static Logger LOG = LoggerFactory.getLogger(Main.class);

    private Set<String> usedClassStyles = new HashSet<String>();
    private Set<String> usedIdStyles = new HashSet<String>();

    public static void main(String[] args) {
        Main main = new Main();
        main.buildUsedStyleNameSet(new File(args[0]));
        String newStyleSheet = main.buildNewStyleSheet(new File(args[1]));
        main.writeNewStyleSheet(new File(args[2]), newStyleSheet);
    }

    private void writeNewStyleSheet(File file, String body) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write(body);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String buildNewStyleSheet(File stylesheet) {
        StringBuilder newStyleSheet = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(stylesheet));
            String line;
            boolean inStyleBody = false;
            boolean isUsed = false;
            StringBuilder buf = new StringBuilder();
            StringBuilder newTokenString = new StringBuilder();
            String oldTokenString;
            while ((line = br.readLine()) != null) {
                if (line.indexOf("{") > 0) {
                    newTokenString = new StringBuilder();
                    oldTokenString = line.substring(0, line.indexOf("{"));
                    String[] tokens = oldTokenString.split(" ");
                    for (String token : tokens) {
                        if ((token.startsWith(".") && usedClassStyles.contains(token.substring(1))) ||
                                (token.startsWith("#") && usedIdStyles.contains(token.substring(1))) ||
                                token.startsWith("@sprite")) {
                            newTokenString.append(token).append(" ");
                            isUsed = true;
                        } else if (!token.startsWith(".") && !token.startsWith("#") && !token.startsWith("@")) {
                            // This is an element so we have to include it
                            newTokenString.append(token).append(" ");
                            isUsed = true;
                        }
                    }
                    buf = new StringBuilder(line.substring(line.indexOf("{")));
                    buf.append("\n");
                    inStyleBody = true;
                } else if (inStyleBody) {
                    buf.append(line).append("\n");
                }
                if (line.contains("}")) {
                    inStyleBody = false;
                    String body = buf.toString();
                    if (isUsed) {
                        newStyleSheet.append(newTokenString).append(body).append("\n");
                        isUsed = false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newStyleSheet.toString();
    }

    private void buildUsedStyleNameSet(File srcPath) {
        Collection<File> files = FileUtils.listFiles(srcPath, new String[]{"java"}, true);
        try {
            for (File file : files) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    populateUsedStylesInJava(line, file.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        files = FileUtils.listFiles(srcPath, new String[]{"ui.xml", "jsp", "html"}, true);
        try {
            for (File file : files) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    populateUsedStylesInHTML(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        System.out.println("############################################### CLASS ");
//        for (String style : usedClassStyles) {
//            System.out.println(style);
//        }
//        System.out.println("############################################### ID ");
//        for (String style : usedIdStyles) {
//            System.out.println(style);
//        }
    }

    private void populateUsedStylesInJava(String line, String fileName) {
        char quote = '"';
        for (String methodName : CLASS_METHOD_NAMES) {
            try {
                int index = line.indexOf(methodName);
                if (index > 0) {
                    int styleStart = line.indexOf(quote, index) + 1;
                    if (styleStart <= 0) {
                        LOG.error("Variable used for style name in file " + fileName + "(" + line.trim() + ")");
                        continue;
                    }
                    int methodEnd = line.indexOf(")", styleStart);
                    int endStyle = getStyleName(line, styleStart, quote, usedClassStyles);
                    styleStart = line.indexOf("\"", endStyle + 1) + 1;
                    if (styleStart > 0 && styleStart < methodEnd) {
                        getStyleName(line, styleStart, quote, usedClassStyles);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void populateUsedStylesInHTML(String line) {
        String cls = "class=";
        try {
            int index = line.indexOf(cls);
            if (index > 0) {
                char quote = line.charAt(index + cls.length());
                int styleStart = line.indexOf(quote, index) + 1;
                getStyleName(line, styleStart, quote, usedIdStyles);
            }
        } catch (Exception e) {
            LOG.error("Line = " + line);
            e.printStackTrace();
        }
    }

    private int getStyleName(String str, int start, char endQuote, Set<String> styleSet) {
        int styleEnd = str.indexOf(endQuote, start);
        String style = str.substring(start, styleEnd);
        String[] styles = style.split(" ");
        for (String s : styles) {
            styleSet.add(s.trim());
        }
        return styleEnd;
    }

}
