package com.utp.parkutp;
import com.utp.parkutp.dto.*;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ValidacionDtoTest {
 @Test void sedeRechazaCodigoInseguroYNombreVacio() {
  try(var factory=Validation.buildDefaultValidatorFactory()) { var dto=new SedeDto();dto.setId("../sede");dto.setNombre(" ");assertEquals(2,factory.getValidator().validate(dto).size()); }
 }
 @Test void movimientoExigeEventoYTipoConocido() {
  try(var factory=Validation.buildDefaultValidatorFactory()) { var dto=new MovimientoDto();dto.setTipo("OTRO");dto.setOrigen("SIMULADOR");assertEquals(2,factory.getValidator().validate(dto).size()); }
 }
}
