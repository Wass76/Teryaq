package com.Teryaq.product.service;

import com.Teryaq.product.entity.Category;
import com.Teryaq.product.entity.MasterProduct;
import com.Teryaq.product.repo.MasterProductRepo;
import com.Teryaq.utils.exception.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MasterProductService {

    MasterProductRepo masterProductRepo;
    public List<MasterProduct> getMasterProduct() {return masterProductRepo.findAll();}

    public MasterProduct getByID(long id) {return masterProductRepo.findById(id)
            .orElseThrow(()->new EntityNotFoundException("Master Product With Id" + id + "not found"));}

    public void insertMasterProduct(MasterProduct masterProduct) {masterProductRepo.save(masterProduct);}

    public MasterProduct editMasterProduct(Long id, MasterProduct masterProduct) {
        return masterProductRepo.findById(id).map(mProduct->{
            mProduct.setTradeName(masterProduct.getTradeName());
            mProduct.setScientificName(masterProduct.getScientificName());
            mProduct.setActiveIngredients(masterProduct.getActiveIngredients());
            mProduct.setForm(masterProduct.getForm());
            mProduct.setBarcode(masterProduct.getBarcode());
            mProduct.setCategories(masterProduct.getCategories());
            mProduct.setConcentration(masterProduct.getConcentration());
            mProduct.setManufacturer(masterProduct.getManufacturer());
            mProduct.setNotes(masterProduct.getNotes());
           return masterProductRepo.save(mProduct);
        }).orElseThrow(()-> new EntityNotFoundException("Master Product With Id" + id + "not found"));
    }

    public void deleteMasterProduct(Long id) {
        if(!masterProductRepo.existsById(id)) {
            throw new EntityNotFoundException("Master Product with ID " + id + " not found!") ;
        }
        masterProductRepo.deleteById(id);}
}
