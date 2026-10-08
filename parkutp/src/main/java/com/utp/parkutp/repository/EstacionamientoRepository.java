package com.utp.parkutp.repository;
import com.utp.parkutp.entity.EstacionamientoEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface EstacionamientoRepository extends JpaRepository<EstacionamientoEntity,Long>{
 @Query("select e from EstacionamientoEntity e join fetch e.sede order by e.id") List<EstacionamientoEntity> listarConSede();
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select e from EstacionamientoEntity e where e.id=:id")
 Optional<EstacionamientoEntity> bloquear(@Param("id") long id);
}
