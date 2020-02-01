package com.dmtavt.tests.javolution;

import com.dmtavt.tests.Person;
import com.dmtavt.tests.woodstox.WoodstoxParser;
import javolution.xml.stream.XMLStreamException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.dmtavt.tests.App.fileSizeHumanReadable;
import static org.junit.jupiter.api.Assertions.*;

class JavolutionParserTest {
    static final int numEntries = 4000000;
    static final Path dir = Paths.get("C:\\tmp\\xml");

    static Path getFilePath() {
        return dir.resolve("xml-" + numEntries + ".xml");
    }

    @Test
    void parse() throws IOException, XMLStreamException {
        long timeLo = System.nanoTime();
        List<? extends Person> people = JavolutionParser.parse(getFilePath());
        long timeHi = System.nanoTime();

        long totalSalary = people.stream().mapToLong(p -> p.salaryAmount).sum();
        double seconds = (timeHi - timeLo) / 1e9f;
        long fileSize = Files.size(getFilePath());
        String fs = fileSizeHumanReadable(fileSize);
        System.out.printf("Parsed %d persons (%s) in %.2f seconds, total salary %.0E\n", people.size(), fs, seconds, (double)totalSalary);
    }
}