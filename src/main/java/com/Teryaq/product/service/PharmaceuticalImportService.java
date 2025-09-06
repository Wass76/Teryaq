package com.Teryaq.product.service;

import com.Teryaq.product.dto.ImportResponse;
import com.Teryaq.product.dto.MProductDTORequest;
import com.Teryaq.product.entity.Form;
import com.Teryaq.product.entity.Manufacturer;
import com.Teryaq.product.entity.MasterProduct;
import com.Teryaq.product.repo.FormRepo;
import com.Teryaq.product.repo.ManufacturerRepo;
import com.Teryaq.product.repo.MasterProductRepo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Service for processing pharmaceutical data imports using existing entities
 * يستخدم الكيانات الموجودة: Manufacturer, Form, MasterProduct
 */
@Service
public class PharmaceuticalImportService {

    private static final Logger logger = LoggerFactory.getLogger(PharmaceuticalImportService.class);

    @Autowired
    private ManufacturerRepo manufacturerRepository;

    @Autowired
    private FormRepo formRepository;

    @Autowired
    private MasterProductRepo masterProductRepository;

    @Value("${pharmaceutical.python.script.path:/opt/scripts/}")
    private String pythonScriptPath;

    @Value("${pharmaceutical.temp.dir:/tmp/pharmaceutical/}")
    private String tempDir;

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String databaseUsername;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Process pharmaceutical Excel file and import data to existing tables
     */
    public ImportResponse processPharmaceuticalFile(MultipartFile file) throws IOException {
        logger.info("بدء معالجة ملف البيانات الصيدلانية / Starting pharmaceutical file processing: {}", file.getOriginalFilename());

        // Create temp directory if not exists
        Path tempDirPath = Paths.get(tempDir);
        if (!Files.exists(tempDirPath)) {
            Files.createDirectories(tempDirPath);
        }

        // Save uploaded file temporarily
        String tempFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path tempFilePath = tempDirPath.resolve(tempFileName);
        file.transferTo(tempFilePath.toFile());

        try {
            // Call Python script to process the file with database connection
            List<MProductDTORequest> products = callPythonScriptWithDatabase(tempFilePath.toString());

            if (products == null || products.isEmpty()) {
                return new ImportResponse(false, "لم يتم العثور على بيانات صالحة في الملف / No valid data found in file", 0, null);
            }

            // Process each product through existing repositories
            int successCount = 0;
            int failureCount = 0;
            StringBuilder errors = new StringBuilder();

            for (MProductDTORequest productRequest : products) {
                try {
                    // Create MasterProduct entity from request
                    MasterProduct masterProduct = createMasterProductFromRequest(productRequest);

                    // Save to database using existing repository
                    masterProductRepository.save(masterProduct);
                    successCount++;

                } catch (Exception e) {
                    failureCount++;
                    errors.append("خطأ في إضافة المنتج / Error adding product ")
                            .append(productRequest.getTradeName())
                            .append(": ")
                            .append(e.getMessage())
                            .append("\n");
                    logger.error("Error adding product: {}", productRequest.getTradeName(), e);
                }
            }

            // Prepare response
            String message = String.format(
                    "تم استيراد %d منتج بنجاح، فشل %d منتج / Successfully imported %d products, failed %d products",
                    successCount, failureCount, successCount, failureCount
            );

            boolean isSuccess = successCount > 0;
            String errorDetails = errors.length() > 0 ? errors.toString() : null;

            logger.info("انتهت معالجة الملف / File processing completed: {} success, {} failures", successCount, failureCount);

            return new ImportResponse(isSuccess, message, successCount, errorDetails);

        } finally {
            // Clean up temp file
            try {
                Files.deleteIfExists(tempFilePath);
            } catch (IOException e) {
                logger.warn("Failed to delete temp file: {}", tempFilePath, e);
            }
        }
    }

    /**
     * Create MasterProduct entity from request DTO
     */
    private MasterProduct createMasterProductFromRequest(MProductDTORequest request) {
        MasterProduct masterProduct = new MasterProduct();

        // Set basic properties
        masterProduct.setTradeName(request.getTradeName());
        masterProduct.setScientificName(request.getScientificName());
        masterProduct.setConcentration(request.getConcentration());
        masterProduct.setSize(request.getSize());
        masterProduct.setRefPurchasePrice(request.getRefPurchasePrice());
        masterProduct.setRefSellingPrice(request.getRefSellingPrice());
        masterProduct.setNotes(request.getNotes());
        masterProduct.setTax(request.getTax());
        masterProduct.setBarcode(request.getBarcode());

        // Set relationships using existing entities
        if (request.getFormId() != null) {
            Optional<Form> form = formRepository.findById(request.getFormId());
            form.ifPresent(masterProduct::setForm);
        }

        if (request.getManufacturerId() != null) {
            Optional<Manufacturer> manufacturer = manufacturerRepository.findById(request.getManufacturerId());
            manufacturer.ifPresent(masterProduct::setManufacturer);
        }

        return masterProduct;
    }

    /**
     * Call Python script with database connection parameters
     */
    private List<MProductDTORequest> callPythonScriptWithDatabase(String filePath) {
        try {
            // Build database connection string for Python script
            String dbConnectionString = buildDatabaseConnectionString();

            // Build command to execute Python script with database config
            String scriptPath = pythonScriptPath + "extract_with_existing_db.py";
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python3", scriptPath, filePath, dbConnectionString
            );

            processBuilder.directory(new File(pythonScriptPath));
            processBuilder.redirectErrorStream(true);

            // Start process
            Process process = processBuilder.start();

            // Read output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // Wait for process to complete
            boolean finished = process.waitFor(5, TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                logger.error("Python script timed out");
                return null;
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                logger.error("Python script failed with exit code: {}, output: {}", exitCode, output.toString());
                return null;
            }

            // Parse JSON output
            String jsonOutput = output.toString().trim();
            if (jsonOutput.startsWith("[") && jsonOutput.endsWith("]")) {
                return objectMapper.readValue(jsonOutput, new TypeReference<List<MProductDTORequest>>() {});
            } else {
                logger.error("Invalid JSON output from Python script: {}", jsonOutput);
                return null;
            }

        } catch (Exception e) {
            logger.error("Error calling Python script", e);
            return null;
        }
    }

    /**
     * Build database connection string for Python script
     */
    private String buildDatabaseConnectionString() {
        try {
            // Parse JDBC URL to create Python-compatible connection string
            if (databaseUrl.contains("mysql")) {
                // Extract database details from JDBC URL
                // jdbc:mysql://localhost:3306/pharmaceutical_db -> mysql://username:password@localhost:3306/pharmaceutical_db
                String cleanUrl = databaseUrl.replace("jdbc:", "");
                return cleanUrl.replace("//", "//" + databaseUsername + ":" + databasePassword + "@");
            } else if (databaseUrl.contains("sqlite")) {
                // Handle SQLite
                return databaseUrl.replace("jdbc:sqlite:", "sqlite://");
            } else {
                // Default fallback
                logger.warn("Unsupported database type, using SQLite fallback");
                return "sqlite:///tmp/pharmaceutical_fallback.db";
            }
        } catch (Exception e) {
            logger.error("Error building database connection string: {}", e.getMessage());
            return "sqlite:///tmp/pharmaceutical_fallback.db";
        }
    }

    /**
     * Get import statistics
     */
    public ImportResponse getImportStatistics() {
        try {
            long totalProducts = masterProductRepository.count();
            long totalManufacturers = manufacturerRepository.count();
            long totalForms = formRepository.count();

            String message = String.format(
                    "إحصائيات قاعدة البيانات / Database Statistics: %d منتج، %d مصنع، %d شكل صيدلاني / %d products, %d manufacturers, %d forms",
                    totalProducts, totalManufacturers, totalForms, totalProducts, totalManufacturers, totalForms
            );

            return new ImportResponse(true, message, (int) totalProducts, null);

        } catch (Exception e) {
            logger.error("Error getting statistics: {}", e.getMessage());
            return new ImportResponse(false, "خطأ في الحصول على الإحصائيات / Error getting statistics", 0, e.getMessage());
        }
    }

    /**
     * Validate database connection and tables
     */
    public boolean validateDatabaseSchema() {
        try {
            // Check if required tables exist by trying to count records
            manufacturerRepository.count();
            formRepository.count();
            masterProductRepository.count();

            logger.info("تم التحقق من صحة قاعدة البيانات / Database schema validation successful");
            return true;

        } catch (Exception e) {
            logger.error("فشل في التحقق من قاعدة البيانات / Database schema validation failed: {}", e.getMessage());
            return false;
        }
    }
}

