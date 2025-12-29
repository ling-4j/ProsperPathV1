package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.Transaction;
import com.mycompany.myapp.domain.enumeration.TransactionType;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class TransactionImportParser {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter VCB_DATE =
        DateTimeFormatter.ofPattern("d/M/yyyy");

    // ================= ENTRY =================

    public List<Transaction> parse(MultipartFile file) {
        try (
            InputStream is = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(is)
        ) {
            Sheet sheet = workbook.getSheetAt(0);
            HeaderAnchor anchor = findHeaderAnchor(sheet);
            return parseData(sheet, anchor);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse bank statement file", e);
        }
    }

    // ================= HEADER =================

    private HeaderAnchor findHeaderAnchor(Sheet sheet) {
        for (Row row : sheet) {
            for (Cell cell : row) {
                String v = normalize(getString(cell));
                if (v.contains("stt")) {
                    return new HeaderAnchor(row.getRowNum(), cell.getColumnIndex());
                }
            }
        }
        throw new RuntimeException("Cannot find STT column");
    }

    // ================= DATA =================

    private List<Transaction> parseData(Sheet sheet, HeaderAnchor h) {
        List<Transaction> list = new ArrayList<>();

        int dateCol = h.sttCol + 1;
        int debitCol = h.sttCol + 2;
        int creditCol = h.sttCol + 3;
        int descCol = h.sttCol + 5;

        for (int i = h.headerRow + 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Transaction tx = parseRow(row, dateCol, debitCol, creditCol, descCol);
            if (tx != null) {
                list.add(tx);
            }
        }

        return list;
    }

    private Transaction parseRow(
        Row row,
        int dateCol,
        int debitCol,
        int creditCol,
        int descCol
    ) {
        Instant date = parseVcbDate(row.getCell(dateCol));
        if (date == null) return null;

        BigDecimal debit = parseMoney(row.getCell(debitCol));
        BigDecimal credit = parseMoney(row.getCell(creditCol));
        if (debit == null && credit == null) return null;

        Transaction tx = new Transaction();
        tx.setTransactionDate(date);
        tx.setDescription(getString(row.getCell(descCol)));

        if (debit != null && debit.compareTo(BigDecimal.ZERO) > 0) {
            tx.setAmount(debit);
            tx.setTransactionType(TransactionType.EXPENSE);
        } else {
            tx.setAmount(credit);
            tx.setTransactionType(TransactionType.INCOME);
        }

        return tx;
    }

    // ================= PARSERS =================

    // "25/12/2025\n5424 - 39690"
    private Instant parseVcbDate(Cell cell) {
        if (cell == null) return null;

        String raw = getString(cell);
        if (raw.isEmpty()) return null;

        String firstLine = raw.split("\\R")[0].trim();
        try {
            LocalDate date = LocalDate.parse(firstLine, VCB_DATE);
            return date.atStartOfDay(VN_ZONE).toInstant();
        } catch (Exception e) {
            return null;
        }
    }

    // "900,000"
    private BigDecimal parseMoney(Cell cell) {
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }

        if (cell.getCellType() == CellType.STRING) {
            String v = cell.getStringCellValue()
                .replace(",", "")
                .trim();
            if (v.isEmpty()) return null;
            return new BigDecimal(v);
        }

        return null;
    }

    private String getString(Cell cell) {
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    private String normalize(String s) {
        return s.toLowerCase()
            .replace("\n", " ")
            .replace(".", "")
            .replace("/", " ")
            .replaceAll("\\s+", " ")
            .trim();
    }

    // ================= INNER =================

    private static class HeaderAnchor {
        final int headerRow;
        final int sttCol;

        HeaderAnchor(int headerRow, int sttCol) {
            this.headerRow = headerRow;
            this.sttCol = sttCol;
        }
    }
}
