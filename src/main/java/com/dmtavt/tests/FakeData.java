package com.dmtavt.tests;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Random;

public class FakeData {
    public static void createHugeXml(Path path, int numEntries) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(path, StandardOpenOption.CREATE_NEW)) {
            String template =
                    "    <person id=\"%d\">\n" +
                            "        <firstname>%s</firstname>\n" +
                            "        <lastname>%s</lastname>\n" +
                            "        <middlename>%s</middlename>\n" +
                            "        <dob_year>%d</dob_year>\n" +
                            "        <dob_month>%d</dob_month>\n" +
                            "        <gender>%s</gender>\n" +
                            "        <salary currency=\"Euro\">%d</salary>\n" +
                            "        <street>%d apple street</street>\n" +
                            "        <city>%s</city>\n" +
                            "    </person>\n";
            Random r = new Random(42);
            bw.write("<persons>\n");
            for (int i = 0; i < numEntries; i++) {
                bw.write(String.format(template, i+1,
                        createFakeName(r, 3, 10), createFakeName(r, 3, 10), createFakeName(r, 3, 10), // name
                        1900 + r.nextInt(120), 1 + r.nextInt(12), // dob
                        r.nextBoolean() ? "M" : "F", 1 + r.nextInt(100000), // sex, salary
                        1 + r.nextInt(1000), r.nextBoolean() ? "London" : "New York")); // address
            }
            bw.write("</persons>\n");
        }
    }

    public static String createFakeName(Random r, int minLen, int maxLen) {
        if (minLen <= 0 || maxLen < minLen) throw new IllegalArgumentException("len > 0");
        int len = minLen + r.nextInt(maxLen - minLen);
        char[] name = new char[len];
        name[0] = (char)('A' + r.nextInt(26));
        for (int i = 1; i < len; i++) {
            name[i] = (char)('a' + r.nextInt(26));
        }
        return new String(name);
    }
}
