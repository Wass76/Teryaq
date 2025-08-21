package com.Teryaq.product;

import com.Teryaq.language.Language;
import com.Teryaq.language.LanguageRepo;
import com.Teryaq.product.repo.*;
import com.Teryaq.product.entity.*;
import com.Teryaq.user.Enum.PharmacyType;
import com.Teryaq.user.entity.Customer;
import com.Teryaq.user.entity.Pharmacy;
import com.Teryaq.user.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeed {

    private static final Logger logger = LoggerFactory.getLogger(DataSeed.class);

    private final LanguageRepo languageRepo;
    private final CategoryRepo categoryRepo;
    private final CategoryTranslationRepo categoryTranslationRepo;
    private final FormRepo formRepo;
    private final FormTranslationRepo formTranslationRepo;
    private final TypeRepo typeRepo;
    private final TypeTranslationRepo typeTranslationRepo;
    private final ManufacturerRepo manufacturerRepo;
    private final ManufacturerTranslationRepo manufacturerTranslationRepo;
    private final CustomerRepo customerRepository;
    private final PharmacyRepository pharmacyRepository;
    
  

    @PostConstruct
    public void seedAll() {
        try {
            logger.info("Starting data seeding process...");
            
            // Check if database is ready
            if (!isDatabaseReady()) {
                logger.warn("Database is not ready for seeding. Skipping data seeding.");
                return;
            }
            
            seedLanguages();
            seedCategories();
            seedForms();
            seedTypes();
            seedManufacturers();
            seedPharmacy(); // Must be before customers since customers depend on pharmacy
            seedCustomers();
            
            logger.info("Data seeding completed successfully!");
        } catch (Exception e) {
            logger.error("Error during data seeding: {}", e.getMessage(), e);
            logger.warn("Application will continue without seeded data. You may need to seed data manually later.");
        }
    }
    
    private boolean isDatabaseReady() {
        try {
            // Try to access a simple repository method to check if tables exist
            languageRepo.count();
            return true;
        } catch (Exception e) {
            logger.warn("Database tables are not ready yet: {}", e.getMessage());
            return false;
        }
    }

    private void seedLanguages() {
        try {
            if (languageRepo.count() == 0) {
                List<Language> languages = List.of(
                        new Language("ar", "Arabic"),
                        new Language("en", "English")
                );
                languageRepo.saveAll(languages);
                logger.info("✅ Languages seeded");
            } else {
                logger.info("Languages already exist, skipping seeding");
            }
        } catch (Exception e) {
            logger.error("Error seeding languages: {}", e.getMessage());
        }
    }

    private void seedCategories() {
        try {
            if (categoryRepo.count() == 0) {
                Language ar = languageRepo.findByCode("ar").orElseThrow();

                Category cat1 = new Category();
                cat1.setName("Painkillers");
                cat1 = categoryRepo.save(cat1);

                Category cat2 = new Category();
                cat2.setName("Antibiotics");
                cat2 = categoryRepo.save(cat2);

                Category cat3 = new Category();
                cat3.setName("Sterilizers");
                cat3 = categoryRepo.save(cat3);

                List<CategoryTranslation> translations = List.of(
                        new CategoryTranslation("مسكنات", cat1, ar),
                        new CategoryTranslation( "مضادات حيوية", cat2, ar),
                        new CategoryTranslation( "المعقمات ", cat3, ar)
                );
                categoryTranslationRepo.saveAll(translations);
                logger.info("✅ Categories seeded");
            } else {
                logger.info("Categories already exist, skipping seeding");
            }
        } catch (Exception e) {
            logger.error("Error seeding categories: {}", e.getMessage());
        }
    }

    private void seedForms() {
        try {
            if (formRepo.count() == 0) {
                Language ar = languageRepo.findByCode("ar").orElseThrow();

                Form form1 = new Form();
                form1.setName("Coated Tablets");
                form1 = formRepo.save(form1);

                Form form2 = new Form();
                form2.setName("syrup");
                form2 = formRepo.save(form2);

                Form form3 = new Form();
                form3.setName("Serum");
                form3 = formRepo.save(form3);

                List<FormTranslation> translations = List.of(
                        new FormTranslation("أقراص ملبسة", form1, ar),
                        new FormTranslation("شراب", form2, ar),
                        new FormTranslation("سيروم", form3, ar)
                );
                formTranslationRepo.saveAll(translations);
                logger.info("✅ Forms seeded");
            } else {
                logger.info("Forms already exist, skipping seeding");
            }
        } catch (Exception e) {
            logger.error("Error seeding forms: {}", e.getMessage());
        }
    }

    private void seedTypes() {
        try {
            if (typeRepo.count() == 0) {
                Language ar = languageRepo.findByCode("ar").orElseThrow();

                Type type1 = new Type();
                type1.setName("Medicine");
                type1 = typeRepo.save(type1);

                Type type2 = new Type();
                type2.setName("cosmetic");
                type2 = typeRepo.save(type2);

                Type type3 = new Type();
                type3.setName("Medical supplies");
                type3 = typeRepo.save(type3);

                List<TypeTranslation> translations = List.of(
                        new TypeTranslation( "دواء ", type1, ar),
                        new TypeTranslation( "مستحضر تجميل ", type2, ar),
                        new TypeTranslation( "مستلزمات طبية ", type3, ar)
                );
                typeTranslationRepo.saveAll(translations);
                logger.info("✅ Types seeded");
            } else {
                logger.info("Types already exist, skipping seeding");
            }
        } catch (Exception e) {
            logger.error("Error seeding types: {}", e.getMessage());
        }
    }

    private void seedManufacturers() {
        try {
            if (manufacturerRepo.count() == 0) {
                Language ar = languageRepo.findByCode("ar").orElseThrow();

                Manufacturer m1 = new Manufacturer();
                m1.setName("Teryaq Pharma");
                m1 = manufacturerRepo.save(m1);

                Manufacturer m2 = new Manufacturer();
                m2.setName("Ultra Medica");
                m2 = manufacturerRepo.save(m2);

                Manufacturer m3 = new Manufacturer();
                m3.setName("Avenzor");
                m3 = manufacturerRepo.save(m3);

                List<ManufacturerTranslation> translations = List.of(
                        new ManufacturerTranslation("ترياق فارما", m1, ar),
                        new ManufacturerTranslation("ألترا ميديكا", m2, ar),
                        new ManufacturerTranslation("ابن زهر", m3, ar)
                );
                manufacturerTranslationRepo.saveAll(translations);
                logger.info("✅ Manufacturers seeded");
            } else {
                logger.info("Manufacturers already exist, skipping seeding");
            }
        } catch (Exception e) {
            logger.error("Error seeding manufacturers: {}", e.getMessage());
        }
    }

//    private void seedSuppliers() {
//        if (supplierRepo.count() == 0) {
//            List<Supplier> suppliers = List.of(
//                new Supplier("شركة الشام للأدوية", "0999999999", "دمشق - باب شرقي", Currency.SYP , pharmacyRepository.findAll().getFirst()),
//                new Supplier("شركة ابن زهر", "0988888888", "حلب - الجميلية", Currency.SYP , pharmacyRepository.findAll().getFirst()),
//                new Supplier("شركة ترياق", "0933333333", "دمشق - المزة", Currency.SYP ,  pharmacyRepository.findAll().getFirst())
//            );
//            supplierRepo.saveAll(suppliers);
//            System.out.println("✅ Suppliers seeded");
//        }
//    }

    private void seedCustomers() {
        try {
            // Get the pharmacy first
            Pharmacy pharmacy = pharmacyRepository.findAll().get(0);
            
            // Always ensure cash customer exists
            ensureCashCustomerExists(pharmacy);
            
            if (customerRepository.count() == 1) { // Only cash customer exists
                List<Customer> customers = List.of(
                    createCustomer("أحمد محمد", "0991111111", "دمشق - المزة", pharmacy),
                    createCustomer("فاطمة علي", "0992222222", "دمشق - باب شرقي", pharmacy),
                    createCustomer("محمد حسن", "0993333333", "دمشق - أبو رمانة", pharmacy),
                    createCustomer("عائشة أحمد", "0994444444", "دمشق - القابون", pharmacy),
                    createCustomer("علي محمود", "0995555555", "دمشق - الميدان", pharmacy)
                );
                customerRepository.saveAll(customers);
                logger.info("✅ Additional customers seeded");
            } else if (customerRepository.count() == 0) {
                // No customers exist, create all including cash customer
                List<Customer> customers = List.of(
                    createCashCustomer(pharmacy),  // Cash Customer for direct sales
                    createCustomer("أحمد محمد", "0991111111", "دمشق - المزة", pharmacy),
                    createCustomer("فاطمة علي", "0992222222", "دمشق - باب شرقي", pharmacy),
                    createCustomer("محمد حسن", "0993333333", "دمشق - أبو رمانة", pharmacy),
                    createCustomer("عائشة أحمد", "0994444444", "دمشق - القابون", pharmacy),
                    createCustomer("علي محمود", "0995555555", "دمشق - الميدان", pharmacy)
                );
                customerRepository.saveAll(customers);
                logger.info("✅ All customers seeded including cash customer");
            } else {
                // Customers exist, but still ensure cash customer exists
                ensureCashCustomerExists(pharmacy);
                logger.info("Customers already exist, ensuring cash customer exists");
            }
        } catch (Exception e) {
            logger.error("Error seeding customers: {}", e.getMessage());
        }
    }
    
    private void ensureCashCustomerExists(Pharmacy pharmacy) {
        try {
            // Check if cash customer already exists
            if (!customerRepository.findByNameAndPharmacyId("cash customer", pharmacy.getId()).isPresent()) {
                Customer cashCustomer = createCashCustomer(pharmacy);
                customerRepository.save(cashCustomer);
                logger.info("✅ Cash customer created for pharmacy: {}", pharmacy.getName());
            } else {
                logger.info("Cash customer already exists for pharmacy: {}", pharmacy.getName());
            }
        } catch (Exception e) {
            logger.error("Error ensuring cash customer exists: {}", e.getMessage());
        }
    }
    
    private Customer createCashCustomer(Pharmacy pharmacy) {
        Customer cashCustomer = new Customer();
        cashCustomer.setName("cash customer");
        cashCustomer.setPhoneNumber(null);  // لا يوجد رقم هاتف
        cashCustomer.setAddress(null);      // لا يوجد عنوان
        cashCustomer.setPharmacy(pharmacy);
        return cashCustomer;
    }
    
    private Customer createCustomer(String name, String phoneNumber, String address, Pharmacy pharmacy) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setPhoneNumber(phoneNumber);
        customer.setAddress(address);
        customer.setPharmacy(pharmacy);
        return customer;
    }

    private void seedPharmacy() {
        try {
            if (pharmacyRepository.count() == 0) {
                Pharmacy pharmacy = new Pharmacy();
                pharmacy.setName("صيدلية ترياق");
                pharmacy.setLicenseNumber("PH-001-2024");
                pharmacy.setAddress("دمشق - المزة");
                pharmacy.setEmail("info@teryaq-pharmacy.com");
                pharmacy.setPhoneNumber("011-1234567");
                pharmacy.setOpeningHours("8:00 AM - 10:00 PM");
                pharmacy.setType(PharmacyType.MAIN);
                pharmacy = pharmacyRepository.save(pharmacy);
                logger.info("✅ Pharmacy seeded");
            } else {
                logger.info("Pharmacy already exist, skipping seeding");
            }
        } catch (Exception e) {
            logger.error("Error seeding pharmacy: {}", e.getMessage());
        }
    }

//    private void seedEmployee() {
//        if (employeeRepository.count() == 0) {
//            // Get pharmacy and role
//            Pharmacy pharmacy = pharmacyRepository.findAll().get(0);
//            Role employeeRole = roleRepository.findByName("EMPLOYEE")
//                    .orElse(roleRepository.findByName("PHARMACY_EMPLOYEE")
//                            .orElse(roleRepository.findAll().get(0))); // Fallback to first role
//
//            // Create employee
//            Employee employee = Employee.builder()
//                    .firstName("صيدلي")
//                    .lastName("ترياق")
//                    .email("pharmacist@teryaq.com")
//                    .password(passwordEncoder.encode("password123"))
//                    .role(employeeRole)
//                    .status(UserStatus.ACTIVE)
//                    .position("PHARMACIST")
//                    .pharmacy(pharmacy)
//                    .phoneNumber("0999999999")
//                    .dateOfHire(LocalDate.now())
//                    .build();
//
//            employeeRepository.save(employee);
//            System.out.println("✅ Employee seeded with pharmacy association");
//            System.out.println("Email: pharmacist@teryaq.com");
//            System.out.println("Password: password123");
//        }
//    }
}
    //     if (masterProductRepo.count() == 0) {
    //         Language ar = languageRepo.findByCode("ar").orElseThrow();
    //         Category painkillers = categoryRepo.findById(1L).orElseThrow();
    //         Category antibiotics = categoryRepo.findById(2L).orElseThrow();
    //         Form tablets = formRepo.findById(1L).orElseThrow(); 
    //         Type medicine = typeRepo.findById(1L).orElseThrow();
    //         Manufacturer teryaq = manufacturerRepo.findById(1L).orElseThrow();
    //         Manufacturer ultra = manufacturerRepo.findById(2L).orElseThrow();

    //         // Master Product 1: Paracetamol
    //         MasterProduct paracetamol = new MasterProduct();
    //         paracetamol.setTradeName("Paracetamol");
    //         paracetamol.setScientificName("Acetaminophen");
    //         paracetamol.setConcentration("500mg");
    //         paracetamol.setSize("20 tablets");
    //         paracetamol.setRefPurchasePrice(1500);
    //         paracetamol.setRefSellingPrice(2000);
    //         paracetamol.setBarcode("1234567890123");
    //         paracetamol.setCategories(new HashSet<>(List.of(painkillers)));
    //         paracetamol.setForm(tablets);
    //         paracetamol.setType(medicine);
    //         paracetamol.setManufacturer(teryaq);
    //         paracetamol = masterProductRepo.save(paracetamol);

    //         // Master Product 2: Amoxicillin
    //         MasterProduct amoxicillin = new MasterProduct();
    //         amoxicillin.setTradeName("Amoxicillin");
    //         amoxicillin.setScientificName("Amoxicillin Trihydrate");
    //         amoxicillin.setConcentration("250mg");
    //         amoxicillin.setSize("12 capsules");
    //         amoxicillin.setRefPurchasePrice(2500);
    //         amoxicillin.setRefSellingPrice(3500);
    //         amoxicillin.setBarcode("1234567890124");
    //         amoxicillin.setCategories(new HashSet<>(List.of(antibiotics)));
    //         amoxicillin.setForm(tablets);
    //         amoxicillin.setType(medicine);
    //         amoxicillin.setManufacturer(ultra);
    //         amoxicillin = masterProductRepo.save(amoxicillin);

    //         // Master Product 3: Ibuprofen
    //         MasterProduct ibuprofen = new MasterProduct();
    //         ibuprofen.setTradeName("Ibuprofen");
    //         ibuprofen.setScientificName("Ibuprofen");
    //         ibuprofen.setConcentration("400mg");
    //         ibuprofen.setSize("30 tablets");
    //         ibuprofen.setRefPurchasePrice(1800);
    //         ibuprofen.setRefSellingPrice(2500);
    //         ibuprofen.setBarcode("1234567890125");
    //         ibuprofen.setCategories(new HashSet<>(List.of(painkillers)));
    //         ibuprofen.setForm(tablets);
    //         ibuprofen.setType(medicine);
    //         ibuprofen.setManufacturer(teryaq);
    //         ibuprofen = masterProductRepo.save(ibuprofen);

    //         // Translations
    //         List<MasterProductTranslation> translations = List.of(
    //             new MasterProductTranslation("باراسيتامول", "أسيتامينوفين", "مسكن للألم وخافض للحرارة", paracetamol, ar),
    //             new MasterProductTranslation("أموكسيسيلين", "أموكسيسيلين تريهيدرات", "مضاد حيوي واسع الطيف", amoxicillin, ar),
    //             new MasterProductTranslation("إيبوبروفين", "إيبوبروفين", "مسكن للألم ومضاد للالتهاب", ibuprofen, ar)
    //         );
    //         masterProductTranslationRepo.saveAll(translations);
    //         System.out.println("✅ Master Products seeded");
    //     }
    // }

    // private void seedPharmacyProducts() {
    //     if (pharmacyProductRepo.count() == 0) {
    //         Pharmacy pharmacy = pharmacyRepository.findById(1L).orElseThrow();
    //         Category painkillers = categoryRepo.findById(1L).orElseThrow();
    //         Form tablets = formRepo.findById(1L).orElseThrow();
    //         Type medicine = typeRepo.findById(1L).orElseThrow();
    //         Manufacturer teryaq = manufacturerRepo.findById(1L).orElseThrow();

    //         // Pharmacy Product 1: Local Paracetamol
    //         PharmacyProduct localParacetamol = new PharmacyProduct();
    //         localParacetamol.setTradeName("باراسيتامول محلي");
    //         localParacetamol.setScientificName("Acetaminophen");
    //         localParacetamol.setConcentration("500mg");
    //         localParacetamol.setSize("10 tablets");
    //         localParacetamol.setNotes("منتج محلي عالي الجودة");
    //         localParacetamol.setTax(0.05f); // 5% tax
    //         localParacetamol.setRequiresPrescription(false);
    //         localParacetamol.setPharmacy(pharmacy);
    //         localParacetamol.setCategories(new HashSet<>(List.of(painkillers)));
    //         localParacetamol.setForm(tablets);
    //         localParacetamol.setType(medicine);
    //         localParacetamol.setManufacturer(teryaq);
    //         localParacetamol = pharmacyProductRepo.save(localParacetamol);

    //         // Pharmacy Product 2: Local Ibuprofen
    //         PharmacyProduct localIbuprofen = new PharmacyProduct();
    //         localIbuprofen.setTradeName("إيبوبروفين محلي");
    //         localIbuprofen.setScientificName("Ibuprofen");
    //         localIbuprofen.setConcentration("400mg");
    //         localIbuprofen.setSize("15 tablets");
    //         localIbuprofen.setNotes("مسكن قوي للألم");
    //         localIbuprofen.setTax(0.05f);
    //         localIbuprofen.setRequiresPrescription(false);
    //         localIbuprofen.setPharmacy(pharmacy);
    //         localIbuprofen.setCategories(new HashSet<>(List.of(painkillers)));
    //         localIbuprofen.setForm(tablets);
    //         localIbuprofen.setType(medicine);
    //         localIbuprofen.setManufacturer(teryaq);
    //         localIbuprofen = pharmacyProductRepo.save(localIbuprofen);

    //         System.out.println("✅ Pharmacy Products seeded");
    //     }
    // }

    // private void seedPurchaseOrders() {
    //     if (purchaseOrderRepo.count() == 0) {
    //         // Get first supplier and products from existing data
    //         List<Supplier> suppliers = supplierRepo.findAll();
    //         List<MasterProduct> products = masterProductRepo.findAll();
            
    //         if (suppliers.isEmpty() || products.size() < 2) {
    //             System.out.println("⚠️ Skipping Purchase Orders - insufficient data");
    //             return;
    //         }
            
    //         Supplier supplier = suppliers.get(0);
    //         MasterProduct paracetamol = products.get(0);
    //         MasterProduct amoxicillin = products.get(1);

    //         // Purchase Order 1
    //         PurchaseOrder order1 = new PurchaseOrder();
    //         order1.setSupplier(supplier);
    //         order1.setCurrency("SYP");
    //         order1.setTotal(275000.0);
    //         order1.setStatus(OrderStatus.DONE);
    //         order1 = purchaseOrderRepo.save(order1);

    //         // Purchase Order Items
    //         PurchaseOrderItem item1 = new PurchaseOrderItem();
    //         item1.setPurchaseOrder(order1);
    //         item1.setProductId(paracetamol.getId());
    //         item1.setProductType(ProductType.MASTER);
    //         item1.setQuantity(100);
    //         item1.setPrice(1500.0);
    //         purchaseOrderItemRepo.save(item1);

    //         PurchaseOrderItem item2 = new PurchaseOrderItem();
    //         item2.setPurchaseOrder(order1);
    //         item2.setProductId(amoxicillin.getId());
    //         item2.setProductType(ProductType.MASTER);
    //         item2.setQuantity(50);
    //         item2.setPrice(2500.0);
    //         purchaseOrderItemRepo.save(item2);

    //         // Purchase Order 2
    //         PurchaseOrder order2 = new PurchaseOrder();
    //         order2.setSupplier(supplier);
    //         order2.setCurrency("SYP");
    //         order2.setTotal(225000.0);
    //         order2.setStatus(OrderStatus.DONE);
    //         order2 = purchaseOrderRepo.save(order2);

    //         PurchaseOrderItem item3 = new PurchaseOrderItem();
    //         item3.setPurchaseOrder(order2);
    //         item3.setProductId(paracetamol.getId());
    //         item3.setProductType(ProductType.MASTER);
    //         item3.setQuantity(150);
    //         item3.setPrice(1500.0);
    //         purchaseOrderItemRepo.save(item3);

    //         System.out.println("✅ Purchase Orders seeded");
    //     }
    // }

    // private void seedPurchaseInvoices() {
    //     if (purchaseInvoiceRepo.count() == 0) {
    //         List<PurchaseOrder> orders = purchaseOrderRepo.findAll();
    //         List<MasterProduct> products = masterProductRepo.findAll();
            
    //         if (orders.size() < 2 || products.size() < 2) {
    //             System.out.println("⚠️ Skipping Purchase Invoices - insufficient data");
    //             return;
    //         }
            
    //         PurchaseOrder order1 = orders.get(0);
    //         PurchaseOrder order2 = orders.get(1);
    //         MasterProduct paracetamol = products.get(0);
    //         MasterProduct amoxicillin = products.get(1);

    //         // Purchase Invoice 1
    //         PurchaseInvoice invoice1 = new PurchaseInvoice();
    //         invoice1.setPurchaseOrder(order1);
    //         invoice1.setCurrency(Currency.SYP);
    //         invoice1.setTotal(275000.0);
    //         invoice1.setSupplier(order1.getSupplier());
    //         invoice1 = purchaseInvoiceRepo.save(invoice1);

    //         // Purchase Invoice Items
    //         PurchaseInvoiceItem invItem1 = new PurchaseInvoiceItem();
    //         invItem1.setPurchaseInvoice(invoice1);
    //         invItem1.setProductId(paracetamol.getId());
    //         invItem1.setProductType(ProductType.MASTER);
    //         invItem1.setReceivedQty(100);
    //         invItem1.setBonusQty(10);
    //         invItem1.setInvoicePrice(1500.0);
    //         invItem1.setActualPrice(1500.0);
    //         invItem1.setBatchNo("BATCH-001-2024");
    //         invItem1.setExpiryDate(LocalDate.now().plusMonths(12));
    //         purchaseInvoiceItemRepo.save(invItem1);

    //         PurchaseInvoiceItem invItem2 = new PurchaseInvoiceItem();
    //         invItem2.setPurchaseInvoice(invoice1);
    //         invItem2.setProductId(amoxicillin.getId());
    //         invItem2.setProductType(ProductType.MASTER);
    //         invItem2.setReceivedQty(50);
    //         invItem2.setBonusQty(5);
    //         invItem2.setInvoicePrice(2500.0);
    //         invItem2.setActualPrice(2500.0);
    //         invItem2.setBatchNo("BATCH-002-2024");
    //         invItem2.setExpiryDate(LocalDate.now().plusMonths(18));
    //         purchaseInvoiceItemRepo.save(invItem2);

    //         // Purchase Invoice 2
    //         PurchaseInvoice invoice2 = new PurchaseInvoice();
    //         invoice2.setPurchaseOrder(order2);
    //         invoice2.setCurrency(Currency.SYP);
    //         invoice2.setTotal(225000.0);
    //         invoice2.setSupplier(order2.getSupplier());
    //         invoice2 = purchaseInvoiceRepo.save(invoice2);

    //         PurchaseInvoiceItem invItem3 = new PurchaseInvoiceItem();
    //         invItem3.setPurchaseInvoice(invoice2);
    //         invItem3.setProductId(paracetamol.getId());
    //         invItem3.setProductType(ProductType.MASTER);
    //         invItem3.setReceivedQty(150);
    //         invItem3.setBonusQty(15);
    //         invItem3.setInvoicePrice(1500.0);
    //         invItem3.setActualPrice(1500.0);
    //         invItem3.setBatchNo("BATCH-003-2024");
    //         invItem3.setExpiryDate(LocalDate.now().plusMonths(15));
    //         purchaseInvoiceItemRepo.save(invItem3);

    //         System.out.println("✅ Purchase Invoices seeded");
    //     }
    // }

    // private void seedStockItems() {
    //     if (stockItemRepo.count() == 0) {
    //         List<PurchaseInvoice> invoices = purchaseInvoiceRepo.findAll();
    //         List<MasterProduct> products = masterProductRepo.findAll();
            
    //         if (invoices.size() < 2 || products.size() < 3) {
    //             System.out.println("⚠️ Skipping Stock Items - insufficient data");
    //             return;
    //         }
            
    //         PurchaseInvoice invoice1 = invoices.get(0);
    //         PurchaseInvoice invoice2 = invoices.get(1);
    //         MasterProduct paracetamol = products.get(0);
    //         MasterProduct amoxicillin = products.get(1);
    //         MasterProduct ibuprofen = products.get(2);

    //         // Stock Items from Invoice 1
    //         StockItem stock1 = new StockItem();
    //         stock1.setProductId(paracetamol.getId());
    //         stock1.setProductType(ProductType.MASTER);
    //         stock1.setQuantity(100);
    //         stock1.setBonusQty(10);
    //         stock1.setExpiryDate(LocalDate.now().plusMonths(12));
    //         stock1.setBatchNo("BATCH-001-2024");
    //         stock1.setActualPurchasePrice(1500.0);
    //         stock1.setDateAdded(LocalDateTime.now().minusDays(25));
    //         stock1.setAddedBy(1L);
    //         stock1.setPurchaseInvoice(invoice1);
    //         stockItemRepo.save(stock1);

    //         StockItem stock2 = new StockItem();
    //         stock2.setProductId(amoxicillin.getId());
    //         stock2.setProductType(ProductType.MASTER);
    //         stock2.setQuantity(50);
    //         stock2.setBonusQty(5);
    //         stock2.setExpiryDate(LocalDate.now().plusMonths(18));
    //         stock2.setBatchNo("BATCH-002-2024");
    //         stock2.setActualPurchasePrice(2500.0);
    //         stock2.setDateAdded(LocalDateTime.now().minusDays(25));
    //         stock2.setAddedBy(1L);
    //         stock2.setPurchaseInvoice(invoice1);
    //         stockItemRepo.save(stock2);

    //         // Stock Items from Invoice 2
    //         StockItem stock3 = new StockItem();
    //         stock3.setProductId(paracetamol.getId());
    //         stock3.setProductType(ProductType.MASTER);
    //         stock3.setQuantity(150);
    //         stock3.setBonusQty(15);
    //         stock3.setExpiryDate(LocalDate.now().plusMonths(15));
    //         stock3.setBatchNo("BATCH-003-2024");
    //         stock3.setActualPurchasePrice(1500.0);
    //         stock3.setDateAdded(LocalDateTime.now().minusDays(10));
    //         stock3.setAddedBy(1L);
    //         stock3.setPurchaseInvoice(invoice2);
    //         stockItemRepo.save(stock3);

    //         // Additional Stock Items for testing
    //         StockItem stock4 = new StockItem();
    //         stock4.setProductId(ibuprofen.getId());
    //         stock4.setProductType(ProductType.MASTER);
    //         stock4.setQuantity(75);
    //         stock4.setBonusQty(8);
    //         stock4.setExpiryDate(LocalDate.now().plusMonths(10));
    //         stock4.setBatchNo("BATCH-004-2024");
    //         stock4.setActualPurchasePrice(1800.0);
    //         stock4.setDateAdded(LocalDateTime.now().minusDays(5));
    //         stock4.setAddedBy(1L);
    //         stock4.setPurchaseInvoice(invoice2);
    //         stockItemRepo.save(stock4);

    //         System.out.println("✅ Stock Items seeded");
    //     }
    // }

    // private void seedSampleSales() {
    //     if (saleInvoiceRepository.count() == 0) {
    //         List<Customer> customers = customerRepository.findAll();
    //         List<StockItem> stockItems = stockItemRepo.findAll();
            
    //         if (customers.size() < 2 || stockItems.size() < 3) {
    //             System.out.println("⚠️ Skipping Sample Sales - insufficient data");
    //             return;
    //         }
            
    //         Customer customer1 = customers.get(0);
    //         Customer customer2 = customers.get(1);
            
    //         StockItem stock1 = stockItems.get(0);
    //         StockItem stock2 = stockItems.get(1);
    //         StockItem stock3 = stockItems.get(2);
            
    //         // Sample Sale Invoice 1 - Cash Payment
    //         SaleInvoice sale1 = new SaleInvoice();
    //         sale1.setCustomer(customer1);
    //         sale1.setInvoiceDate(LocalDateTime.now().minusDays(2));
    //         sale1.setTotalAmount(5000);
    //         sale1.setPaymentType(PaymentType.CASH);
    //         sale1.setPaymentMethod(PaymentMethod.CASH);
    //         sale1.setDiscount(500);
    //         sale1.setDiscountType(DiscountType.FIXED_AMOUNT);
    //         sale1.setPaidAmount(4500);
    //         sale1.setRemainingAmount(0);
    //         sale1 = saleInvoiceRepository.save(sale1);
            
    //         SaleInvoiceItem saleItem1 = new SaleInvoiceItem();
    //         saleItem1.setSaleInvoice(sale1);
    //         saleItem1.setStockItem(stock1);
    //         saleItem1.setQuantity(2);
    //         saleItem1.setUnitPrice(1500.0f);
    //         saleItem1.setDiscount(300.0f);
    //         saleItem1.setDiscountType(DiscountType.FIXED_AMOUNT);
    //         saleItem1.setSubTotal(2700.0f);
    //         saleInvoiceItemRepository.save(saleItem1);
            
    //         SaleInvoiceItem saleItem2 = new SaleInvoiceItem();
    //         saleItem2.setSaleInvoice(sale1);
    //         saleItem2.setStockItem(stock2);
    //         saleItem2.setQuantity(1);
    //         saleItem2.setUnitPrice(2500.0f);
    //         saleItem2.setDiscount(200.0f);
    //         saleItem2.setDiscountType(DiscountType.FIXED_AMOUNT);
    //         saleItem2.setSubTotal(2300.0f);
    //         saleInvoiceItemRepository.save(saleItem2);
            
    //         // Sample Sale Invoice 2 - Credit Payment
    //         SaleInvoice sale2 = new SaleInvoice();
    //         sale2.setCustomer(customer2);
    //         sale2.setInvoiceDate(LocalDateTime.now().minusDays(1));
    //         sale2.setTotalAmount(8000);
    //         sale2.setPaymentType(PaymentType.CREDIT);
    //         sale2.setPaymentMethod(PaymentMethod.BANK_ACCOUNT);
    //         sale2.setDiscount(800);
    //         sale2.setDiscountType(DiscountType.PERCENTAGE);
    //         sale2.setPaidAmount(4000);
    //         sale2.setRemainingAmount(3200);
    //         sale2 = saleInvoiceRepository.save(sale2);
            
    //         SaleInvoiceItem saleItem3 = new SaleInvoiceItem();
    //         saleItem3.setSaleInvoice(sale2);
    //         saleItem3.setStockItem(stock3);
    //         saleItem3.setQuantity(3);
    //         saleItem3.setUnitPrice(1500.0f);
    //         saleItem3.setDiscount(225.0f);
    //         saleItem3.setDiscountType(DiscountType.PERCENTAGE);
    //         saleItem3.setSubTotal(4275.0f);
    //         saleInvoiceItemRepository.save(saleItem3);
            
    //         SaleInvoiceItem saleItem4 = new SaleInvoiceItem();
    //         saleItem4.setSaleInvoice(sale2);
    //         saleItem4.setStockItem(stock1);
    //         saleItem4.setQuantity(2);
    //         saleItem4.setUnitPrice(1500.0f);
    //         saleItem4.setDiscount(300.0f);
    //         saleItem4.setDiscountType(DiscountType.FIXED_AMOUNT);
    //         saleItem4.setSubTotal(2700.0f);
    //         saleInvoiceItemRepository.save(saleItem4);
            
    //         System.out.println("✅ Sample Sales seeded");
    //     }
    // }

    // private void seedCustomerDebts() {
    //     if (customerDebtRepository.count() == 0) {
    //         List<Customer> customers = customerRepository.findAll();
            
    //         if (customers.size() < 3) {
    //             System.out.println("⚠️ Skipping Customer Debts - insufficient data");
    //             return;
    //         }
            
    //         Customer customer1 = customers.get(0);
    //         Customer customer2 = customers.get(1);
    //         Customer customer3 = customers.get(2);

    //         // Debt 1: Active debt for customer 1
    //         CustomerDebt debt1 = CustomerDebt.builder()
    //             .customer(customer1)
    //             .amount(new Float("5000.00"))
    //             .paidAmount(new Float("2000.00"))
    //             .remainingAmount(new Float("3000.00"))
    //             .dueDate(LocalDateTime.now().plusMonths(2))
    //             .notes("دين على شراء أدوية مسكنة")
    //             .status("ACTIVE")
    //             .build();
    //         customerDebtRepository.save(debt1);

    //         // Debt 2: Overdue debt for customer 2
    //         CustomerDebt debt2 = CustomerDebt.builder()
    //             .customer(customer2)
    //             .amount(new Float("3000.00"))
    //             .paidAmount(new Float("0.00"))
    //             .remainingAmount(new Float("3000.00"))
    //             .dueDate(LocalDateTime.now().minusDays(15))
    //             .notes("دين متأخر على شراء مضادات حيوية")
    //             .status("ACTIVE")
    //             .build();
    //         customerDebtRepository.save(debt2);

    //         // Debt 3: Paid debt for customer 3
    //         CustomerDebt debt3 = CustomerDebt.builder()
    //             .customer(customer3)
    //             .amount(new Float("2000.00"))
    //             .paidAmount(new Float("2000.00"))
    //             .remainingAmount(new Float("0.00"))
    //             .dueDate(LocalDateTime.now().minusDays(5))
    //             .notes("دين مكتمل الدفع")
    //             .status("PAID")
    //             .paidAt(LocalDateTime.now().minusDays(5))
    //             .build();
    //         customerDebtRepository.save(debt3);

    //         // Debt 4: New debt for customer 1
    //         CustomerDebt debt4 = CustomerDebt.builder()
    //             .customer(customer1)
    //             .amount(new Float("1500.00"))
    //             .paidAmount(new Float("0.00"))
    //             .remainingAmount(new Float("1500.00"))
    //             .dueDate(LocalDateTime.now().plusMonths(1))
    //             .notes("دين جديد على شراء فيتامينات")
    //             .status("ACTIVE")
    //             .build();
    //         customerDebtRepository.save(debt4);

    //         System.out.println("✅ Customer Debts seeded");
    //     }
    // }
//}