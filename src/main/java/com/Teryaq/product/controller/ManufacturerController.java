package com.Teryaq.product.controller;


import com.Teryaq.product.dto.ManufacturerDTORequest;
import com.Teryaq.product.dto.ManufacturerDTOResponse;
import com.Teryaq.product.service.ManufacturerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/manufacturers")
public class ManufacturerController {

    private final ManufacturerService manufacturerService;

    public ManufacturerController(ManufacturerService manufacturerService) {
        this.manufacturerService = manufacturerService;
    }

    @GetMapping
    public List<ManufacturerDTOResponse> getManufacturers(@RequestParam String lang) {
        return manufacturerService.getManufacturers(lang);}

    @GetMapping("{id}")
    public ManufacturerDTOResponse getById(@PathVariable Long id, @RequestParam String lang) {
        return manufacturerService.getByID(id, lang);}

    @PostMapping
    public ResponseEntity<ManufacturerDTOResponse> createManufacturer(@RequestBody ManufacturerDTORequest dto) {
        ManufacturerDTOResponse response = manufacturerService.insertManufacturer(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);}

    @PutMapping("{id}")
    public ManufacturerDTOResponse updateManufacturer(@PathVariable Long id, @RequestBody ManufacturerDTORequest manufacturer) {
        return manufacturerService.editManufacturer(id, manufacturer);
    }

    @DeleteMapping("{id}")
    public void deleteManufacturer(@PathVariable Long id) {
        manufacturerService.deleteManufacturer(id);
    }

}
