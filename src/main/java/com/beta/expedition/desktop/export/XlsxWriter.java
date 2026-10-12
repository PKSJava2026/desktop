package com.beta.expedition.desktop.export;

import com.beta.expedition.desktop.model.TableDto;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class XlsxWriter {

    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
    private static final String MAIN_NS = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";
    private static final String REL_NS = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
    private static final String PKG_REL_NS = "http://schemas.openxmlformats.org/package/2006/relationships";
    private static final int MAX_SHEET_NAME = 31;
    private static final int MIN_COLUMN_WIDTH = 10;
    private static final int MAX_COLUMN_WIDTH = 60;

    private XlsxWriter() {
    }

    public static byte[] write(List<TableDto> sheets) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes, StandardCharsets.UTF_8)) {
            put(zip, "[Content_Types].xml", contentTypes(sheets.size()));
            put(zip, "_rels/.rels", rootRels());
            put(zip, "xl/workbook.xml", workbook(sheets));
            put(zip, "xl/_rels/workbook.xml.rels", workbookRels(sheets.size()));
            put(zip, "xl/styles.xml", styles());
            for (int i = 0; i < sheets.size(); i++) {
                put(zip, "xl/worksheets/sheet" + (i + 1) + ".xml", sheet(sheets.get(i)));
            }
        } catch (IOException e) {
            throw new ExportException("Не удалось сформировать файл Excel", e);
        }
        return bytes.toByteArray();
    }

    private static void put(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String contentTypes(int sheetCount) {
        StringBuilder xml = new StringBuilder(XML_HEADER)
                .append("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">")
                .append("<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>")
                .append("<Default Extension=\"xml\" ContentType=\"application/xml\"/>")
                .append("<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>")
                .append("<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>");
        for (int i = 1; i <= sheetCount; i++) {
            xml.append("<Override PartName=\"/xl/worksheets/sheet").append(i)
                    .append(".xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>");
        }
        return xml.append("</Types>").toString();
    }

    private static String rootRels() {
        return XML_HEADER + "<Relationships xmlns=\"" + PKG_REL_NS + "\">"
                + "<Relationship Id=\"rId1\" Type=\"" + REL_NS + "/officeDocument\" Target=\"xl/workbook.xml\"/>"
                + "</Relationships>";
    }

    private static String workbook(List<TableDto> sheets) {
        StringBuilder xml = new StringBuilder(XML_HEADER)
                .append("<workbook xmlns=\"").append(MAIN_NS).append("\" xmlns:r=\"").append(REL_NS).append("\"><sheets>");
        for (int i = 0; i < sheets.size(); i++) {
            String name = sheets.get(i).name();
            if (name.length() > MAX_SHEET_NAME) {
                name = name.substring(0, MAX_SHEET_NAME);
            }
            xml.append("<sheet name=\"").append(escape(name)).append("\" sheetId=\"").append(i + 1)
                    .append("\" r:id=\"rId").append(i + 1).append("\"/>");
        }
        return xml.append("</sheets></workbook>").toString();
    }

    private static String workbookRels(int sheetCount) {
        StringBuilder xml = new StringBuilder(XML_HEADER)
                .append("<Relationships xmlns=\"").append(PKG_REL_NS).append("\">");
        for (int i = 1; i <= sheetCount; i++) {
            xml.append("<Relationship Id=\"rId").append(i).append("\" Type=\"").append(REL_NS)
                    .append("/worksheet\" Target=\"worksheets/sheet").append(i).append(".xml\"/>");
        }
        xml.append("<Relationship Id=\"rId").append(sheetCount + 1).append("\" Type=\"").append(REL_NS)
                .append("/styles\" Target=\"styles.xml\"/>");
        return xml.append("</Relationships>").toString();
    }

    private static String styles() {
        return XML_HEADER + "<styleSheet xmlns=\"" + MAIN_NS + "\">"
                + "<fonts count=\"2\"><font><sz val=\"11\"/><name val=\"Calibri\"/></font>"
                + "<font><b/><sz val=\"11\"/><name val=\"Calibri\"/></font></fonts>"
                + "<fills count=\"2\"><fill><patternFill patternType=\"none\"/></fill>"
                + "<fill><patternFill patternType=\"gray125\"/></fill></fills>"
                + "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>"
                + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
                + "<cellXfs count=\"2\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
                + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/></cellXfs>"
                + "</styleSheet>";
    }

    private static String sheet(TableDto table) {
        StringBuilder xml = new StringBuilder(XML_HEADER)
                .append("<worksheet xmlns=\"").append(MAIN_NS).append("\">");
        appendColumnWidths(xml, table);
        xml.append("<sheetData>");
        appendRow(xml, 1, table.columns(), true);
        int rowNumber = 2;
        for (List<Object> row : table.rows()) {
            appendRow(xml, rowNumber++, row, false);
        }
        return xml.append("</sheetData></worksheet>").toString();
    }

    private static void appendColumnWidths(StringBuilder xml, TableDto table) {
        int count = table.columns().size();
        if (count == 0) {
            return;
        }
        xml.append("<cols>");
        for (int i = 0; i < count; i++) {
            int longest = table.columns().get(i).length();
            for (List<Object> row : table.rows()) {
                Object value = i < row.size() ? row.get(i) : null;
                if (value != null) {
                    longest = Math.max(longest, value.toString().length());
                }
            }
            int width = Math.min(MAX_COLUMN_WIDTH, Math.max(MIN_COLUMN_WIDTH, longest + 2));
            xml.append("<col min=\"").append(i + 1).append("\" max=\"").append(i + 1).append("\" width=\"")
                    .append(width).append("\" customWidth=\"1\"/>");
        }
        xml.append("</cols>");
    }

    private static void appendRow(StringBuilder xml, int rowNumber, List<?> values, boolean header) {
        xml.append("<row r=\"").append(rowNumber).append("\">");
        for (int i = 0; i < values.size(); i++) {
            Object value = values.get(i);
            if (value == null) {
                continue;
            }
            String reference = columnName(i) + rowNumber;
            if (value instanceof Number number) {
                xml.append("<c r=\"").append(reference).append("\"><v>")
                        .append(number instanceof BigDecimal decimal ? decimal.toPlainString() : number)
                        .append("</v></c>");
            } else {
                xml.append("<c r=\"").append(reference).append("\" t=\"inlineStr\"")
                        .append(header ? " s=\"1\"" : "").append("><is><t xml:space=\"preserve\">")
                        .append(escape(value.toString())).append("</t></is></c>");
            }
        }
        xml.append("</row>");
    }

    private static String columnName(int index) {
        StringBuilder name = new StringBuilder();
        int n = index + 1;
        while (n > 0) {
            int remainder = (n - 1) % 26;
            name.insert(0, (char) ('A' + remainder));
            n = (n - 1) / 26;
        }
        return name.toString();
    }

    private static String escape(String text) {
        StringBuilder out = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            switch (c) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '"' -> out.append("&quot;");
                default -> {
                    if (c >= 0x20 || c == '\t' || c == '\n' || c == '\r') {
                        out.append(c);
                    }
                }
            }
        }
        return out.toString();
    }
}
