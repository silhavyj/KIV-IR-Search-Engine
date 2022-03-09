package cz.zcu.kiv.ir.silhavyj.searchengine.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class IOUtils {

    public static List<String> readLines(final String filename) {
        BufferedReader bufferedReader = null;
        String line;
        final List<String> lines = new LinkedList<>();
        try {
            bufferedReader = new BufferedReader(new FileReader(filename));
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return lines;
    }
}
