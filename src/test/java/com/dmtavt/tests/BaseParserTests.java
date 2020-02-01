package com.dmtavt.tests;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.dmtavt.tests.App.fileSizeHumanReadable;

public class BaseParserTests {
    public static final int numEntries = 4000000;
    public static final Path dir = Paths.get("C:\\tmp\\xml");

    public static Path getFilePath() {
        return dir.resolve("xml-" + numEntries + ".xml");
    }

    @Test
    public void TestFileGeneration() throws IOException {
        Path path = BaseParserTests.getFilePath();
        System.out.println("Trying to create file: " + path.toString());
        if (!Files.exists(path.getParent()))
            throw new FileNotFoundException("Parent directory does not exist: " + path.getParent().toString());
        FakeData.createHugeXml(path, numEntries);
        long fileSize = Files.size(path);
        System.out.printf("Created file (%s) at: %s", fileSizeHumanReadable(fileSize), path);
    }
}
