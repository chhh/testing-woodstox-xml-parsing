package com.dmtavt.tests.woodstox;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class WoodstoxParser {
    @FunctionalInterface
    interface ConditionCallback {
        boolean processXml(XMLStreamReader2 sr) throws XMLStreamException;
    }

    interface TagPairCallback {
        void tagStart(String tagName, XMLStreamReader2 sr) throws XMLStreamException;

        void tagContents(String tagName, StringBuilder sb);
    }

    public static List<Person> parse(Path path) throws IOException, XMLStreamException {
        XMLInputFactory2 f = (XMLInputFactory2) XMLInputFactory2.newFactory();
        f.configureForSpeed();
//        f.configureForLowMemUsage();
        XMLStreamReader2 sr = null;
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            sr = (XMLStreamReader2) f.createXMLStreamReader(br);

            // fast forward to beginning 'persons' tag (will throw if we don't find the tag at all)
            processUntilTrue(sr, sr1 -> isTagStart(sr1, "persons"));

            final List<Person> persons = new ArrayList<>(); // we've found the tag, so we can allocate storage for data
            final StringBuilder sb = new StringBuilder(); // reuse a single string builder for all character aggregation

            // now keep processing unless we reach closing 'persons' tag
            processUntilTrue(sr, sr1 -> {
                if (isTagEnd(sr1, "persons"))
                    return true;

                if (isTagStart(sr1, "person")) {
                    // now we're finally reached a 'person', can start processing it
                    int idIndex = sr1.getAttributeInfo().findAttributeIndex("", "id");
                    Person p = new Person(Integer.parseInt(sr1.getAttributeValue(idIndex)));

                    sr1.next();
                    processUntilTrue(sr1, sr2 -> {
                        // processing the meat of a 'person' tag
                        // split it into a function of its own to not clutter the main loop
                        //return processPerson(sr2, p, sb);
                        if (isTagEnd(sr2, "person"))
                            return true; // we're done processing a 'person' only when we reach the ending 'person' tag

                        if (isTagStart(sr2))
                            processTagPair(sr2, sb, p);

                        return false;
                    });
                    // we've reached the end of a 'person'
                    if (p.isComplete()) {
                        persons.add(p);
                    } else {
                        throw new IllegalStateException("Whoa, a person had incomplete data");
                    }
                }

                return false;
            });
            return persons;

        } finally {
            if (sr != null)
                sr.close();
        }

    }

    public static void processTagPair(XMLStreamReader2 sr, StringBuilder sb, TagPairCallback callback) throws XMLStreamException {
        final String tagName = sr.getLocalName();
        callback.tagStart(tagName, sr); // let the caller do whatever they need with the tag name and attributes
        sb.setLength(0); // clear our buffer, preparing to read the characters inside
        processUntilTrue(sr, sr1 -> {
            switch (sr1.getEventType()) {
                case XMLStreamReader2.END_ELEMENT: // ending condition
                    callback.tagContents(tagName, sb); // let the caller do whatever they need with text contents of the tag
                    return true;
                case XMLStreamReader2.CHARACTERS:
                    sb.append(sr1.getText());
                    break;
            }
            return false;
        });
    }

    public static boolean isTagStart(XMLStreamReader2 sr, String tagName) {
        return XMLStreamReader2.START_ELEMENT == sr.getEventType() && tagName.equalsIgnoreCase(sr.getLocalName());
    }

    public static boolean isTagStart(XMLStreamReader2 sr) {
        return XMLStreamReader2.START_ELEMENT == sr.getEventType();
    }

    public static boolean isTagEnd(XMLStreamReader2 sr, String tagName) {
        return XMLStreamReader2.END_ELEMENT == sr.getEventType() && tagName.equalsIgnoreCase(sr.getLocalName());
    }

    public static void processUntilTrue(XMLStreamReader2 sr, ConditionCallback callback) throws XMLStreamException {
        do {
            if (callback.processXml(sr))
                return;
        } while (sr.hasNext() && sr.next() >= 0);
        throw new IllegalStateException("xml document ended without callback returning true");
    }


    public static class Person implements TagPairCallback {
        final int id;
        String first;
        String last;
        String middle;
        int dobYear;
        int dobMonth;
        String gender;
        String salaryCurrency;
        int salaryAmount;
        String street;
        String city;

        public Person(int id) {
            this.id = id;
        }

        boolean isComplete() {
            return id != 0 && dobYear > 0 && dobMonth > 0 && salaryAmount > 0
                    && first != null && last != null && middle != null
                    && gender != null && salaryCurrency != null && city != null && street != null;
        }

        @Override
        public void tagStart(String tagName, XMLStreamReader2 sr) throws XMLStreamException {
            switch (tagName) {
                case "salary": // we only care about 'salary' as it's the only tag that has attributes
                    this.salaryCurrency = sr.getAttributeValue(sr.getAttributeIndex("", "currency"));
                    break;
            }
        }

        @Override
        public void tagContents(String tagName, StringBuilder sb) {
            switch (tagName) {
                case "firstname":
                    this.first = sb.toString();
                    break;
                case "lastname":
                    this.last = sb.toString();
                    break;
                case "middlename":
                    this.middle = sb.toString();
                    break;
                case "dob_year":
                    this.dobYear = Integer.parseInt(sb.toString());
                    break;
                case "dob_month":
                    this.dobMonth = Integer.parseInt(sb.toString());
                    break;
                case "gender":
                    this.gender = sb.toString();
                    break;
                case "salary":
                    this.salaryAmount = Integer.parseInt(sb.toString());
                    break;
                case "street":
                    this.street = sb.toString();
                    break;
                case "city":
                    this.city = sb.toString();
                    break;
            }
        }
    }
}
