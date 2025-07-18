package com.Teryaq.product.controller;


import com.Teryaq.product.dto.ManufacturerDTORequest;
import com.Teryaq.product.dto.ManufacturerDTOResponse;
import com.Teryaq.product.service.ManufacturerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("api/v1/manufacturers")
public class ManufacturerController {

    private final ManufacturerService manufacturerService;

    public ManufacturerController(ManufacturerService manufacturerService) {
        this.manufacturerService = manufacturerService;
    }

    @GetMapping
    public  ResponseEntity<?> getManufacturers(@RequestParam(name = "lang", defaultValue = "en") String lang) {
        return ResponseEntity.ok(manufacturerService.getManufacturers(lang));}

    @GetMapping("{id}")
    public  ResponseEntity<?> getById(@PathVariable Long id, @RequestParam(name = "lang", defaultValue = "en") String lang) {
        return ResponseEntity.ok(manufacturerService.getByID(id, lang));}

    @PostMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN') || hasRole('PHARMACY_MANAGER')")
    public ResponseEntity<?> createManufacturer(@RequestBody ManufacturerDTORequest dto,
                                                @RequestParam(name = "lang", defaultValue = "en") String lang) {
        ManufacturerDTOResponse response = manufacturerService.insertManufacturer(dto, lang);
        return new ResponseEntity<>(response, HttpStatus.CREATED);}

    @PutMapping("{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public  ResponseEntity<?> updateManufacturer(@PathVariable Long id,
                                                 @RequestBody ManufacturerDTORequest manufacturer,
                                                 @RequestParam(name = "lang", defaultValue = "en") String lang) {
        return ResponseEntity.ok(manufacturerService.editManufacturer(id, manufacturer, lang));
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public  ResponseEntity<Void> deleteManufacturer(@PathVariable Long id) {
        manufacturerService.deleteManufacturer(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
