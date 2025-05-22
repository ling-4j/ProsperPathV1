package com.mycompany.myapp.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.mycompany.myapp.domain.*;
import com.mycompany.myapp.domain.enumeration.TransactionType;
import com.mycompany.myapp.repository.TransactionRepository;
import com.mycompany.myapp.service.criteria.TransactionCriteria;
import jakarta.persistence.criteria.JoinType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.*;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;
import tech.jhipster.service.filter.InstantFilter;
import tech.jhipster.service.filter.LongFilter;

@Service
@Transactional(readOnly = true)
public class TransactionQueryService extends QueryService<Transaction> {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionQueryService.class);
    private final TransactionRepository transactionRepository;

    public TransactionQueryService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public Page<Transaction> findByCriteria(TransactionCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Transaction> specification = createSpecification(criteria);
        return transactionRepository.findAll(specification, page);
    }

    @Transactional(readOnly = true)
    public long countByCriteria(TransactionCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Transaction> specification = createSpecification(criteria);
        return transactionRepository.count(specification);
    }

    protected Specification<Transaction> createSpecification(TransactionCriteria criteria) {
        Specification<Transaction> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getDistinct() != null) specification = specification.and(distinct(criteria.getDistinct()));
            if (criteria.getId() != null) specification = specification.and(buildRangeSpecification(criteria.getId(), Transaction_.id));
            if (criteria.getAmount() != null) specification = specification.and(buildRangeSpecification(criteria.getAmount(), Transaction_.amount));
            if (criteria.getTransactionType() != null) specification = specification.and(buildSpecification(criteria.getTransactionType(), Transaction_.transactionType));
            if (criteria.getDescription() != null) specification = specification.and(buildStringSpecification(criteria.getDescription(), Transaction_.description));
            if (criteria.getTransactionDate() != null) specification = specification.and(buildRangeSpecification(criteria.getTransactionDate(), Transaction_.transactionDate));
            if (criteria.getCreatedAt() != null) specification = specification.and(buildRangeSpecification(criteria.getCreatedAt(), Transaction_.createdAt));
            if (criteria.getUpdatedAt() != null) specification = specification.and(buildRangeSpecification(criteria.getUpdatedAt(), Transaction_.updatedAt));
            if (criteria.getCategoryId() != null) specification = specification.and(buildSpecification(criteria.getCategoryId(), root -> root.join(Transaction_.category, JoinType.LEFT).get(Category_.id)));
            if (criteria.getUserId() != null) specification = specification.and(buildSpecification(criteria.getUserId(), root -> root.join(Transaction_.user, JoinType.LEFT).get(User_.id)));
        }
        return specification;
    }

    public List<Transaction> findByFilters(Long userId, Long category, LocalDate fromDate, LocalDate toDate, String type) {
        LOG.debug("Finding transactions for userId: {}, category: {}, fromDate: {}, toDate: {}, type: {}", userId, category, fromDate, toDate, type);
        if (userId == null) throw new IllegalArgumentException("userId cannot be null");
        TransactionCriteria criteria = new TransactionCriteria();
        criteria.setUserId(new LongFilter());
        criteria.getUserId().setEquals(userId);
        if (category != null) {
            criteria.setCategoryId(new LongFilter());
            criteria.getCategoryId().setEquals(category);
        }
        if (fromDate != null) {
            Instant fromInstant = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            criteria.setTransactionDate(new InstantFilter());
            criteria.getTransactionDate().setGreaterThanOrEqual(fromInstant);
        }
        if (toDate != null) {
            Instant toInstant = toDate.atStartOfDay(ZoneId.systemDefault()).plusDays(1).toInstant();
            criteria.setTransactionDate(new InstantFilter());
            criteria.getTransactionDate().setLessThanOrEqual(toInstant);
        }
        if (type != null) {
            try {
                TransactionType transactionType = TransactionType.valueOf(type.toUpperCase());
                criteria.setTransactionType(new TransactionCriteria.TransactionTypeFilter());
                criteria.getTransactionType().setEquals(transactionType);
            } catch (IllegalArgumentException e) {
                LOG.warn("Invalid transaction type: {}. Ignoring type filter.", type);
            }
        }
        Specification<Transaction> specification = createSpecification(criteria);
        return transactionRepository.findAll(specification);
    }

    public byte[] exportToExcel(Long userId, List<Transaction> transactions, String sort) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Transactions");
            List<Transaction> sortedTransactions = transactions.stream().sorted((t1, t2) -> {
                int compare = t1.getTransactionDate().compareTo(t2.getTransactionDate());
                return "transactionDate,asc".equalsIgnoreCase(sort) ? -compare : compare;
            }).collect(Collectors.toList());
            String headerText = "Thống kê giao dịch";
            String[] headers = { "DANH MỤC", "LOẠI", "MÔ TẢ", "NGÀY", "SỐ TIỀN" };
            Row headerRow = sheet.createRow(0);
            Cell mergedCell = headerRow.createCell(0);
            mergedCell.setCellValue(headerText);
            CellStyle mergedStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font mergedFont = workbook.createFont();
            mergedFont.setBold(true);
            mergedFont.setColor(IndexedColors.RED.getIndex());
            mergedFont.setFontHeightInPoints((short) 18);
            mergedStyle.setFont(mergedFont);
            mergedStyle.setAlignment(HorizontalAlignment.CENTER);
            mergedCell.setCellStyle(mergedStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
            Row columnHeaderRow = sheet.createRow(1);
            CellStyle headerStyle = createHeaderCellStyle(workbook);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = columnHeaderRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            CellStyle amountStyle = createAmountCellStyle(workbook);
            CellStyle amountStyleRed = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font fontRed = workbook.createFont();
            fontRed.setBold(true);
            fontRed.setColor(IndexedColors.RED.getIndex());
            fontRed.setFontHeightInPoints((short) 12);
            amountStyleRed.setFont(fontRed);
            amountStyleRed.setDataFormat(workbook.createDataFormat().getFormat("#,##0 \"VND\""));
            amountStyleRed.setAlignment(HorizontalAlignment.RIGHT);
            amountStyle.setAlignment(HorizontalAlignment.RIGHT);
            CellStyle dateStyle = createDateCellStyle(workbook);
            int rowIdx = 2;
            double totalAmount = 0;
            for (Transaction transaction : sortedTransactions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(transaction.getCategory() != null ? transaction.getCategory().getCategoryName() : "");
                row.createCell(1).setCellValue(translateTransactionTypeToVietnamese(transaction.getTransactionType()));
                row.createCell(2).setCellValue(transaction.getDescription());
                // Chuyển đổi ngày giờ từ UTC sang Asia/Ho_Chi_Minh cho Excel
                ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");
                String dateString = transaction.getTransactionDate()
                    .atZone(ZoneId.of("UTC"))
                    .withZoneSameInstant(zoneId)
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                Cell dateCell = row.createCell(3);
                dateCell.setCellValue(dateString);
                dateCell.setCellStyle(dateStyle);
                Cell amountCell = row.createCell(4);
                double amount = transaction.getAmount().doubleValue();
                if (transaction.getTransactionType() == TransactionType.INCOME) {
                    amountCell.setCellValue(amount);
                    amountCell.setCellStyle(amountStyle);
                    totalAmount += amount;
                } else {
                    amountCell.setCellValue(-amount);
                    amountCell.setCellStyle(amountStyleRed);
                    totalAmount -= amount;
                }
            }
            Row totalRow = sheet.createRow(rowIdx);
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("Tổng");
            
            CellStyle totalLabelStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font totalLabelFont = workbook.createFont();
            totalLabelFont.setBold(true);
            totalLabelFont.setFontHeightInPoints((short) 13);
            totalLabelStyle.setFont(totalLabelFont);
            totalLabelStyle.setAlignment(HorizontalAlignment.CENTER);
            totalLabelCell.setCellStyle(totalLabelStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 3));
            for (int i = 1; i <= 3; i++) totalRow.createCell(i);
            Cell totalAmountCell = totalRow.createCell(4);
            totalAmountCell.setCellValue(Math.abs(totalAmount));
            if (totalAmount > 0) {
                totalAmountCell.setCellStyle(amountStyle);
            } else if (totalAmount < 0) {
                totalAmountCell.setCellStyle(amountStyleRed);
            } else {
                totalAmountCell.setCellStyle(amountStyle);
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to export data to Excel", e);
        }
    }

    public byte[] exportToPDF(Long userId, List<Transaction> transactions, String sort) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();
            BaseFont baseFont = BaseFont.createFont("fonts/Roboto-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(baseFont, 18, com.itextpdf.text.Font.BOLD, BaseColor.RED);
            com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(baseFont, 12, com.itextpdf.text.Font.NORMAL);
            com.itextpdf.text.Font dataFont = new com.itextpdf.text.Font(baseFont, 10, com.itextpdf.text.Font.NORMAL);
            com.itextpdf.text.Font incomeFont = new com.itextpdf.text.Font(baseFont, 10, com.itextpdf.text.Font.BOLD, BaseColor.BLUE);
            com.itextpdf.text.Font expenseFont = new com.itextpdf.text.Font(baseFont, 10, com.itextpdf.text.Font.BOLD, BaseColor.RED);
            com.itextpdf.text.Font totalFont = new com.itextpdf.text.Font(baseFont, 12, com.itextpdf.text.Font.BOLD, BaseColor.BLUE);
            Paragraph title = new Paragraph("Thống kê giao dịch", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);
            List<Transaction> sortedTransactions = transactions.stream().sorted((t1, t2) -> {
                int compare = t1.getTransactionDate().compareTo(t2.getTransactionDate());
                return "transactionDate,desc".equalsIgnoreCase(sort) ? -compare : compare;
            }).collect(Collectors.toList());
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new int[] { 2, 2, 4, 3, 3 });
            String[] headers = { "DANH MỤC", "LOẠI", "MÔ TẢ", "NGÀY", "SỐ TIỀN" };
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }
            double totalAmount = 0;
            for (Transaction transaction : sortedTransactions) {
                table.addCell(new Phrase(transaction.getCategory() != null ? transaction.getCategory().getCategoryName() : "", dataFont));
                PdfPCell typeCell = new PdfPCell(new Phrase(translateTransactionTypeToVietnamese(transaction.getTransactionType()), dataFont));
                typeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(typeCell);
                table.addCell(new Phrase(transaction.getDescription() != null ? transaction.getDescription() : "", dataFont));
                // Chuyển đổi ngày giờ từ UTC sang Asia/Ho_Chi_Minh
                ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");
                String dateString = transaction.getTransactionDate()
                    .atZone(ZoneId.of("UTC"))
                    .withZoneSameInstant(zoneId)
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                PdfPCell dateCell = new PdfPCell(new Phrase(dateString, dataFont));
                dateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(dateCell);
                double amount = transaction.getAmount() != null ? transaction.getAmount().doubleValue() : 0;
                String amountStr;
                com.itextpdf.text.Font amountFont;
                if (transaction.getTransactionType() == TransactionType.INCOME) {
                    amountStr = String.format("%,.0f VND", amount);
                    amountFont = incomeFont;
                    totalAmount += amount;
                } else {
                    amountStr = String.format("-%,.0f VND", amount);
                    amountFont = expenseFont;
                    totalAmount -= amount;
                }
                PdfPCell amountCell = new PdfPCell(new Phrase(amountStr, amountFont));
                amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(amountCell);
            }
            PdfPCell totalLabelCell = new PdfPCell(new Phrase("Tổng", headerFont));
            totalLabelCell.setColspan(4);
            totalLabelCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            totalLabelCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(totalLabelCell);
            String totalStr = String.format("%,.0f VND", totalAmount);
            if (totalAmount < 0) {
                totalStr = "-" + totalStr.replace("-", "");
            }
            PdfPCell totalAmountCell = new PdfPCell(new Phrase(totalStr, totalFont));
            totalAmountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalAmountCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(totalAmountCell);
            document.add(table);
            document.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export data to PDF", e);
        }
    }

    private String translateTransactionTypeToVietnamese(TransactionType transactionType) {
        switch (transactionType) {
            case INCOME:
                return "Thu nhập";
            case EXPENSE:
                return "Chi tiêu";
            default:
                return transactionType.toString();
        }
    }

    private CellStyle createHeaderCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 13);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createDateCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy HH:mm:ss"));
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }

    private CellStyle createAmountCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.BLUE.getIndex());
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,## \"VND\""));
        return style;
    }
}
