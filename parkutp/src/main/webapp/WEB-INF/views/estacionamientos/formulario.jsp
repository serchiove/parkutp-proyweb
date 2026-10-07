<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
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
<form action="<c:out value='${accion}'/>" method="post">
<c:if test="${not empty errores}"><div class="alert alert-danger" role="alert"><ul class="mb-0"><c:forEach var="error" items="${errores}"><li><c:out value="${error}"/></li></c:forEach></ul></div></c:if>
<div class="mb-3"><label for="sedeId" class="form-label">Sede</label><select name="sedeId" id="sedeId" class="form-select">
<option value="">Selecciona una sede</option>
<c:forEach var="sede" items="${sedes}"><option value="<c:out value='${sede.id}'/>" ${sede.id eq valores.sedeId ? 'selected' : ''}><c:out value="${sede.nombre}"/></option></c:forEach>
</select></div>
<div class="mb-3"><label for="nombre" class="form-label">Nombre del estacionamiento</label><input name="nombre" value="<c:out value='${valores.nombre}'/>" id="nombre" class="form-control" maxlength="120" placeholder="Ej.: Estacionamiento principal"/></div>
<div class="mb-3"><label for="capacidad" class="form-label">Capacidad total de vehículos</label><input name="capacidad" value="<c:out value='${valores.capacidad}'/>" id="capacidad" type="number" min="1" max="10000" class="form-control"/><div class="form-text">No se puede reducir por debajo de la ocupación actual.</div></div>
<input type="hidden" name="_activo" value="on"/>
<div class="form-check mb-4"><input type="checkbox" name="activo" value="true" id="activo" class="form-check-input" ${form.activo ? 'checked' : ''}/><label for="activo" class="form-check-label">Habilitado para recibir vehículos</label></div>
<div class="d-flex gap-2"><button class="btn btn-danger" type="submit">Guardar estacionamiento</button><a class="btn btn-outline-secondary" href="<c:url value='/estacionamientos'/>">Cancelar</a></div>
</form></div>

</main><footer class="container pb-4 text-secondary small">ParkUTP · Prototipo académico APF2 · Accesos separados de ingreso y salida</footer></body></html>
