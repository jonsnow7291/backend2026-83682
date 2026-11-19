package com.uniminuto.clinica.util;

import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Constructor sencillo de PDFs (encabezado de la clínica, párrafos, tablas) sobre OpenPDF.
 */
public class PdfBuilder {

    private static final Font TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private static final Font NEGRITA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font ENCABEZADO_TABLA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font CELDA = FontFactory.getFont(FontFactory.HELVETICA, 9);

    private final ByteArrayOutputStream salida = new ByteArrayOutputStream();
    private final Document documento;

    /**
     * Crea un PDF con encabezado de la clínica, título y fecha de generación.
     *
     * @param titulo      título del documento.
     * @param horizontal  {@code true} para orientación horizontal (reportes anchos).
     */
    public PdfBuilder(String titulo, boolean horizontal) {
        documento = new Document(horizontal ? PageSize.A4.rotate() : PageSize.A4, 36, 36, 36, 36);
        try {
            PdfWriter.getInstance(documento, salida);
            documento.open();
            Paragraph clinica = new Paragraph("CLÍNICA VETERINARIA", NEGRITA);
            clinica.setAlignment(Element.ALIGN_CENTER);
            documento.add(clinica);
            Paragraph t = new Paragraph(titulo, TITULO);
            t.setAlignment(Element.ALIGN_CENTER);
            t.setSpacingAfter(4);
            documento.add(t);
            Paragraph fecha = new Paragraph("Generado: "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), NORMAL);
            fecha.setAlignment(Element.ALIGN_CENTER);
            fecha.setSpacingAfter(10);
            documento.add(fecha);
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo crear el PDF", e);
        }
    }

    /**
     * Agrega una línea "Etiqueta: valor".
     *
     * @param etiqueta etiqueta en negrita.
     * @param valor    valor (se muestra "-" si es nulo).
     * @return este constructor.
     */
    public PdfBuilder campo(String etiqueta, Object valor) {
        Paragraph p = new Paragraph();
        p.add(new org.openpdf.text.Chunk(etiqueta + ": ", NEGRITA));
        p.add(new org.openpdf.text.Chunk(valor == null ? "-" : String.valueOf(valor), NORMAL));
        documento.add(p);
        return this;
    }

    /**
     * Agrega un subtítulo.
     *
     * @param texto texto del subtítulo.
     * @return este constructor.
     */
    public PdfBuilder seccion(String texto) {
        Paragraph p = new Paragraph(texto, NEGRITA);
        p.setSpacingBefore(10);
        p.setSpacingAfter(4);
        documento.add(p);
        return this;
    }

    /**
     * Agrega un párrafo de texto normal.
     *
     * @param texto contenido.
     * @return este constructor.
     */
    public PdfBuilder texto(String texto) {
        documento.add(new Paragraph(texto == null ? "-" : texto, NORMAL));
        return this;
    }

    /**
     * Agrega una tabla con encabezado.
     *
     * @param columnas encabezados.
     * @param filas    filas de datos (cada una con tantos valores como columnas).
     * @return este constructor.
     */
    public PdfBuilder tabla(List<String> columnas, List<List<Object>> filas) {
        PdfPTable tabla = new PdfPTable(columnas.size());
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(6);
        for (String c : columnas) {
            PdfPCell cell = new PdfPCell(new Phrase(c, ENCABEZADO_TABLA));
            cell.setBackgroundColor(new Color(40, 80, 120));
            cell.setPadding(4);
            tabla.addCell(cell);
        }
        for (List<Object> fila : filas) {
            for (Object v : fila) {
                PdfPCell cell = new PdfPCell(new Phrase(v == null ? "" : String.valueOf(v), CELDA));
                cell.setPadding(3);
                tabla.addCell(cell);
            }
        }
        documento.add(tabla);
        return this;
    }

    /**
     * Cierra el documento y devuelve los bytes del PDF.
     *
     * @return contenido del PDF.
     */
    public byte[] build() {
        documento.close();
        return salida.toByteArray();
    }
}
