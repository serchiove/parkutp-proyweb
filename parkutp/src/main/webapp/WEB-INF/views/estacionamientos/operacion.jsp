<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
            <!doctype html>
            <html lang="es">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width,initial-scale=1">
                <title>ParkUTP · Arequipa</title>
                <link rel="stylesheet" href="<c:url value='/webjars/bootstrap/5.3.3/css/bootstrap.min.css'/>">
                <link rel="stylesheet" href="<c:url value='/css/parkutp.css'/>">
            </head>

            <body>
                <nav class="navbar navbar-dark park-nav">
                    <div class="container"><a class="navbar-brand fw-bold"
                            href="<c:url value='/estacionamientos'/>">Park<span>UTP</span><small>AREQUIPA</small></a>
                        <div class="d-flex gap-3"><a href="<c:url value='/estacionamientos'/>"
                                class="nav-link text-white">Estacionamientos</a><a
                                href="<c:url value='/jsf/sedes.xhtml'/>" class="nav-link text-white">Sedes</a></div>
                    </div>
                </nav>
                <main class="container py-4 py-md-5">

                    <a href="<c:url value='/estacionamientos'/>" class="back-link">← Volver al panel</a>
                    <div id="operacion" data-id="${estacionamiento.id}"
                        data-api="<c:url value='/api/estacionamientos/${estacionamiento.id}'/>">
                        <div class="d-flex justify-content-between flex-wrap gap-3 mt-3 mb-4">
                            <div>
                                <div class="eyebrow">OPERACIÓN · DOS ACCESOS</div>
                                <h1>
                                    <c:out value="${estacionamiento.nombre}" />
                                </h1>
                                <p class="text-secondary">
                                    <c:out value="${estacionamiento.sedeNombre}" /> · <span id="conexion"
                                        aria-live="polite">Actualizando…</span>
                                </p>
                            </div><span id="luz" class="status status-off">Consultando aforo</span>
                        </div>
                        <div id="aviso" role="status" aria-live="polite"></div>
                        <div class="row g-3 mb-4">
                            <div class="col-md-4">
                                <div class="metric"><span>Capacidad</span><strong id="capacidad">
                                        <c:out value="${estacionamiento.capacidad}" />
                                    </strong></div>
                            </div>
                            <div class="col-md-4">
                                <div class="metric"><span>Vehículos dentro</span><strong id="ocupados">
                                        <c:out value="${estacionamiento.ocupados}" />
                                    </strong></div>
                            </div>
                            <div class="col-md-4">
                                <div class="metric metric-green"><span>Disponibles</span><strong id="disponibles">
                                        <c:out value="${estacionamiento.disponibles}" />
                                    </strong></div>
                            </div>
                        </div>
                        <section class="parking-card mb-4" aria-labelledby="titulo-aforo">
                            <div class="d-flex justify-content-between gap-3">
                                <h2 id="titulo-aforo" class="h4">Ocupación del estacionamiento</h2><strong
                                    id="porcentaje">Consultando…</strong>
                            </div>
                            <div id="barra-aforo" class="progress aforo-progress" role="progressbar"
                                aria-label="Porcentaje de ocupación" aria-valuemin="0" aria-valuemax="100">
                                <div id="avance-aforo" class="progress-bar bg-secondary"></div>
                            </div>
                            <p id="detalle-aforo" class="text-secondary mt-3 mb-0">Esperando el aforo actualizado.</p>
                        </section>
                        <div class="parking-card mb-4">
                            <h2 class="h4">Registrar paso de un vehículo</h2>
                            <p class="text-secondary">Cada botón representa un cruce completo por su acceso. El
                                simulador permite probar la futura integración física.</p>
                            <div class="row g-3 mb-3">
                                <div class="col-md-6"><label for="origen" class="form-label">Modo de
                                        registro</label><select id="origen" class="form-select">
                                        <option value="SIMULADOR">Simulador de sensor</option>
                                        <option value="MANUAL">Registro manual de seguridad</option>
                                    </select></div>
                                <div class="col-md-6"><label for="placa" class="form-label">Placa opcional</label><input
                                        id="placa" class="form-control" maxlength="12" placeholder="Ej.: ABC-123">
                                    <div class="form-text">El sensor de paso no identifica placas. Esta versión controla
                                        el aforo total.</div>
                                </div>
                            </div>
                            <div class="row g-3">
                                <div class="col-md-6"><button class="btn btn-success w-100 paso" data-tipo="ENTRADA"
                                        type="button">Acceso de ingreso · Registrar entrada</button></div>
                                <div class="col-md-6"><button class="btn btn-dark w-100 paso" data-tipo="SALIDA"
                                        type="button">Acceso de salida · Registrar salida</button></div>
                            </div>
                            <button id="reintentar" class="btn btn-outline-warning mt-3" type="button" hidden>Reintentar
                                el mismo evento pendiente</button>
                            <p id="restriccion" class="small mt-3 mb-0" aria-live="polite"></p>
                            <p class="small text-secondary mt-3 mb-0">La luz mostrada es virtual. Esta versión todavía
                                no está conectada a dispositivos físicos.</p>
                        </div>
                        <div class="parking-card">
                            <h2 class="h4">Historial de movimientos</h2>
                            <p class="small text-secondary">Hasta 50 registros más recientes que coincidan con los
                                filtros · Hora de Arequipa</p>
                            <form id="filtros" class="row g-3 mb-3">
                                <div class="col-md-3"><label for="filtro-fecha" class="form-label">Fecha</label><input
                                        id="filtro-fecha" name="fecha" type="date" class="form-control"></div>
                                <div class="col-md-3"><label for="filtro-placa" class="form-label">Placa o parte de
                                        ella</label><input id="filtro-placa" name="placa" maxlength="12"
                                        class="form-control" placeholder="Ej.: ABC"></div>
                                <div class="col-md-3"><label for="filtro-tipo"
                                        class="form-label">Movimiento</label><select id="filtro-tipo" name="tipo"
                                        class="form-select">
                                        <option value="">Todos</option>
                                        <option value="ENTRADA">Entrada</option>
                                        <option value="SALIDA">Salida</option>
                                    </select></div>
                                <div class="col-md-3 d-flex align-items-end gap-2"><button class="btn btn-dark"
                                        type="submit">Buscar</button><button class="btn btn-outline-secondary"
                                        type="reset">Limpiar</button></div>
                            </form>
                            <p id="resultado-historial" class="small text-secondary" role="status" aria-live="polite">
                                Consultando historial…</p>
                            <div class="table-responsive">
                                <table class="table align-middle">
                                    <thead>
                                        <tr>
                                            <th scope="col">Hora</th>
                                            <th scope="col">Acceso</th>
                                            <th scope="col">Origen</th>
                                            <th scope="col">Placa</th>
                                        </tr>
                                    </thead>
                                    <tbody id="historial">
                                        <c:forEach var="m" items="${movimientos}">
                                            <tr>
                                                <td>
                                                    <c:out value="${m.fechaTexto}" />
                                                </td>
                                                <td>
                                                    <c:out value="${m.tipo}" />
                                                </td>
                                                <td>
                                                    <c:out value="${m.origen}" />
                                                </td>
                                                <td>
                                                    <c:out value="${m.placa}" default="—" />
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                    <script defer src="<c:url value='/js/operacion.js'/>"></script>

                </main>
                <footer class="container pb-4 text-secondary small">ParkUTP · Prototipo académico APF2 · Accesos
                    separados de ingreso y salida</footer>
            </body>

            </html>