package com.Teryaq.product;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.Teryaq.language.Language;
import com.Teryaq.language.LanguageRepo;
import com.Teryaq.product.entity.Category;
import com.Teryaq.product.entity.CategoryTranslation;
import com.Teryaq.product.entity.Form;
import com.Teryaq.product.entity.FormTranslation;
import com.Teryaq.product.entity.Manufacturer;
import com.Teryaq.product.entity.ManufacturerTranslation;
import com.Teryaq.product.entity.Type;
import com.Teryaq.product.entity.TypeTranslation;
import com.Teryaq.product.repo.CategoryRepo;
import com.Teryaq.product.repo.CategoryTranslationRepo;
import com.Teryaq.product.repo.FormRepo;
import com.Teryaq.product.repo.FormTranslationRepo;
import com.Teryaq.product.repo.ManufacturerRepo;
import com.Teryaq.product.repo.ManufacturerTranslationRepo;
import com.Teryaq.product.repo.TypeRepo;
import com.Teryaq.product.repo.TypeTranslationRepo;
import com.Teryaq.user.Enum.PharmacyType;
import com.Teryaq.user.entity.Customer;
import com.Teryaq.user.entity.Pharmacy;
import com.Teryaq.user.repository.CustomerRepo;
import com.Teryaq.user.repository.PharmacyRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

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
             if (customerRepository.count() == 0) {
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
}

