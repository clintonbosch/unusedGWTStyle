package za.co.bsg.cleaner;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collection;

/**
 * This application parses all the .java, .ui.xml files in an application searching for all explicite styles and
 * then parses the given stylesheet and outputs a trimmed down style sheet with no unused styles
 */
public class Main {

    public static final String[] EXTENSIONS = new String[]{"java", "ui.xml"};

    public static void main(String[] args) {
        Main main = new Main();
        File src = new File(args[0]);
        main.buildUsedStyleNameSet(src);
    }

    private void buildUsedStyleNameSet(File srcPath) {
        Collection<File> files = FileUtils.listFiles(srcPath, EXTENSIONS, true);
        for (File file : files) {
            System.out.println(file.getName());
        }
//        File folder = new File(path);
//        File[] listOfFiles = folder.listFiles();
//
//        for (int i = 0; i < listOfFiles.length; i++) {
//            File oldFile = listOfFiles[i];
//            if (oldFile.isFile()) {
//                String oldFileName = oldFile.getName();
//                System.out.println("-----------------------------");
//                System.out.println(oldFileName);
//                int index = oldFileName.lastIndexOf(SEPARATOR);
//                StringBuilder buf = new StringBuilder();
//                buf.append(oldFileName.substring(index + SEPARATOR.length(), oldFileName.indexOf(EXT)));
//                buf.append(SEPARATOR);
//                buf.append(oldFileName.substring(0, index));
//                buf.append(EXT);
//                String newFileName = buf.toString();
//                System.out.println(newFileName);
//                File newFile = new File(newFileName);
//                oldFile.renameTo(newFile);
//            }
//        }
    }

}
