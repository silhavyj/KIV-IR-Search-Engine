package cz.zcu.kiv.ir.silhavyj.searchengine.utils;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

/***
 * @author Jakub Silhavy
 *
 * This class works as a utility class that is used throughout the
 * application. Its main purpose is to handle IO operations e.g.
 * reading the content of a file, writting into a file, etc.
 */
public class IOUtils {

    /***
     * Reads the content of a file line by line.
     * @param filename path to the file on the disk
     * @return List of lines which make up the content of the file
     */
    public static List<String> readLines(final String filename) {
        BufferedReader bufferedReader = null;
        String line;
        final List<String> lines = new LinkedList<>();
        try {
            // Read the file line by line.
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

    /***
     * Reads the content of a file as one string.
     * @param filename path to the file on the disk
     * @return the content of the file
     */
    public static String readFile(final String filename) {
        BufferedReader bufferedReader = null;
        String line;
        final StringBuilder stringBuilder = new StringBuilder();
        try {
            // Read the content of the file line by line.
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
        // Concatenate the lines that make up the content of the file
        // and return it as one string.
        return stringBuilder.toString();
    }

    /***
     * Creates a directory on the disk.
     * This method is called when a directory into which a file
     * should be stored is missing (fetched-articles).
     * @param dirname name of the directory
     */
    public static void createDirectoryIfMissing(final String dirname) {
        final var dir = new File(dirname);
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                System.err.printf("could not create directory %s", dirname);
            }
        }
    }

    /***
     * Writes a string into a file.
     * @param filename path to the file on the disk
     * @param data data to be stored in the file.
     */
    public static void writeToFile(final String filename, final String data) {
        BufferedWriter bufferedWriter = null;
        try {
            File file = new File(filename);

            // Make sure the file exists.
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
