package com.cubedhost.fir;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.objectweb.asm.ClassReader;

public class Fir {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static FileFilter forgeModFilter = (file) -> file.getName().endsWith(".jar") || file.getName().endsWith(".zip");

    private Fir() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java -jar Fir.jar <input>");
            System.err.println("Input can be a single file, multiple files, or a directory, which will be scanned recursively.");
            System.exit(1);
        }

        // Add inputs
        List<File> inputs = new ArrayList<>();
        Multimap<String, Map<String, Object>> output = HashMultimap.create();
        for (String inputString : args) {
            File file = new File(inputString);
            if (!file.exists()) {
                continue;
            }

            addInputFilesRecursively(inputs, file);
        }

        // Produce outputs
        for (File input : inputs) {
            List<Map<String, Object>> processed = processInput(input);
            if (processed != null) {
                output.putAll(input.getPath(), processed);
            }
        }

        // Print JSON output
        System.out.println(gson.toJson(output.asMap()));
    }

    private static void addInputFilesRecursively(List<File> inputList, File directory) throws IOException {
        if (!directory.isDirectory()) {
            inputList.add(directory);
            return;
        }

        for (File file : directory.listFiles(forgeModFilter)) {
            if (file.isDirectory()) {
                addInputFilesRecursively(inputList, file);
            } else {
                inputList.add(file);
            }
        }
    }

    private static List<Map<String, Object>> processInput(File input) {
        List<Map<String, Object>> output = new ArrayList<>();
        InfoClassVisitor visitor = new InfoClassVisitor(output);

        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(input))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (!entry.getName().endsWith(".class")) {
                    continue;
                }

                ClassReader cr = new ClassReader(zip);
                cr.accept(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        return output;
    }
}
