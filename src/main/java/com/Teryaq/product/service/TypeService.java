package com.Teryaq.product.service;

import com.Teryaq.product.entity.Type;
import com.Teryaq.product.repo.TypeRepo;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TypeService {

    private final TypeRepo typeRepo;

    public TypeService(TypeRepo typeRepo) {
        this.typeRepo = typeRepo;
    }

    public List<Type> getTypes() {return typeRepo.findAll();}

    public Type getByID(long id) {
        return typeRepo.findById(id)
            .orElseThrow(()->new EntityNotFoundException("Type With Id" + id + "not found")) ;}

    public void insertType(Type type) {typeRepo.save(type);}

    public Type editType(Long id,Type type) {

        return typeRepo.findById(id).map(type1 ->{
                    type1.setName(type.getName());
                    return typeRepo.save(type1);
                })
                .orElseThrow(()->new EntityNotFoundException("Type With Id" + id + "not found"));
    }

    public void deleteType(Long id) {
        if(!typeRepo.existsById(id)) {
            throw new EntityNotFoundException("Type with ID " + id + " not found!") ;
        }
        typeRepo.deleteById(id);}
}
