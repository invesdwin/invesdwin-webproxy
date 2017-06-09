package de.invesdwin.webproxy;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTableHeaderCell;

import de.invesdwin.context.integration.csv.CsvVerification;

@Immutable
public final class HtmlPages {

    private HtmlPages() {}

    public static String toText(final Page page) {
        if (page == null) {
            return null;
        } else if (page instanceof HtmlPage) {
            final HtmlPage htmlPage = (HtmlPage) page;
            return htmlPage.asText();
        } else {
            return page.getWebResponse().getContentAsString();
        }
    }

    public static String toHtml(final Page page) {
        if (page == null) {
            return null;
        } else {
            final String str = page.getWebResponse().getContentAsString();
            if (str != null) {
                return str;
            } else if (page instanceof HtmlPage) {
                final HtmlPage cResponse = (HtmlPage) page;
                return cResponse.asXml();
            } else {
                return null;
            }
        }
    }

    public static String extractTables(@Nonnull final HtmlPage page) {
        return extractTables(page, null);
    }

    public static String extractTables(@Nonnull final HtmlPage page, final CsvVerification csvVerification) {
        final StringBuilder tabellen = new StringBuilder();
        for (final DomElement table : page.getElementsByTagName("table")) {
            normalizeTableHeaders(table);
            final String tabelle = table.asText();
            if (csvVerification == null || csvVerification.isCsv(tabelle)) {
                tabellen.append(tabelle);
                tabellen.append("\n");
            }
        }
        return tabellen.toString();
    }

    public static String extractTableRows(final HtmlPage page, final CsvVerification csvVerification) {
        final StringBuilder tabellen = new StringBuilder();
        for (final DomElement table : page.getElementsByTagName("table")) {
            normalizeTableHeaders(table);
            final String tabelle = table.asText();
            if (csvVerification == null) {
                tabellen.append(tabelle);
                tabellen.append("\n");
            } else {
                tabellen.append(csvVerification.filterCsv(tabelle));
            }
        }
        return tabellen.toString();
    }

    /**
     * Eliminate BR-Tags table headers, so that the columns are being kept properly. BR is being turned into newline by
     * asText() and ten set as the HTML-Content. Another call to asText() then eliminates the newlines because html is
     * white space agnostic.
     */
    private static void normalizeTableHeaders(final DomElement table) {
        for (final HtmlElement thElement : table.getElementsByTagName("th")) {
            final HtmlTableHeaderCell th = (HtmlTableHeaderCell) thElement;
            final String textContent = th.asText();
            th.setTextContent(textContent);
        }
    }

    public static List<DomElement> findFirstChildLevelOfTag(final DomElement rootElement, final String tagName) {
        return findFirstChildLevelOfTag(rootElement.getChildElements(), tagName);
    }

    private static List<DomElement> findFirstChildLevelOfTag(final Iterable<DomElement> domElements,
            final String tagName) {
        List<DomElement> divs = new ArrayList<DomElement>();
        for (final DomElement domElement : domElements) {
            if (tagName.equals(domElement.getTagName())) {
                divs.add(domElement);
            }
        }
        if (divs.isEmpty()) {
            for (final DomElement domElement : domElements) {
                divs = findFirstChildLevelOfTag(domElement.getChildElements(), tagName);
                if (!divs.isEmpty()) {
                    break;
                }
            }
        }
        return divs;
    }

}
