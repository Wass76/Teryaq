package com.Teryaq.user.repository;

import com.Teryaq.user.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AreaRepository extends JpaRepository<Area, Long> {

    Optional<Area> findByName(String name);

    List<Area> findByIsActiveTrue();

    List<Area> findByIsActive(Boolean isActive);

    boolean existsByName(String name);
}
