package ru.ifmo.rain.chizhikov.walk;

import java.io.*;
import java.nio.file.*;


//java -p . -cp . -m info.kgeorgiy.java.advanced.walk Walk ru.ifmo.rain.chizhikov.walk.Walk

public class Walk {
    private static final int FNV_INIT_NUMBER = 0x811c9dc5;
    private static final int FNV_PRIME_NUMBER = 0x01000193;

    private static int calculateHashOfFile(Path path) {
        int current = FNV_INIT_NUMBER;

        try (FileInputStream inputStream = new FileInputStream(path.toString())) {

            byte[] buffer = new byte[1024];
            int readingByte;

            while ((readingByte = inputStream.read(buffer)) != -1) {
                for (int i = 0; i < readingByte; ++i) {
                    current *= FNV_PRIME_NUMBER;
                    current ^= buffer[i] & 0xff;
                }
            }
        } catch (IOException e) {
            current = 0;
        }

        return current;
    }


    private static void walk(String input, String output) {
        Path inputPath;
        Path outPath;
        try {
            inputPath = Paths.get(input);
            outPath = Paths.get(output);
        } catch (InvalidPathException e) {
            System.out.println("ERROR: InvalidPathException: " + e.getMessage());
            return;
        }
        if (outPath.getParent() != null) {
            try {
                Files.createDirectories(outPath.getParent());
            } catch (IOException e) {
                System.out.println("ERROR: IOException can't create folder for output file: " + e.getMessage());
                return;
            }
        }

        try (BufferedReader reader = Files.newBufferedReader(inputPath)) {
            try (BufferedWriter writer = Files.newBufferedWriter(outPath)) {
                try {
                    String file;

                    while ((file = reader.readLine()) != null) {
                        try {
                            Path path = Paths.get(file);

                            writer.write(String.format("%08x %s", Walk.calculateHashOfFile(path), path.toString()));
                            writer.newLine();
                        } catch (InvalidPathException e) {
                            writer.write(String.format("%08x %s", 0, file));
                            writer.newLine();
                        }
                    }
                } catch (IOException e) {
                    System.out.println("ERROR: IOException: " + e.getMessage());
                }
            } catch (FileNotFoundException e) {
                System.out.println("ERROR: Output file not found: " + e.getMessage());
            } catch (SecurityException e) {
                System.out.println("ERROR: SecurityException while working with output file: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("ERROR: IOException while working with output file: " + e.getMessage());
            }
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: Input file not found: " + e.getMessage());
        } catch (SecurityException e) {
            System.out.println("ERROR: SecurityException while working with input file: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("ERROR: IOException while working with input file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args != null) {
            if (args.length != 2 || args[0] == null || args[1] == null) {
                System.out.println("ERROR: Invalid arguments");
            } else {
                walk(args[0], args[1]);
            }
        }
    }

}
