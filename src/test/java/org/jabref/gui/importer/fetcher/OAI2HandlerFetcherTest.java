package org.jabref.gui.importer.fetcher;

import java.io.IOException;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jabref.logic.importer.util.OAI2Handler;
import org.jabref.model.entry.BibEntry;
import org.jabref.testutils.category.GUITest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

/**
 * Test for OAI2-Handler and Fetcher.
 *
 * @author Ulrich St&auml;rk
 * @author Christian Kopf
 * @author Christopher Oezbek
 */

@Category(GUITest.class)
public class OAI2HandlerFetcherTest {

    protected OAI2Handler handler;

    protected BibEntry be;

    protected SAXParserFactory parserFactory;

    protected SAXParser saxParser;


    @Before
    public void setUp() throws ParserConfigurationException, SAXException {
        parserFactory = SAXParserFactory.newInstance();
        saxParser = parserFactory.newSAXParser();
        be = new BibEntry("article");
        handler = new OAI2Handler(be);
    }

    @Test
    public void testCorrectLineBreaks() {
        Assert.assertEquals("Test this", OAI2Handler.correctLineBreaks("Test\nthis"));
        Assert.assertEquals("Test this", OAI2Handler.correctLineBreaks("Test \n this"));
        Assert.assertEquals("Test\nthis", OAI2Handler.correctLineBreaks("Test\n\nthis"));
        Assert.assertEquals("Test\nthis", OAI2Handler.correctLineBreaks("Test\n    \nthis"));
        Assert.assertEquals("Test\nthis", OAI2Handler.correctLineBreaks("  Test   \n   \n   this  "));
    }

    @Test
    public void testParse() throws Throwable {
        try {
            saxParser.parse(this.getClass().getResourceAsStream("oai2.xml"), handler);
            Assert.assertEquals(Optional.of("hep-ph/0408155"), be.getField("eprint"));
            Assert.assertEquals(Optional.of("G. F. Giudice and A. Riotto and A. Zaffaroni and J. López-Peña"),
                    be.getField("author"));
            Assert.assertEquals(Optional.of("Nucl.Phys. B"), be.getField("journal"));
            Assert.assertEquals(Optional.of("710"), be.getField("volume"));
            Assert.assertEquals(Optional.of("2005"), be.getField("year"));
            Assert.assertEquals(Optional.of("511-525"), be.getField("pages"));

            // Citekey is only generated if the user says so in the import
            // inspection dialog.
            Assert.assertEquals(Optional.empty(), be.getCiteKeyOptional());

            Assert.assertEquals(Optional.of("Heavy Particles from Inflation"), be.getField("title"));
            Assert.assertTrue(be.getField("abstract").isPresent());
            Assert.assertEquals(Optional.of("23 pages"), be.getField("comment"));
            Assert.assertEquals(Optional.of("CERN-PH-TH/2004-151"), be.getField("reportno"));
        } catch (SAXException e) {
            throw e.getException();
        }
    }

    @Test
    public void testOai22xml() throws SAXException, IOException {

        saxParser.parse(this.getClass().getResourceAsStream("oai22.xml"), handler);
        Assert.assertEquals(Optional.of("2005"), be.getField("year"));

    }

    @Test
    public void testOai23xml() throws SAXException, IOException {

        saxParser.parse(this.getClass().getResourceAsStream("oai23.xml"), handler);
        Assert.assertEquals(Optional.of("Javier López Peña and Gabriel Navarro"), be.getField("author"));

    }

    @Test
    public void testUrlConstructor() {
        OAI2Fetcher fetcher = new OAI2Fetcher();
        Assert.assertEquals(
                "http://export.arxiv.org/oai2?verb=GetRecord&identifier=oai%3AarXiv.org%3Ahep-ph%2F0408155&metadataPrefix=arXiv",
                fetcher.constructUrl("hep-ph/0408155"));

        Assert.assertEquals(
                "http://export.arxiv.org/oai2?verb=GetRecord&identifier=oai%3AarXiv.org%3Amath%2F0612188&metadataPrefix=arXiv",
                fetcher.constructUrl("math/0612188"));

    }

    @Test
    public void testFixKey() {
        Assert.assertEquals("", OAI2Fetcher.fixKey(""));
        Assert.assertEquals("test", OAI2Fetcher.fixKey("test"));
        Assert.assertEquals("math/0601001", OAI2Fetcher.fixKey("math.RA/0601001"));
        Assert.assertEquals("math/0601001", OAI2Fetcher.fixKey("math.QA/0601001"));
        Assert.assertEquals("hep-ph/0408155", OAI2Fetcher.fixKey("hep-ph/0408155"));
        Assert.assertEquals("0709.3040v1", OAI2Fetcher.fixKey("arXiv:0709.3040v1"));
        Assert.assertEquals("", OAI2Fetcher.fixKey("arXiv:"));
    }

    @Test
    @Ignore
    public void testOnline() throws InterruptedException, IOException, SAXException {

        {
            OAI2Fetcher fetcher = new OAI2Fetcher();
            be = fetcher.importOai2Entry("math.RA/0612188");
            Assert.assertNotNull(be);

            Assert.assertEquals(Optional.of("math/0612188"), be.getField("eprint"));
            Assert.assertEquals(Optional.of("On the classification and properties of noncommutative duplicates"),
                    be.getField("title"));
            Assert.assertEquals(Optional.of("Javier López Peña and Gabriel Navarro"), be.getField("author"));
            Assert.assertEquals(Optional.of("2007"), be.getField("year"));

            Thread.sleep(20000);
        }

        {
            OAI2Fetcher fetcher = new OAI2Fetcher();
            be = fetcher.importOai2Entry("astro-ph/0702080");
            Assert.assertNotNull(be);

            Assert.assertEquals(Optional.of("astro-ph/0702080"), be.getField("eprint"));
            Assert.assertEquals(
                    Optional.of(
                            "Magnetized Hypermassive Neutron Star Collapse: a candidate central engine for short-hard GRBs"),
                    be.getField("title"));

            Thread.sleep(20000);
        }

        {
            OAI2Fetcher fetcher = new OAI2Fetcher();
            be = fetcher.importOai2Entry("math.QA/0601001");
            Assert.assertNotNull(be);

            Assert.assertEquals(Optional.of("math/0601001"), be.getField("eprint"));
            Thread.sleep(20000);
        }

        {
            OAI2Fetcher fetcher = new OAI2Fetcher();
            be = fetcher.importOai2Entry("hep-ph/0408155");
            Assert.assertNotNull(be);

            Assert.assertEquals(Optional.of("hep-ph/0408155"), be.getField("eprint"));
            Thread.sleep(20000);
        }

        OAI2Fetcher fetcher = new OAI2Fetcher();
        be = fetcher.importOai2Entry("0709.3040");
        Assert.assertNotNull(be);

        Assert.assertEquals(Optional.of("2007"), be.getField("year"));
        Assert.assertEquals(Optional.of("#sep#"), be.getField("month"));

    }
}
