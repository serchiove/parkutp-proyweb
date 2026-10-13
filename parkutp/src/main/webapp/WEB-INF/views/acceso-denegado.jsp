<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Acceso denegado · ParkUTP</title><link rel="stylesheet" href="<c:url value='/webjars/bootstrap/5.3.3/css/bootstrap.min.css'/>"></head>
<body><main class="container py-5"><h1>No puedes realizar esta operación</h1>
<p>Tu cuenta no tiene el permiso necesario o el formulario ha vencido.</p>
<a class="btn btn-dark" href="<c:url value='/estacionamientos'/>">Volver al panel</a></main></body></html>
