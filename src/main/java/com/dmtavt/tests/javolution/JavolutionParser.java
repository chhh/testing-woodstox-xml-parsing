package com.dmtavt.tests.javolution;

import com.dmtavt.tests.Person;
import javolution.text.CharArray;
import javolution.xml.internal.stream.XMLStreamReaderImpl;
import javolution.xml.stream.XMLStreamException;
import javolution.xml.stream.XMLStreamReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JavolutionParser {
    @FunctionalInterface
    interface ConditionCallback {
        boolean processXml(XMLStreamReader sr) throws XMLStreamException;
    }

    interface TagPairCallback {
        void tagStart(CharArray tagName, XMLStreamReader sr) throws XMLStreamException;

        void tagContents(CharArray tagName, StringBuilder sb);
    }

    public static List<? extends Person> parse(Path path) throws IOException, XMLStreamException {
        XMLStreamReader sr = null;
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            XMLStreamReaderImpl reader = new XMLStreamReaderImpl();
            reader.setInput(br);
            sr = (XMLStreamReader)reader;


            // fast forward to beginning 'persons' tag (will throw if we don't find the tag at all)
            processUntilTrue(sr, sr1 -> isTagStart(sr1, "persons"));

            final List<JavolutionPerson> persons = new ArrayList<>(); // we've found the tag, so we can allocate storage for data
            final StringBuilder sb = new StringBuilder(); // reuse a single string builder for all character aggregation

            // now keep processing unless we reach closing 'persons' tag
            processUntilTrue(sr, sr1 -> {
                if (isTagEnd(sr1, "persons"))
                    return true;

                if (isTagStart(sr1, "person")) {
                    // now we're finally reached a 'person', can start processing it
                    JavolutionPerson p = new JavolutionPerson(Integer.parseInt(sr1.getAttributeValue("", "id").toString()));

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

    public static void processTagPair(XMLStreamReader sr, StringBuilder sb, TagPairCallback callback) throws XMLStreamException {
        final CharArray tagName = sr.getLocalName();
        callback.tagStart(tagName, sr); // let the caller do whatever they need with the tag name and attributes
        sb.setLength(0); // clear our buffer, preparing to read the characters inside
        processUntilTrue(sr, sr1 -> {
            switch (sr1.getEventType()) {
                case XMLStreamReader.END_ELEMENT: // ending condition
                    callback.tagContents(tagName, sb); // let the caller do whatever they need with text contents of the tag
                    return true;
                case XMLStreamReader.CHARACTERS:
                    sb.append(sr1.getText());
                    break;
            }
            return false;
        });
    }

    public static boolean isTagStart(XMLStreamReader sr, CharSequence tagName) {
        return XMLStreamReader.START_ELEMENT == sr.getEventType() && sr.getLocalName().contentEquals(tagName);
    }

    public static boolean isTagStart(XMLStreamReader sr) {
        return XMLStreamReader.START_ELEMENT == sr.getEventType();
    }

    public static boolean isTagEnd(XMLStreamReader sr, CharSequence tagName) {
        return XMLStreamReader.END_ELEMENT == sr.getEventType() && sr.getLocalName().contentEquals(tagName);
    }

    public static void processUntilTrue(XMLStreamReader sr, ConditionCallback callback) throws XMLStreamException {
        do {
            if (callback.processXml(sr))
                return;
        } while (sr.hasNext() && sr.next() >= 0);
        throw new IllegalStateException("xml document ended without callback returning true");
    }


    public static class JavolutionPerson extends Person implements TagPairCallback {
        public JavolutionPerson(int id) {
            super(id);
        }

        @Override
        public void tagStart(CharArray tagName, XMLStreamReader sr) throws XMLStreamException {
            if (tagName.contentEquals("salary")) {
                // we only care about 'salary' as it's the only tag that has attributes
                //this.salaryCurrency = sr.getAttributeValue(sr.getAttributeIndex("", "currency"));
                this.salaryCurrency = sr.getAttributeValue("", "currency").toString();
            }
        }

        @Override
        public void tagContents(CharArray tagName, StringBuilder sb) {
            if (tagName.contentEquals("firstname")) {
                this.first = sb.toString();
            } else if (tagName.contentEquals("lastname")) {
                this.last = sb.toString();
            } else if (tagName.contentEquals("middlename")) {
                this.middle = sb.toString();
            } else if (tagName.contentEquals("dob_year")) {
                this.dobYear = Integer.parseInt(sb.toString());
            } else if (tagName.contentEquals("dob_month")) {
                this.dobMonth = Integer.parseInt(sb.toString());
            } else if (tagName.contentEquals("gender")) {
                this.gender = sb.toString();
            } else if (tagName.contentEquals("salary")) {
                this.salaryAmount = Integer.parseInt(sb.toString());
            } else if (tagName.contentEquals("street")) {
                this.street = sb.toString();
            } else if (tagName.contentEquals("city")) {
                this.city = sb.toString();
            }
        }
    }
}
