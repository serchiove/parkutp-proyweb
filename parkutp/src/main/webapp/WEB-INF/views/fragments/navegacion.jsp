<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<nav class="navbar navbar-dark park-nav"><div class="container gap-3">
<a class="navbar-brand fw-bold" href="<c:url value='/estacionamientos'/>">Park<span>UTP</span><small>AREQUIPA</small></a>
<div class="d-flex flex-wrap align-items-center gap-3">
<a class="nav-link text-white" href="<c:url value='/estacionamientos'/>">Estacionamientos</a>
<a class="nav-link text-white" href="<c:url value='/jsf/sedes.xhtml'/>">Sedes</a>
<span class="text-white small"><c:out value="${pageContext.request.userPrincipal.name}"/> ·
<c:choose><c:when test="${pageContext.request.isUserInRole('ADMIN')}">Administrador</c:when>
<c:when test="${pageContext.request.isUserInRole('OPERADOR')}">Operador</c:when><c:otherwise>Consulta</c:otherwise></c:choose></span>
<form action="<c:url value='/logout'/>" method="post" class="m-0">
<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
<button type="submit" class="btn btn-outline-light btn-sm">Cerrar sesión</button></form></div></div></nav>
