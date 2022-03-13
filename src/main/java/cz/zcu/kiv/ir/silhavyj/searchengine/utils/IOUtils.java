package cz.zcu.kiv.ir.silhavyj.searchengine.utils;

import java.io.*;
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

    public static String readFile(final String filename) {
        BufferedReader bufferedReader = null;
        String line;
        final StringBuilder stringBuilder = new StringBuilder();
        try {
            bufferedReader = new BufferedReader(new FileReader(filename));
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
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
        return stringBuilder.toString();
    }

    public static void createDirectoryIfMissing(final String dirname) {
        final var dir = new File(dirname);
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                System.err.printf("could not create directory %s", dirname);
            }
        }
    }

    public static void writeToFile(final String filename, final String data) {
        BufferedWriter bufferedWriter = null;
        try {
            File file = new File(filename);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    System.err.printf("could not create file %s", filename);
                }
            }
            FileWriter fileWriter = new FileWriter(file);
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally  {
            try {
                if (bufferedWriter != null)
                    bufferedWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
