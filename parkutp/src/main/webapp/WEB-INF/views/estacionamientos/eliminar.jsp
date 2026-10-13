<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>ParkUTP · Arequipa</title>
<link rel="stylesheet" href="<c:url value='/webjars/bootstrap/5.3.3/css/bootstrap.min.css'/>">
<link rel="stylesheet" href="<c:url value='/css/parkutp.css'/>">
</head><body>
<jsp:include page="/WEB-INF/views/fragments/navegacion.jsp"/>
<main class="container py-4 py-md-5">

<div class="form-panel"><div class="eyebrow">CONFIRMACIÓN</div><h1 class="h2">Eliminar estacionamiento</h1><p>¿Eliminar <strong><c:out value="${estacionamiento.nombre}"/></strong>?</p><p class="text-secondary">Si tiene movimientos o dispositivos asociados, el sistema conservará el registro. Puedes desactivarlo desde Editar.</p>
<form action="<c:url value='/estacionamientos/${estacionamiento.id}/eliminar'/>" method="post"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button type="submit" class="btn btn-danger">Confirmar eliminación</button><a class="btn btn-outline-secondary" href="<c:url value='/estacionamientos'/>">Cancelar</a></form></div>

</main><footer class="container pb-4 text-secondary small">ParkUTP · Prototipo académico APF2 · Accesos separados de ingreso y salida</footer></body></html>
