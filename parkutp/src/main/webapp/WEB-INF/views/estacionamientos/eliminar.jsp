<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>ParkUTP · Arequipa</title>
<link rel="stylesheet" href="<c:url value='/webjars/bootstrap/5.3.3/css/bootstrap.min.css'/>">
<link rel="stylesheet" href="<c:url value='/css/parkutp.css'/>">
</head><body>
<nav class="navbar navbar-dark park-nav"><div class="container"><a class="navbar-brand fw-bold" href="<c:url value='/estacionamientos'/>">Park<span>UTP</span><small>AREQUIPA</small></a><div class="d-flex gap-3"><a href="<c:url value='/estacionamientos'/>" class="nav-link text-white">Estacionamientos</a><a href="<c:url value='/jsf/sedes.xhtml'/>" class="nav-link text-white">Sedes</a></div></div></nav>
<main class="container py-4 py-md-5">

<div class="form-panel"><div class="eyebrow">CONFIRMACIÓN</div><h1 class="h2">Eliminar estacionamiento</h1><p>¿Eliminar <strong><c:out value="${estacionamiento.nombre}"/></strong>?</p><p class="text-secondary">Si tiene movimientos o dispositivos asociados, el sistema conservará el registro. Puedes desactivarlo desde Editar.</p>
<form action="<c:url value='/estacionamientos/${estacionamiento.id}/eliminar'/>" method="post"><button type="submit" class="btn btn-danger">Confirmar eliminación</button><a class="btn btn-outline-secondary" href="<c:url value='/estacionamientos'/>">Cancelar</a></form></div>

</main><footer class="container pb-4 text-secondary small">ParkUTP · Prototipo académico APF2 · Accesos separados de ingreso y salida</footer></body></html>
