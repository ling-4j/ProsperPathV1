package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.Transaction;
import com.mycompany.myapp.domain.enumeration.TransactionType;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class TransactionImportParser {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d/M/yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("d/M/yyyy[ HH:mm]");

    // ================= ENTRY =================
    public List<Transaction> parse(MultipartFile file) {
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            HeaderAnchor header = findHeader(sheet);
            return parseSheet(sheet, header);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse bank statement file", e);
        }
    }

    // ================= HEADER =================
    private HeaderAnchor findHeader(Sheet sheet) {
        for (Row row : sheet) {
            Integer dateCol = null, debitCol = null, creditCol = null, descCol = null;

            for (Cell cell : row) {
                String text = normalize(getString(cell));

                if (text.contains("transaction date") || text.contains("tnx date") || text.contains("doc no")) dateCol =
                    cell.getColumnIndex();

                if (text.contains("debit") || text.contains("ghi nợ") || text.contains("phát sinh nợ")) debitCol = cell.getColumnIndex();

                if (text.contains("credit") || text.contains("ghi có") || text.contains("phát sinh có")) creditCol = cell.getColumnIndex();

                if (text.contains("nội dung") || text.contains("description") || text.contains("transactions in detail")) descCol =
                    cell.getColumnIndex();
            }

            if (dateCol != null && debitCol != null && creditCol != null && descCol != null) {
                return new HeaderAnchor(row.getRowNum(), dateCol, debitCol, creditCol, descCol);
            }
        }
        throw new RuntimeException("Cannot find required header columns");
    }

    // ================= DATA =================
    private List<Transaction> parseSheet(Sheet sheet, HeaderAnchor header) {
        List<Transaction> transactions = new ArrayList<>();
        for (int i = header.headerRow + 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Transaction tx = parseRow(row, header);
            if (tx != null) transactions.add(tx);
        }
        return transactions;
    }

    private Transaction parseRow(Row row, HeaderAnchor h) {
        Instant date = parseDate(row.getCell(h.dateCol));
        if (date == null) return null;

        BigDecimal debit = parseMoney(row.getCell(h.debitCol));
        BigDecimal credit = parseMoney(row.getCell(h.creditCol));
        if (debit == null && credit == null) return null;

        Transaction tx = new Transaction();
        tx.setTransactionDate(date);
        tx.setDescription(getString(row.getCell(h.descCol)));

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
    private Instant parseDate(Cell cell) {
        if (cell == null) return null;

        String raw = getString(cell).trim();
        if (raw.isEmpty()) return null;

        String firstLine = raw.split("\\R")[0].trim();

        try {
            LocalDateTime dt = LocalDateTime.parse(firstLine, DATE_TIME);
            return dt.atZone(VN_ZONE).toInstant();
        } catch (Exception e) {
            try {
                LocalDate date = LocalDate.parse(firstLine, DATE);
                return date.atStartOfDay(VN_ZONE).toInstant();
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private BigDecimal parseMoney(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case NUMERIC:
                return BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING:
                String text = cell.getStringCellValue().replace(",", "").trim();
                if (text.isEmpty()) return null;
                try {
                    return new BigDecimal(text);
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }

    private String getString(Cell cell) {
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    private String normalize(String s) {
        return s.toLowerCase().replace("\n", " ").replace(".", "").replace("/", " ").replaceAll("\\s+", " ").trim();
    }

    // ================= INNER CLASS =================
    private static class HeaderAnchor {

        final int headerRow;
        final int dateCol;
        final int debitCol;
        final int creditCol;
        final int descCol;

        HeaderAnchor(int headerRow, int dateCol, int debitCol, int creditCol, int descCol) {
            this.headerRow = headerRow;
            this.dateCol = dateCol;
            this.debitCol = debitCol;
            this.creditCol = creditCol;
            this.descCol = descCol;
        }
    }
}
