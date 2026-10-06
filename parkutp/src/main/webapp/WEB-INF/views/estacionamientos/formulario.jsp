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

<a href="<c:url value='/estacionamientos'/>" class="back-link">← Volver a estacionamientos</a>
<div class="form-panel mt-3"><div class="eyebrow">CONFIGURACIÓN</div><h1 class="h2">${empty id ? 'Nuevo estacionamiento' : 'Editar estacionamiento'}</h1><p class="text-secondary">Define la zona y su capacidad. Se crearán los accesos de ingreso y salida.</p>
<c:if test="${empty sedes}"><div class="alert alert-warning">Necesitas una sede. <a href="<c:url value='/jsf/sedes.xhtml'/>">Regístrala aquí</a>.</div></c:if>
<c:url var="accion" value="${empty id ? '/estacionamientos' : '/estacionamientos/'.concat(id)}"/>
<form:form modelAttribute="form" action="${accion}" method="post">
<form:errors path="*" element="div" cssClass="alert alert-danger"/>
<div class="mb-3"><label for="sedeId" class="form-label">Sede</label><form:select path="sedeId" id="sedeId" cssClass="form-select"><form:option value="" label="Selecciona una sede"/><form:options items="${sedes}" itemValue="id" itemLabel="nombre"/></form:select></div>
<div class="mb-3"><label for="nombre" class="form-label">Nombre del estacionamiento</label><form:input path="nombre" id="nombre" cssClass="form-control" maxlength="120" placeholder="Ej.: Estacionamiento principal"/></div>
<div class="mb-3"><label for="capacidad" class="form-label">Capacidad total de vehículos</label><form:input path="capacidad" id="capacidad" type="number" min="1" max="10000" cssClass="form-control"/><div class="form-text">No se puede reducir por debajo de la ocupación actual.</div></div>
<div class="form-check mb-4"><form:checkbox path="activo" id="activo" cssClass="form-check-input"/><label for="activo" class="form-check-label">Habilitado para recibir vehículos</label></div>
<div class="d-flex gap-2"><button class="btn btn-danger" type="submit">Guardar estacionamiento</button><a class="btn btn-outline-secondary" href="<c:url value='/estacionamientos'/>">Cancelar</a></div>
</form:form></div>

</main><footer class="container pb-4 text-secondary small">ParkUTP · Prototipo académico APF2 · Accesos separados de ingreso y salida</footer></body></html>
