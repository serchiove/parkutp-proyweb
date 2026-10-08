package com.utp.parkutp.repository;
import com.utp.parkutp.entity.AccesoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface AccesoRepository extends JpaRepository<AccesoEntity,Long>{
 List<AccesoEntity> findByEstacionamientoIdOrderById(long id);
 Optional<AccesoEntity> findByEstacionamientoIdAndTipo(long id,String tipo);
}
