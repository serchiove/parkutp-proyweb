<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!doctype html><html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>ParkUTP · Arequipa</title>
<link rel="stylesheet" href="<c:url value='/webjars/bootstrap/5.3.3/css/bootstrap.min.css'/>">
<link rel="stylesheet" href="<c:url value='/css/parkutp.css'/>">
</head><body>
<nav class="navbar navbar-dark park-nav"><div class="container"><a class="navbar-brand fw-bold" href="<c:url value='/estacionamientos'/>">Park<span>UTP</span><small>AREQUIPA</small></a><div class="d-flex gap-3"><a href="<c:url value='/estacionamientos'/>" class="nav-link text-white">Estacionamientos</a><a href="<c:url value='/jsf/sedes.xhtml'/>" class="nav-link text-white">Sedes</a></div></div></nav>
<main class="container py-4 py-md-5">

<div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4"><div><div class="eyebrow">CONTROL DEL ESTACIONAMIENTO</div><h1>Un mismo aforo para ambos accesos.</h1><p class="text-secondary mb-0">Consulta la disponibilidad y administra las zonas de estacionamiento.</p></div><a class="btn btn-danger" href="<c:url value='/estacionamientos/nuevo'/>">+ Nuevo estacionamiento</a></div>
<c:if test="${not empty mensaje}"><div class="alert alert-success" role="status"><c:out value="${mensaje}"/></div></c:if>
<c:if test="${not empty error}"><div class="alert alert-danger" role="alert"><c:out value="${error}"/></div></c:if>
<div class="row g-3 mb-4"><div class="col-md-4"><div class="metric"><span>Capacidad activa</span><strong><c:out value="${totalCapacidad}"/></strong></div></div><div class="col-md-4"><div class="metric"><span>Vehículos en zonas activas</span><strong><c:out value="${totalOcupados}"/></strong></div></div><div class="col-md-4"><div class="metric metric-green"><span>Espacios disponibles</span><strong><c:out value="${totalDisponibles}"/></strong></div></div></div>
<c:if test="${empty estacionamientos}"><div class="empty-state"><h2>Configura tu primer estacionamiento</h2><p>Primero registra una sede y luego define la capacidad de la zona.</p><a class="btn btn-outline-dark" href="<c:url value='/jsf/sedes.xhtml'/>">Registrar sede</a></div></c:if>
<div class="row g-4"><c:forEach var="e" items="${estacionamientos}"><div class="col-lg-6"><article class="parking-card">
<div class="d-flex justify-content-between gap-2"><div><p class="text-secondary small mb-1"><c:out value="${e.sedeNombre}"/></p><h2 class="h4"><c:out value="${e.nombre}"/></h2></div><span class="status ${!e.activo ? 'status-off' : e.disponibles == 0 ? 'status-full' : 'status-open'}"><c:choose><c:when test="${!e.activo}">Inactivo</c:when><c:when test="${e.disponibles == 0}">Lleno</c:when><c:otherwise>Disponible</c:otherwise></c:choose></span></div>
<p class="availability"><strong><c:out value="${e.disponibles}"/></strong> / <c:out value="${e.capacidad}"/> disponibles</p>
<div class="progress mb-2" role="progressbar" aria-label="Ocupación" aria-valuenow="${e.ocupados}" aria-valuemin="0" aria-valuemax="${e.capacidad}"><div class="progress-bar bg-danger" style="width:${e.porcentaje}%"></div></div><p class="small text-secondary"><c:out value="${e.ocupados}"/> vehículos dentro · Un acceso de entrada y otro de salida</p>
<div class="d-flex gap-2 flex-wrap"><a class="btn btn-dark btn-sm" href="<c:url value='/estacionamientos/${e.id}/operacion'/>">Ver operación</a><a class="btn btn-outline-secondary btn-sm" href="<c:url value='/estacionamientos/${e.id}/editar'/>">Editar</a><a class="btn btn-outline-danger btn-sm" href="<c:url value='/estacionamientos/${e.id}/eliminar'/>">Eliminar</a></div>
</article></div></c:forEach></div>
<p class="small text-secondary mt-4">Los datos corresponden a la carga de esta página. La pantalla de operación actualiza el aforo cada 5 segundos.</p>

</main><footer class="container pb-4 text-secondary small">ParkUTP · Prototipo académico APF2 · Accesos separados de ingreso y salida</footer></body></html>
