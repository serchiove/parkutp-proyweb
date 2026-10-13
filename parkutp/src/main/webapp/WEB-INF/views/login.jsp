<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Ingresar · ParkUTP</title><link rel="stylesheet" href="<c:url value='/webjars/bootstrap/5.3.3/css/bootstrap.min.css'/>">
<link rel="stylesheet" href="<c:url value='/css/parkutp.css'/>"></head><body>
<main class="container py-5"><div class="form-panel mx-auto"><div class="eyebrow">PARKUTP · AREQUIPA</div>
<h1 class="h2">Inicia sesión</h1><p class="text-secondary">Accede con tu cuenta para consultar u operar los estacionamientos.</p>
<c:if test="${param.error ne null}"><div class="alert alert-danger" role="alert">Usuario o contraseña incorrectos, o cuenta inactiva.</div></c:if>
<c:if test="${param.salida ne null}"><div class="alert alert-success" role="status">Cerraste tu sesión.</div></c:if>
<form action="<c:url value='/login'/>" method="post">
<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
<div class="mb-3"><label class="form-label" for="username">Usuario</label><input id="username" name="username" class="form-control" maxlength="60" autocomplete="username" required autofocus></div>
<div class="mb-4"><label class="form-label" for="password">Contraseña</label><input id="password" name="password" type="password" class="form-control" autocomplete="current-password" required></div>
<button class="btn btn-danger w-100" type="submit">Ingresar</button></form></div></main></body></html>
