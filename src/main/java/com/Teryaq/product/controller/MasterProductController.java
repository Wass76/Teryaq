package com.Teryaq.product.controller;


import com.Teryaq.product.entity.MasterProduct;
import com.Teryaq.product.service.MasterProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/master_products")
public class MasterProductController {

    private final MasterProductService masterProductService;

    public MasterProductController(MasterProductService masterProductService) {
        this.masterProductService = masterProductService;
    }

    @GetMapping
    public List<MasterProduct> getAllMasterProducts() {
        return masterProductService.getMasterProduct();}

    @GetMapping("{id}")
    public MasterProduct getMasterProductById(@PathVariable Long id) {
        return masterProductService.getByID(id);
    }

    @PostMapping
    public void createMasterProduct(@RequestBody MasterProduct masterProduct) {
        masterProductService.insertMasterProduct(masterProduct);
    }

    @PutMapping("{id}")
    public MasterProduct updateMasterProductById(@PathVariable Long id,
                                                 @RequestBody MasterProduct masterProduct) {
        return masterProductService.editMasterProduct(id, masterProduct);
    }

    @DeleteMapping("{id}")
    public void deleteMasterProductById(@PathVariable Long id) {
        masterProductService.deleteMasterProduct(id);
    }
}
