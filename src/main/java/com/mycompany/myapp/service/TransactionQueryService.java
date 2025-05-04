package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*; // for static metamodels
import com.mycompany.myapp.domain.enumeration.TransactionType;
import com.mycompany.myapp.repository.TransactionRepository;
import com.mycompany.myapp.service.criteria.TransactionCriteria;
import jakarta.persistence.criteria.JoinType;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

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
                specification = specification
                        .and(buildSpecification(criteria.getTransactionType(), Transaction_.transactionType));
            }
            if (criteria.getDescription() != null) {
                specification = specification
                        .and(buildStringSpecification(criteria.getDescription(), Transaction_.description));
            }
            if (criteria.getTransactionDate() != null) {
                specification = specification
                        .and(buildRangeSpecification(criteria.getTransactionDate(), Transaction_.transactionDate));
            }
            if (criteria.getCreatedAt() != null) {
                specification = specification
                        .and(buildRangeSpecification(criteria.getCreatedAt(), Transaction_.createdAt));
            }
            if (criteria.getUpdatedAt() != null) {
                specification = specification
                        .and(buildRangeSpecification(criteria.getUpdatedAt(), Transaction_.updatedAt));
            }
            if (criteria.getCategoryId() != null) {
                specification = specification.and(
                        buildSpecification(criteria.getCategoryId(),
                                root -> root.join(Transaction_.category, JoinType.LEFT).get(Category_.id)));
            }
            if (criteria.getUserId() != null) {
                specification = specification.and(
                        buildSpecification(criteria.getUserId(),
                                root -> root.join(Transaction_.user, JoinType.LEFT).get(User_.id)));
            }
        }
        return specification;
    }

    /**
     * Filter transactions based on the search parameters and return the matching
     * entities.
     * 
     * @param category The category ID to filter by.
     * @param fromDate The start date to filter by.
     * @param toDate   The end date to filter by.
     * @param type     The transaction type to filter by.
     * @return the list of matching transactions.
     */
    public List<Transaction> findByFilters(Long category, LocalDate fromDate, LocalDate toDate, String type) {
        Specification<Transaction> specification = Specification.where(null);

        if (category != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("category").get("id"), category));
        }
        if (fromDate != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .greaterThanOrEqualTo(root.get("transactionDate"), fromDate));
        }
        if (toDate != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .lessThanOrEqualTo(root.get("transactionDate"), toDate));
        }
        if (type != null) {
            specification = specification
                    .and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("transactionType"), type));
        }

        return transactionRepository.findAll(specification);
    }

    /**
     * Export transactions to an Excel file.
     * 
     * @param transactions The list of transactions to export.
     * @return the byte array representing the Excel file.
     */
    public byte[] exportToExcel(List<Transaction> transactions) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Transactions");

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
            for (Transaction transaction : transactions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(
                        transaction.getCategory() != null ? transaction.getCategory().getCategoryName() : "");
                row.createCell(1).setCellValue(translateTransactionTypeToVietnamese(transaction.getTransactionType()));
                row.createCell(2).setCellValue(transaction.getDescription());
                Cell dateCell = row.createCell(3);
                dateCell.setCellValue(transaction.getTransactionDate().atZone(java.time.ZoneId.systemDefault())
                        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
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
     * @param transactions The list of transactions to export.
     * @return the byte array representing the PDF file.
     */
    public byte[] exportToPDF(List<Transaction> transactions) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Add title
            com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.RED);
            Paragraph title = new Paragraph("Thống kê giao dịch", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(Chunk.NEWLINE);

            // Create table
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new int[] { 2, 2, 4, 3, 3 });

            // Add table headers
            com.itextpdf.text.Font headerFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12);
            String[] headers = { "DANH MỤC", "LOẠI", "MÔ TẢ", "NGÀY", "SỐ TIỀN" };
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }

            // Add table data
            com.itextpdf.text.Font dataFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 10);
            for (Transaction transaction : transactions) {
                // Cột DANH MỤC (mặc định là căn trái)
                table.addCell(new Phrase(transaction.getCategory() != null ? transaction.getCategory().getCategoryName() : "", dataFont));
                
                // Cột LOẠI (căn giữa)
                PdfPCell typeCell = new PdfPCell(new Phrase(translateTransactionTypeToVietnamese(transaction.getTransactionType()), dataFont));
                typeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(typeCell);
            
                // Cột MÔ TẢ (mặc định là căn trái)
                table.addCell(new Phrase(transaction.getDescription(), dataFont));
            
                // Cột NGÀY (căn giữa)
                String dateString = transaction.getTransactionDate()
                                               .atZone(java.time.ZoneId.systemDefault())
                                               .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                PdfPCell dateCell = new PdfPCell(new Phrase(dateString, dataFont));
                dateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(dateCell);
            
                // Cột SỐ TIỀN (căn phải)
                PdfPCell amountCell = new PdfPCell(new Phrase(String.format("%,.0f VND", transaction.getAmount()), dataFont));
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
