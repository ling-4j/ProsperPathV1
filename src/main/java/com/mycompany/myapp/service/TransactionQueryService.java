package com.mycompany.myapp.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.mycompany.myapp.domain.*; // for static metamodels
import com.mycompany.myapp.domain.enumeration.TransactionType;
import com.mycompany.myapp.repository.TransactionRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.service.criteria.TransactionCriteria;
import jakarta.persistence.criteria.JoinType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
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

/**
 * Service for executing complex queries for {@link Transaction} entities in the
 * database.
 * The main input is a {@link TransactionCriteria} which gets converted to
 * {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link Transaction} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class TransactionQueryService extends QueryService<Transaction> {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionQueryService.class);

    private final TransactionRepository transactionRepository;

    public TransactionQueryService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Return a {@link Page} of {@link Transaction} which matches the criteria from
     * the database.
     *
     * @param criteria The object which holds all the filters, which the entities
     *                 should match.
     * @param page     The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<Transaction> findByCriteria(TransactionCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Transaction> specification = createSpecification(criteria);
        return transactionRepository.findAll(specification, page);
    }

    /**
     * Return the number of matching entities in the database.
     *
     * @param criteria The object which holds all the filters, which the entities
     *                 should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(TransactionCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Transaction> specification = createSpecification(criteria);
        return transactionRepository.count(specification);
    }

    /**
     * Function to convert {@link TransactionCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities
     *                 should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Transaction> createSpecification(TransactionCriteria criteria) {
        Specification<Transaction> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Transaction_.id));
            }
            if (criteria.getAmount() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getAmount(), Transaction_.amount));
            }
            if (criteria.getTransactionType() != null) {
                specification = specification.and(buildSpecification(criteria.getTransactionType(), Transaction_.transactionType));
            }
            if (criteria.getDescription() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDescription(), Transaction_.description));
            }
            if (criteria.getTransactionDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getTransactionDate(), Transaction_.transactionDate));
            }
            if (criteria.getCreatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedAt(), Transaction_.createdAt));
            }
            if (criteria.getUpdatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getUpdatedAt(), Transaction_.updatedAt));
            }
            if (criteria.getCategoryId() != null) {
                specification = specification.and(
                    buildSpecification(criteria.getCategoryId(), root -> root.join(Transaction_.category, JoinType.LEFT).get(Category_.id))
                );
            }
            if (criteria.getUserId() != null) {
                specification = specification.and(
                    buildSpecification(criteria.getUserId(), root -> root.join(Transaction_.user, JoinType.LEFT).get(User_.id))
                );
            }
        }
        return specification;
    }

    /**
     * Find transactions by filters for a specific user.
     *
     * @param userId   The ID of the user whose transactions are being queried (required).
     * @param category The category ID to filter by.
     * @param fromDate The start date to filter by.
     * @param toDate   The end date to filter by.
     * @param type     The transaction type to filter by.
     * @return the list of matching transactions.
     */
    public List<Transaction> findByFilters(Long userId, Long category, LocalDate fromDate, LocalDate toDate, String type) {
        LOG.debug(
            "Finding transactions for userId: {}, category: {}, fromDate: {}, toDate: {}, type: {}",
            userId,
            category,
            fromDate,
            toDate,
            type
        );

        // Validate userId
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }

        // Create TransactionCriteria instance
        TransactionCriteria criteria = new TransactionCriteria();

        // Set mandatory userId filter
        criteria.setUserId(new LongFilter());
        criteria.getUserId().setEquals(userId);

        // Set category filter if provided
        if (category != null) {
            criteria.setCategoryId(new LongFilter());
            criteria.getCategoryId().setEquals(category);
        }

        // Set fromDate filter if provided
        if (fromDate != null) {
            Instant fromInstant = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            criteria.setTransactionDate(new InstantFilter());
            criteria.getTransactionDate().setGreaterThanOrEqual(fromInstant);
        }

        // Set toDate filter if provided
        if (toDate != null) {
            Instant toInstant = toDate.atStartOfDay(ZoneId.systemDefault()).plusDays(1).toInstant();
            criteria.setTransactionDate(new InstantFilter());
            criteria.getTransactionDate().setLessThanOrEqual(toInstant);
        }

        if (type != null) {
            try {
                TransactionType transactionType = TransactionType.valueOf(type.toUpperCase()); // Chuyển String thành TransactionType
                criteria.setTransactionType(new TransactionCriteria.TransactionTypeFilter());
                criteria.getTransactionType().setEquals(transactionType);
            } catch (IllegalArgumentException e) {
                LOG.warn("Invalid transaction type: {}. Ignoring type filter.", type);
                // Bỏ qua bộ lọc type nếu không hợp lệ, hoặc ném ngoại lệ tùy ý
            }
        }

        // Build specification and fetch data
        Specification<Transaction> specification = createSpecification(criteria);
        return transactionRepository.findAll(specification);
    }

    /**
     * Export transactions to an Excel file.
     *
     * @param transactions The list of transactions to export.
     * @return the byte array representing the Excel file.
     */
    public byte[] exportToExcel(Long userId, List<Transaction> transactions, String sort) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Transactions");

            List<Transaction> sortedTransactions = transactions
                .stream()
                .sorted((t1, t2) -> {
                    int compare = t1.getTransactionDate().compareTo(t2.getTransactionDate());
                    return "transactionDate,asc".equalsIgnoreCase(sort) ? -compare : compare;
                })
                .collect(Collectors.toList());
            // Set header text in Vietnamese
            String headerText = "Thống kê giao dịch";
            String[] headers = { "DANH MỤC", "LOẠI", "MÔ TẢ", "NGÀY", "SỐ TIỀN" };

            // Create merged header row
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

            // Create header row
            Row columnHeaderRow = sheet.createRow(1);
            CellStyle headerStyle = createHeaderCellStyle(workbook);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = columnHeaderRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            CellStyle amountStyle = createAmountCellStyle(workbook);
            CellStyle dateStyle = createDateCellStyle(workbook);
            int rowIdx = 2;
            for (Transaction transaction : sortedTransactions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(transaction.getCategory() != null ? transaction.getCategory().getCategoryName() : "");
                row.createCell(1).setCellValue(translateTransactionTypeToVietnamese(transaction.getTransactionType()));
                row.createCell(2).setCellValue(transaction.getDescription());
                Cell dateCell = row.createCell(3);
                dateCell.setCellValue(
                    transaction
                        .getTransactionDate()
                        .atZone(java.time.ZoneId.systemDefault())
                        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                );
                dateCell.setCellStyle(dateStyle);
                Cell amountCell = row.createCell(4);
                amountCell.setCellValue(transaction.getAmount().doubleValue());
                amountCell.setCellStyle(amountStyle);
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to export data to Excel", e);
        }
    }

    /**
     * Export transactions to a PDF file.
     *
     * @param userId       The ID of the user whose transactions are being exported.
     * @param transactions The list of transactions to export.
     * @param sort         The sort parameter (e.g., "transactionDate,asc" or
     *                     "transactionDate,desc").
     * @return the byte array representing the PDF file.
     */
    public byte[] exportToPDF(Long userId, List<Transaction> transactions, String sort) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Load Roboto font or fallback to a font supporting Vietnamese
            BaseFont baseFont = BaseFont.createFont("fonts/Roboto-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(baseFont, 18, com.itextpdf.text.Font.BOLD, BaseColor.RED);
            com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(baseFont, 12, com.itextpdf.text.Font.NORMAL);
            com.itextpdf.text.Font dataFont = new com.itextpdf.text.Font(baseFont, 10, com.itextpdf.text.Font.NORMAL);

            // Add title
            Paragraph title = new Paragraph("Thống kê giao dịch", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(Chunk.NEWLINE);

            // Sort transactions based on sort parameter
            List<Transaction> sortedTransactions = transactions
                .stream()
                .sorted((t1, t2) -> {
                    int compare = t1.getTransactionDate().compareTo(t2.getTransactionDate());
                    return "transactionDate,desc".equalsIgnoreCase(sort) ? -compare : compare;
                })
                .collect(Collectors.toList());

            // Create table
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new int[] { 2, 2, 4, 3, 3 });

            // Add table headers
            String[] headers = { "DANH MỤC", "LOẠI", "MÔ TẢ", "NGÀY", "SỐ TIỀN" };
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }

            // Add table data
            for (Transaction transaction : sortedTransactions) {
                // Cột DANH MỤC (mặc định là căn trái)
                table.addCell(new Phrase(transaction.getCategory() != null ? transaction.getCategory().getCategoryName() : "", dataFont));

                // Cột LOẠI (căn giữa)
                PdfPCell typeCell = new PdfPCell(
                    new Phrase(translateTransactionTypeToVietnamese(transaction.getTransactionType()), dataFont)
                );
                typeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(typeCell);

                // Cột MÔ TẢ (mặc định là căn trái)
                table.addCell(new Phrase(transaction.getDescription() != null ? transaction.getDescription() : "", dataFont));

                // Cột NGÀY (căn giữa)
                String dateString = transaction
                    .getTransactionDate()
                    .atZone(java.time.ZoneId.systemDefault())
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                PdfPCell dateCell = new PdfPCell(new Phrase(dateString, dataFont));
                dateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(dateCell);

                // Cột SỐ TIỀN (căn phải)
                PdfPCell amountCell = new PdfPCell(
                    new Phrase(String.format("%,.0f VND", transaction.getAmount() != null ? transaction.getAmount() : 0), dataFont)
                );
                amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(amountCell);
            }

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
