package com.utp.parkutp.repository;
import com.utp.parkutp.entity.SedeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SedeRepository extends JpaRepository<SedeEntity,String>{
 List<SedeEntity> findAllByOrderByNombreAsc();
}
