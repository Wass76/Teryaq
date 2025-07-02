package com.Teryaq.product.controller;


import com.Teryaq.product.dto.MProductDTORequest;
import com.Teryaq.product.dto.MProductDTOResponse;
import com.Teryaq.product.dto.SearchDTORequest;
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
    public List<MProductDTOResponse> getAllMasterProducts(@RequestParam String lang) {
        return masterProductService.getMasterProduct(lang);}

    @GetMapping("{id}")
    public MProductDTOResponse getMasterProductById(@PathVariable Long id, @RequestParam String lang) {

        return masterProductService.getByID(id, lang);
    }

    @PostMapping("/search")
    public List<MProductDTOResponse> searchProducts(@RequestBody SearchDTORequest requestDTO) {
        return masterProductService.search(requestDTO);
    }

    @PostMapping
    public void createMasterProduct(@RequestBody MProductDTORequest masterProduct,@RequestParam String lang ) {
        masterProductService.insertMasterProduct(masterProduct, lang);
    }

    @PutMapping("{id}")
    public MProductDTOResponse updateMasterProductById(@PathVariable Long id,
                                                 @RequestBody MProductDTORequest masterProduct, @RequestParam String lang) {
        return masterProductService.editMasterProduct(id, masterProduct, lang);
    }

    @DeleteMapping("{id}")
    public void deleteMasterProductById(@PathVariable Long id) {
        masterProductService.deleteMasterProduct(id);
    }
}


