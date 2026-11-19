package com.uniminuto.clinica.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utilidades para generar archivos Excel (.xlsx) y leer archivos .xlsx/.csv de carga masiva.
 */
public final class ExcelUtil {

    private ExcelUtil() {
    }

    /**
     * Genera un libro Excel de una hoja con título, encabezados, filas y totales.
     *
     * @param hoja      nombre de la hoja.
     * @param titulo    título mostrado en la primera fila (nulo: la hoja empieza directamente con los encabezados).
     * @param columnas  encabezados.
     * @param filas     filas de datos.
     * @param totales   pares etiqueta/valor que se escriben al final (puede ser nulo).
     * @return bytes del .xlsx.
     */
    public static byte[] generar(String hoja, String titulo, List<String> columnas,
                                 List<List<Object>> filas, Map<String, Object> totales) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet(hoja.length() > 30 ? hoja.substring(0, 30) : hoja);
            Font negrita = wb.createFont();
            negrita.setBold(true);
            CellStyle estilo = wb.createCellStyle();
            estilo.setFont(negrita);
            int r = 0;
            if (titulo != null) {
                Row t = sheet.createRow(r++);
                Cell ct = t.createCell(0);
                ct.setCellValue(titulo);
                ct.setCellStyle(estilo);
                sheet.createRow(r++).createCell(0).setCellValue("Generado: " + LocalDateTime.now().withNano(0));
                r++;
            }
            Row h = sheet.createRow(r++);
            for (int i = 0; i < columnas.size(); i++) {
                Cell c = h.createCell(i);
                c.setCellValue(columnas.get(i));
                c.setCellStyle(estilo);
            }
            for (List<Object> fila : filas) {
                Row row = sheet.createRow(r++);
                for (int i = 0; i < fila.size(); i++) {
                    escribir(row.createCell(i), fila.get(i));
                }
            }
            if (totales != null && !totales.isEmpty()) {
                r++;
                for (Map.Entry<String, Object> e : totales.entrySet()) {
                    Row row = sheet.createRow(r++);
                    Cell k = row.createCell(0);
                    k.setCellValue(e.getKey());
                    k.setCellStyle(estilo);
                    escribir(row.createCell(1), e.getValue());
                }
            }
            for (int i = 0; i < columnas.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar el Excel", e);
        }
    }

    private static void escribir(Cell cell, Object v) {
        if (v == null) {
            cell.setBlank();
        } else if (v instanceof Number n) {
            cell.setCellValue(n.doubleValue());
        } else if (v instanceof BigDecimal bd) {
            cell.setCellValue(bd.doubleValue());
        } else {
            cell.setCellValue(String.valueOf(v));
        }
    }

    /**
     * Lee un archivo .xlsx o .csv y devuelve sus filas como texto. La primera fila son los encabezados.
     *
     * @param nombre nombre del archivo (define el formato por extensión).
     * @param in     contenido.
     * @return lista de filas (cada una lista de celdas como texto); incluye la fila de encabezados.
     * @throws IOException si el archivo no se puede leer.
     */
    public static List<List<String>> leer(String nombre, InputStream in) throws IOException {
        String n = nombre == null ? "" : nombre.toLowerCase();
        if (n.endsWith(".xlsx")) {
            return leerXlsx(in);
        }
        if (n.endsWith(".csv")) {
            return leerCsv(in);
        }
        throw new IllegalArgumentException("Formato no soportado: use .xlsx o .csv");
    }

    private static List<List<String>> leerXlsx(InputStream in) throws IOException {
        List<List<String>> filas = new ArrayList<>();
        DataFormatter fmt = new DataFormatter();
        try (Workbook wb = new XSSFWorkbook(in)) {
            Sheet sheet = wb.getSheetAt(0);
            int ancho = sheet.getRow(0) == null ? 0 : sheet.getRow(0).getLastCellNum();
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                List<String> fila = new ArrayList<>();
                for (int c = 0; c < ancho; c++) {
                    Cell cell = row == null ? null : row.getCell(c);
                    if (cell == null) {
                        fila.add("");
                    } else if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC
                            && DateUtil.isCellDateFormatted(cell)) {
                        fila.add(cell.getLocalDateTimeCellValue().toLocalDate().toString());
                    } else {
                        fila.add(fmt.formatCellValue(cell).trim());
                    }
                }
                filas.add(fila);
            }
        }
        return filas;
    }

    private static List<List<String>> leerCsv(InputStream in) throws IOException {
        List<List<String>> filas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String linea;
            boolean primera = true;
            char sep = ',';
            while ((linea = br.readLine()) != null) {
                if (primera) {
                    linea = linea.replace("﻿", "");
                    sep = linea.chars().filter(ch -> ch == ';').count() > linea.chars().filter(ch -> ch == ',').count() ? ';' : ',';
                    primera = false;
                }
                filas.add(partirCsv(linea, sep));
            }
        }
        return filas;
    }

    private static List<String> partirCsv(String linea, char sep) {
        List<String> campos = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean comillas = false;
        for (int i = 0; i < linea.length(); i++) {
            char ch = linea.charAt(i);
            if (ch == '"') {
                if (comillas && i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    comillas = !comillas;
                }
            } else if (ch == sep && !comillas) {
                campos.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(ch);
            }
        }
        campos.add(sb.toString().trim());
        return campos;
    }

    /**
     * Interpreta un texto como fecha ISO ({@code yyyy-MM-dd}).
     *
     * @param texto texto de la celda.
     * @return la fecha.
     */
    public static LocalDate fecha(String texto) {
        return LocalDate.parse(texto.trim());
    }
}
