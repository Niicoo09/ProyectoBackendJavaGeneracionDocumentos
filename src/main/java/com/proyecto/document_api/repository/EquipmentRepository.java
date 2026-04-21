package com.proyecto.document_api.repository;

import com.proyecto.document_api.model.EquipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<EquipmentEntity, String> {
    List<EquipmentEntity> findByTipo(String tipo);
}
