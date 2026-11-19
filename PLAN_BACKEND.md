# Plan de trabajo – Backend (Examen Final Programación Web Full Stack)

**Responsable:** Desarrollador Backend
**Proyecto:** `backend2026-83682` (Spring Boot 4.0.4, Java 21, Spring Data JPA, MySQL 8.4)
**Documento base:** `../EXAMEN FINAL.pdf`
**Trabaja en paralelo con:** Frontend (`../frontend26-83682/PLAN_FRONTEND.md`) y Documentación (`../PLAN_DOCUMENTACION.md`)

> Nota: `PLAN_TRABAJO.md` de esta carpeta corresponde al segundo parcial y ya no aplica. Este plan lo reemplaza para el examen final.

---

## 1. Punto de partida (estado actual)

| Existe | No existe todavía |
|---|---|
| Capas `api / apicontroller / service / serviceimpl / repository / entity / models` | Seguridad (login, roles, permisos) |
| Entidades: `usuario`, `especializacion`, `cliente`, `raza`, `mascota`, `medico`, `medicamento`, `cita`, `historia_medica`, `anotacion_historia`, `formula_medica` | Tablas nuevas: lote, movimiento de inventario, horario de veterinario, tarifa, factura, pago, despacho, etc. |
| CRUD básico de citas, historias, anotaciones, fórmulas | Swagger/OpenAPI, generación de PDF y Excel, carga masiva |
| `GlobalExceptionHandler` y `docker-compose.yml` (solo la BD) | Dockerfile del backend y servicio `backend` en compose |

**Dependencias a agregar al `pom.xml`:** `springdoc-openapi-starter-webmvc-ui` (Swagger), Spring Security + JWT (o Basic), Apache POI (Excel/CSV), OpenPDF o iText (PDF), `spring-boot-starter-validation`.

**Deuda técnica conocida (de `CLAUDE.md`):** el código lanza `org.apache.coyote.BadRequestException` en vez de `com.uniminuto.clinica.exception.BadRequestException`, por lo que los errores de validación devuelven 500 en lugar de 400. **Corregir en la Fase 0**, antes de agregar más reglas.

---

## 2. Reglas de trabajo

- Cada dominio nuevo replica las 5 capas: `api` → `apicontroller` → `service` → `serviceimpl` → `repository`.
- Todo cambio de BD va en un script SQL numerado dentro de `sql/` (`V01__...sql`) **y** se refleja en `schema.sql` para que Docker lo cargue. `ddl-auto=none`, no se confía en Hibernate para crear tablas.
- Operaciones críticas (dispensación, facturación, pagos, carga masiva) con `@Transactional`.
- Los permisos se validan **en el backend** (`@PreAuthorize` o filtro), nunca solo ocultando opciones en el frontend.
- Cada endpoint nuevo lleva anotaciones Swagger (`@Operation`, `@ApiResponse`) y Javadoc. Esto alimenta directamente el entregable 4.
- Rama por funcionalidad (`feature/inventario`, `feature/despacho`…), pull request revisado por el desarrollador Frontend.
- **Contrato primero:** al iniciar cada módulo, publicar la lista de endpoints y DTOs (aunque devuelvan datos de ejemplo) para que Frontend y Documentación no esperen.

---

## 3. Fases y tareas

### Fase 0 – Base técnica y transversales (primero, todo el equipo depende de esto)

- [x] Corregir el uso de `BadRequestException` (usar la de `com.uniminuto.clinica.exception`) y verificar que `GlobalExceptionHandler` devuelve 400/404/409 coherentes.
- [x] Agregar dependencias (Swagger, validation, POI, OpenPDF, Security).
- [x] Configurar Swagger en `/clinica/v1/swagger-ui.html` y agrupar por módulo.
- [x] **Seguridad y roles:** tabla `rol` (o campo `rol` en `usuario`) con `ADMIN`, `SECRETARIA`, `MEDICO`, `DESPACHO_MEDICO`. Login que devuelva token; filtro que resuelva el usuario y su rol en cada request. Guardar el usuario autenticado para auditoría (quién hizo cada operación).
- [x] Seeds de usuarios de prueba por rol (con contraseñas ficticias) en `schema.sql`.
- [x] `Dockerfile` multi-stage del backend + servicio `backend` en `docker-compose.yml` (con `depends_on` de la BD saludable). El frontend se agrega a ese compose junto con el desarrollador Frontend.
- [ ] Entregar a Documentación: URL de Swagger, usuarios de prueba y comando `docker compose up` (ver sección «Referencia rápida» abajo).

### Fase 1 – Requerimiento 1: Inventario, lotes y carga masiva (20 %)

**Modelo de datos (ajustar `medicamento` y crear tablas nuevas):**

| Cambio | Detalle |
|---|---|
| `medicamento` (ampliar) | `codigo` (8 alfanuméricos, único), `registro_invima` (obligatorio si es medicamento, opcional si es utensilio), `tipo` (`MEDICAMENTO`/`UTENSILIO`), `precio_compra`, `precio_venta`, `stock_actual`, `stock_minimo`, `fecha_ingreso`, `fecha_proximo_pedido`, `activo` |
| `lote_medicamento` (nueva) | `medicamento_id`, `numero_lote`, `cantidad_disponible`, `precio_compra`, `fecha_compra`, `fecha_vencimiento` |
| `movimiento_inventario` (nueva) | `medicamento_id`, `lote_id`, `tipo` (`ENTRADA`/`SALIDA`), `cantidad`, `fecha`, `usuario_id`, `formula_medica_id` (nullable), `despacho_id` (nullable), `motivo` |
| `carga_masiva` + `carga_masiva_detalle` (nuevas) | Log: archivo, usuario, fecha, total/válidos/rechazados, fila y motivo del error |

> Decisión a documentar: el stock del medicamento es la suma de sus lotes vigentes; `stock_actual` se mantiene sincronizado o se calcula (elegir una y justificarla).

**Endpoints (`/medicamento`, `/inventario`):**
- [x] CRUD de medicamentos/productos + listado filtrable (agotados, stock bajo, próximos a vencer, vencidos).
- [x] CRUD de lotes y entrada de inventario (genera movimiento `ENTRADA`).
- [x] `GET /inventario/movimientos?medicamentoId=` historial de entradas y salidas.
- [x] **Carga masiva** (`multipart`, `.xlsx` y `.csv`, 50+ registros):
  - `POST /inventario/carga-masiva/previsualizar` → valida encabezados, obligatorios, duplicados (código/nombre), precios, cantidades y fechas; devuelve filas correctas y filas con error (fila + motivo). **No guarda nada.**
  - `POST /inventario/carga-masiva/confirmar` → importa solo las filas válidas, cada registro aislado para que un error no afecte a los demás; registra usuario, fecha y log.
  - `GET /inventario/carga-masiva/{id}/errores` → informe de errores descargable.
  - `GET /inventario/carga-masiva` → historial de cargas.
- [x] Plantilla descargable de ejemplo del archivo de carga (para Frontend y Documentación).

**Reglas:** stock nunca negativo, vencimiento posterior a la compra, precio venta ≥ 0, Invima obligatorio según `tipo`, código de 8 caracteres `[A-Za-z0-9]`.

### Fase 2 – Requerimiento 2: Citas, horarios y calendario (20 %)

**Modelo de datos:**

| Cambio | Detalle |
|---|---|
| `horario_veterinario` (nueva) | `medico_id`, `dia_semana`, `hora_inicio`, `hora_fin`, `activo` |
| `estado_cita` (nueva o enum) | `PENDIENTE`, `CONFIRMADA`, `ATENDIDA`, `CANCELADA`, `FINALIZADA` |
| `cita` (ampliar) | `fecha_hora_inicio`, `fecha_hora_fin`, `motivo`, `estado`, `motivo_cancelacion`, `especializacion_id`; relación obligatoria con mascota, propietario y médico |

**Endpoints (`/cita`, `/horario-veterinario`):**
- [x] `GET /cita/calendario?medicoId&desde&hasta` → citas del veterinario autenticado para vistas día/semana/mes (devuelve datos listos para el calendario: id, título, inicio, fin, estado, mascota, propietario, motivo).
- [x] Registrar, editar, reprogramar (cambio de fecha/hora), marcar atendida, cancelar (conserva historial, no borra).
- [x] `GET /cita/historial-mascota?mascotaId=`.
- [x] CRUD de horarios de disponibilidad por veterinario.
- [x] `GET /cita/reporte-excel?medicoId&desde&hasta` → `.xlsx` con las citas del médico.
- [x] Resumen por estado (pendientes, confirmadas, canceladas, finalizadas).

**Reglas de negocio (validar en `CitaServiceImpl`):**
1. Un veterinario no puede tener dos citas que se solapen.
2. Una mascota no puede tener dos citas simultáneas.
3. No se asignan citas a médicos inactivos.
4. No se programan citas fuera del horario del veterinario.
5. Citas canceladas conservan historial.
6. Cada cita enlaza mascota + propietario + médico, y la mascota debe pertenecer al propietario.
7. El médico solo ve y gestiona **sus** citas; secretaria/admin ven todas (permiso en backend).

### Fase 3 – Requerimiento 3: Atención médica, fórmulas y facturación (20 %)

**Modelo de datos:**

| Tabla | Contenido clave |
|---|---|
| `tarifa` | `especializacion_id`, `servicio` (tipo de consulta/procedimiento), `valor`, `iva_porcentaje`, `activa` |
| `tarifa_log` | `tarifa_id`, valor anterior/nuevo, usuario, fecha (historial de cambios) |
| `atencion_medica` | `cita_id`, motivo, síntomas, diagnóstico, observaciones, recomendaciones, `estado`, fechas |
| `formula_medica` (ampliar) | `estado_despacho`, `atencion_id`; **nueva** `formula_medica_detalle` (`medicamento_id`, dosis, frecuencia, duración, cantidad prescrita, vía de administración, indicaciones) |
| `factura` | `numero`, cita/atención, propietario, mascota, veterinario, especialidad, subtotal, impuestos, descuento, total, fecha, `estado_pago` (`PENDIENTE`, `PARCIAL`, `PAGADA`, `ANULADA`) |
| `factura_detalle` | servicio o medicamento, cantidad, **precio unitario congelado**, IVA aplicado |
| `pago` | `factura_id`, valor, método, fecha, usuario |

**Endpoints:**
- [x] `/especializacion` (activar/inactivar) y `/tarifa` (CRUD con registro en `tarifa_log`; el admin cambia valores sin tocar código).
- [x] `/atencion` abrir desde una cita, registrar síntomas/diagnóstico/observaciones/recomendaciones, finalizar (queda en el historial clínico de la mascota).
- [x] `/formula-medica` crear con varios medicamentos, consultar disponibilidad, **PDF de la fórmula** (`GET /formula-medica/{id}/pdf`). **Crear una fórmula NO descuenta inventario.**
- [x] **Prefactura PDF** al finalizar la atención (servicios + medicamentos formulados).
- [x] `/factura` generar, consultar, anular (procedimiento controlado, nunca borrar), **PDF** (`GET /factura/{id}/pdf`) con todos los campos del enunciado.
- [x] `/pago` registrar pagos (parcial o total); recalcula `estado_pago`.
- [x] Historial de facturación por propietario y por mascota.

**Reglas:** una cita atendida puede generar factura; no cobrar dos veces el mismo servicio por una atención; la tarifa se copia a la factura (cambios posteriores no la alteran); IVA según la tarifa/producto; solo se facturan medicamentos **efectivamente despachados**; pagos siempre ligados a una factura; la anulación conserva el histórico.

### Fase 4 – Requerimiento 5: Despacho y dispensación (20 %)

**Modelo de datos:** `despacho` (fórmula, estado, usuario, fechas), `despacho_detalle` (medicamento, lote, cantidad solicitada/entregada), `despacho_estado_log` (historial de cambios de estado con usuario), reutiliza `lote_medicamento` y `movimiento_inventario`.

**Estados:** `PENDIENTE_DESPACHO` → `EN_PREPARACION` → `DESPACHO_PARCIAL` / `DESPACHADO` / `PENDIENTE_DISPONIBILIDAD` / `CANCELADO`.

**Endpoints (`/despacho`, solo rol `DESPACHO_MEDICO`):**
- [x] `GET /despacho/pendientes` con búsqueda por id de fórmula, propietario, mascota o fecha.
- [x] `GET /despacho/formula/{id}` detalle con existencias, lotes disponibles, vencidos y próximos a vencer.
- [x] `POST /despacho/{id}/preparar` y sugerencia automática de lotes (**FEFO**: primero el que vence antes, solo vigentes).
- [x] `POST /despacho/{id}/confirmar` — **transaccional**: valida existencias y vencimientos, registra entregado vs. solicitado, descuenta del lote, crea movimientos `SALIDA` ligados a despacho + fórmula, registra usuario/fecha/hora, genera comprobante.
- [x] `GET /despacho/{id}/comprobante-pdf`.
- [x] `POST /despacho/{id}/cancelar` y `/reversar` (solo con autorización, deja traza; no se elimina).
- [x] `GET /despacho/historial` y `GET /despacho/historial-medicamento?medicamentoId=`.

**Las 12 reglas del enunciado (5.8)** deben tener cada una su validación y su prueba: perfil exclusivo; el médico crea/confirma la fórmula; despacho no modifica dosis/cantidades; inventario solo se descuenta al confirmar; no vencidos; no más de lo disponible; entrega parcial permitida; toda salida enlazada a despacho y fórmula; **no doble descuento** (idempotencia); usuario/fecha/hora en toda operación; confirmadas no se eliminan (solo reversión autorizada); permisos validados en backend.

**Restricciones de acceso del perfil `DESPACHO_MEDICO`:** solo lectura de inventario + módulo despacho. Debe recibir 403 en citas, atención, historias, fórmulas (crear/editar), facturación, tarifas, reportes y usuarios.

### Fase 5 – Requerimiento 4: Reportes PDF y Excel (10 %)

Un endpoint por reporte, parámetros comunes `desde`, `hasta` y filtros propios; cada uno con modo **JSON** (previsualización) y **exportar** (`?formato=pdf|xlsx`). Encabezado, fecha de generación y totales en todos.

- [x] **Financieros:** ingresos por período, por consultas, por especialidad, por veterinario, por venta de medicamentos; total de facturas, pagadas, pendientes, pagos parciales, impuestos, descuentos; resumen por día/mes/año.
- [x] **Inventario:** existencias, valor a costo, valor potencial de venta, agotados, stock mínimo, próximos a vencer, vencidos, entradas/salidas, historial por medicamento.
- [x] **Gestión médica:** citas por período, atendidas/canceladas/pendientes, consultas por veterinario y por especialidad, mascotas atendidas.
- [x] Acceso restringido a `ADMIN` (y roles autorizados); coherencia con facturas, pagos y movimientos (los totales deben cuadrar entre reportes).

### Fase 6 – Cierre, pruebas y despliegue

- [x] Pruebas unitarias de las reglas críticas (solapamiento de citas, stock negativo, doble descuento, FEFO, permisos por rol, facturación).
- [x] Prueba end-to-end de la cadena: carga masiva → cita → atención → fórmula → prefactura → despacho → factura → pago → reporte (ejecutada con curl contra MariaDB; ver sección de pruebas).
- [x] Colección Postman actualizada (`postman_collection.json`) con login y variables de entorno.
- [x] Javadoc en el 100 % de clases/métodos nuevos; limpiar código muerto.
- [x] `docker compose up --build` desde cero levanta BD + backend sin pasos manuales (verificado con MySQL 8.4; el frontend se agrega al compose con el otro desarrollador). Si el puerto 8080 está ocupado: `BACKEND_PORT=8081 docker compose up --build`. Si ya existía un volumen antiguo: `docker compose down -v` primero para que se recargue `schema.sql`.
- [ ] Revisar con Documentación que Swagger describe todos los endpoints.

---

## 4. Orden y dependencias sugeridas

```
Fase 0 ──► Fase 1 (inventario) ──► Fase 4 (despacho)  ──┐
   │                                                      ├─► Fase 5 (reportes) ─► Fase 6
   └────► Fase 2 (citas) ──► Fase 3 (atención/factura) ───┘
```

- La **Fase 0** desbloquea a todos (roles, Swagger, Docker).
- **Inventario** (lotes + movimientos) es prerrequisito de **Despacho** y de los reportes de inventario.
- **Facturación** necesita atención y fórmula; el despacho alimenta qué medicamentos se pueden facturar.
- Fases 1 y 2 pueden avanzar en paralelo si Frontend empieza por una mientras Backend hace la otra.

## 5. Entregables del Backend

1. Scripts SQL (`schema.sql` actualizado + scripts de migración numerados) y el diagrama ER (lo dibuja Documentación con tu apoyo).
2. API REST completa de los 5 módulos con seguridad por rol.
3. Swagger publicado y completo.
4. Dockerfile + `docker-compose.yml` funcional.
5. Colección Postman, usuarios de prueba por rol y pruebas de reglas críticas.

## 6. Qué necesita Documentación de ti (y cuándo)

| Cuándo | Qué entregar |
|---|---|
| Al terminar el diseño de cada módulo | Tablas nuevas/modificadas + relaciones (para el ER y la justificación) |
| Al publicar cada endpoint | Que aparezca en Swagger con descripción y ejemplos |
| Al implementar reglas de negocio | Lista breve de la regla y dónde se valida |
| Fase 0 | Usuarios de prueba por rol, comandos de ejecución, versiones |
| Fase 6 | Capturas/evidencia: carga masiva 50+ filas, descuento de inventario por despacho, PDFs |

---

## 7. Referencia rápida (implementado)

**Arranque:** `docker compose up --build` (BD en 3307, API en `http://localhost:8080/clinica/v1`) o `./mvnw spring-boot:run` con la BD activa.
**Swagger:** `http://localhost:8080/clinica/v1/swagger-ui.html` (7 grupos por módulo; botón *Authorize* con el token de `POST /auth/login`).
**Usuarios de prueba** (contraseña `Clinica2026*`): `admin`, `secretaria`, `medico1` (médico 1), `medico2` (médico 2), `despacho`.
**Scripts SQL:** `schema.sql` (instalación completa) y `sql/V01`, `V02`, `V03` (migración de una BD existente).

### Decisiones de diseño
- **Stock:** `medicamento.stock_actual` se mantiene sincronizado (suma de lotes vigentes) en la misma transacción de cada movimiento; permite filtrar agotados/stock bajo sin sumar lotes en cada listado.
- **Carga masiva:** `previsualizar` no guarda nada; `confirmar` recibe el archivo de nuevo, revalida e importa cada fila en su propia transacción (`REQUIRES_NEW`). Cada fila crea un producto nuevo (código y nombre únicos) con su lote inicial.
- **Citas:** `cita.fecha_hora` es el inicio; `fecha_hora_fin` = inicio + duración (30 min por defecto). Estados en mayúscula: PENDIENTE, CONFIRMADA, ATENDIDA, CANCELADA, FINALIZADA.
- **Fórmula → despacho:** al crear la fórmula se crea automáticamente un `despacho` PENDIENTE_DESPACHO sin tocar inventario. El inventario solo baja en `POST /despacho/{id}/confirmar` (FEFO, solo lotes vigentes, bloqueo pesimista del despacho y los lotes → sin doble descuento).
- **Cancelar/reversar despacho:** requiere motivo y credenciales de un ADMIN en el cuerpo (queda en `despacho_estado_log`).
- **Facturación:** una factura vigente por cita; precio unitario e IVA congelados; solo medicamentos con `cantidad_entregada > 0`. La anulación exige que no haya pagos.
- **Roles (backend):** matriz por URL en `SecurityConfig` + `@PreAuthorize`. `DESPACHO_MEDICO` solo lee inventario y usa `/despacho/**` (403 en el resto); `MEDICO` solo ve y gestiona sus citas, atenciones y fórmulas.

### Dónde se valida cada regla
| Regla | Lugar |
|---|---|
| Citas sin solape (médico y mascota), médico activo, horario, mascota del propietario | `CitaServiceImpl.validarReglas` |
| Médico solo ve sus citas | `CitaServiceImpl`, `AtencionServiceImpl`, `FormulaMedicaServiceImpl` vía `UsuarioActual.exigirPropioSiEsMedico` |
| Código 8 alfanuméricos, Invima por tipo, precios ≥ 0 | `MedicamentoServiceImpl.validar` |
| Stock nunca negativo, no descontar vencidos, vencimiento > compra | `InventarioServiceImpl` |
| 12 reglas de despacho | Javadoc de `DespachoServiceImpl` |
| No cobrar dos veces, solo cita atendida | `FacturaServiceImpl.generar` |
| Pago ≤ saldo, siempre ligado a factura | `PagoServiceImpl.registrar` |

### Pruebas
`./mvnw test`: `SeguridadWebTest` (login, 401, 403 por rol) y `ReglasCriticasTest` (stock negativo, lote vencido, doble descuento, entrega parcial, doble cobro, pago > saldo). Colección Postman actualizada en `postman_collection.json` (el login guarda `{{token}}`).
Nota: con MariaDB como BD de prueba use `SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT=org.hibernate.dialect.MariaDBDialect` (el `FOR UPDATE OF` del dialecto MySQL 8 no existe en MariaDB); con MySQL 8.4 no hace falta.
