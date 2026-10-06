package com.utp.parkutp.view;
import com.utp.parkutp.dto.SedeDto;
import com.utp.parkutp.model.Sede;
import com.utp.parkutp.service.SedeService;
import com.utp.parkutp.exception.NegocioException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import java.util.List;
// Bean administrado por Spring, resuelto desde JSF mediante JoinFaces.
// RequestScope evita compartir los datos del formulario entre usuarios.
@Component("sedeBean") @RequestScope
public class SedeBean {
 private final SedeService service;
 private final SedeDto form=new SedeDto();
 public SedeBean(SedeService service) { this.service=service; }
 public SedeDto getForm() { return form; }
 public List<Sede> getSedes() { return service.listar(); }
 public String guardar() {
  FacesContext context=FacesContext.getCurrentInstance();
  try {
   service.crear(form);
   context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_INFO,"Sede registrada","Ya puedes asociarle estacionamientos"));
   context.getExternalContext().getFlash().setKeepMessages(true);
   return "/sedes.xhtml?faces-redirect=true";
  } catch(NegocioException ex) { context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR,ex.getMessage(),null));return null; }
 }
}
