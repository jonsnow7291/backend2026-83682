# Memoria técnica - Integrante 2
## Módulo de Historias Clínicas y Anotaciones

## 1. Modelo de datos utilizado

### historia_medica
- `id` (PK, autogenerado).
- `paciente_id` (FK hacia `mascota.mascota_id`).
- `fecha_creacion`.

### anotacion_historia
- `id` (PK, autogenerado).
- `historia_id` (FK hacia `historia_medica.id`).
- `medico_id` (FK hacia `medico.id`).
- `fecha`.
- `descripcion`.

## 2. Endpoints implementados

### Historias Médicas (`/historia-medica`)
- `GET /listar` - Lista historias médicas (todas o filtradas por fecha).
- `POST /guardar` - Registra una nueva historia médica.
- `POST /actualizar` - Actualiza una historia médica existente.
- `POST /eliminar?historiaId={id}` - Elimina una historia médica.
- `GET /consultar?historiaId={id}` - Consulta una historia médica por ID.

### Anotaciones de Historia (`/anotacion-historia`)
- `GET /listar?historiaId={id}` - Lista las anotaciones de una historia, de la más reciente a la más antigua.
- `POST /guardar` - Registra una nueva anotación clínica.
- `POST /actualizar` - Actualiza una anotación existente.
- `GET /consultar?anotacionId={id}` - Consulta una anotación por ID.

## 3. Reglas de negocio implementadas

- La mascota asociada a la historia médica debe existir.
- Cuando se envía `clienteId`, se valida que la mascota seleccionada pertenezca al cliente indicado (`mascota.getCliente().getClienteId().equals(clienteId)`).
- Cuando se envía `citaId`, se valida que la cita exista, corresponda a la mascota indicada y esté en estado `atendida`.
- Las anotaciones exigen la existencia de la historia médica y del médico.
- Las anotaciones se ordenan de forma descendente por fecha (de la más reciente a la más antigua).

## 4. Decisiones técnicas

- Se reutiliza la excepción propia `com.uniminuto.clinica.exception.BadRequestException` para que `GlobalExceptionHandler` devuelva respuestas 400 controladas.
- Los campos `clienteId` y `citaId` del DTO `HistoriaMedicaRq` son opcionales, lo que mantiene compatibilidad con las pruebas existentes y activa las validaciones cuando el frontend los proporciona.
- El frontend usa formularios reactivos, modales de Bootstrap y combos dependientes (Cliente -> Mascota).
- La memoria visual de anotaciones (modal "Ver Historial") se carga bajo demanda desde el servicio de anotaciones.

---

# Memoria técnica - Integrante 3
## Módulo de Fórmulas Médicas y Medicamentos

## 1. Modelo de datos utilizado

### formula_medica
- `id` (PK, autogenerado).
- `cita_id` (FK hacia `cita.id`): consulta en la que se emite la fórmula. A través de la cita se obtienen la mascota, el cliente y el veterinario.
- `medicamento_id` (FK hacia `medicamento.id`).
- `dosis`, `frecuencia`, `duracion_dias`, `indicaciones`.
- `fecha_emision`: fecha clínica de la prescripción.
- `fecha_creacion_registro`, `fecha_actualizacion_registro`: auditoría del registro.

### medicamento
- `id`, `nombre`, `descripcion`, `presentacion`, `fecha_compra`, `fecha_vence`, fechas de registro.

> Las columnas `frecuencia`, `duracion_dias` y `fecha_emision` son nuevas: `schema.sql` ya las incluye y `migracion_parcial.sql` las agrega en bases de datos creadas antes del cambio.

## 2. Endpoints implementados

### Fórmulas Médicas (`/formula-medica`)
- `GET /listar` - Lista todas las fórmulas, de la más reciente a la más antigua.
- `GET /consultar?formulaId={id}` - Consulta el detalle completo de una fórmula.
- `POST /guardar` - Registra una fórmula (cuerpo `FormulaMedicaRq`).
- `PUT /actualizar` - Actualiza una fórmula (cuerpo `FormulaMedicaRq` con `formulaId`).
- Las citas atendidas del combo dependiente se obtienen de `GET /cita/listar-atendidas-por-mascota?mascotaId={id}`.

### Medicamentos (`/medicamento`)
- `GET /listar-todos` - Lista el catálogo de medicamentos.

## 3. Reglas de negocio implementadas

- La mascota debe existir y pertenecer a un cliente registrado.
- La cita debe existir y pertenecer a la mascota indicada: no se puede asociar una fórmula a la consulta de otra mascota.
- El veterinario es el de la cita, que debe tenerlo registrado.
- La cita debe estar en estado `atendida`: solo se prescribe sobre una consulta realizada.
- La fecha de emisión (por defecto, la actual) no puede ser anterior a la fecha de la consulta.
- El medicamento debe existir y no estar vencido en la fecha de emisión.
- Dosis y frecuencia son obligatorias (frecuencia máx. 100 caracteres); la duración debe estar entre 1 y 365 días.
- No se expone eliminación física: la fórmula emitida forma parte del historial clínico de la mascota.

## 4. Decisiones técnicas

- Se usa la excepción propia `com.uniminuto.clinica.exception.BadRequestException`, así `GlobalExceptionHandler` responde 400 con un mensaje entendible que el frontend muestra con SweetAlert2.
- El veterinario no se envía en la solicitud: se toma de la cita, lo que evita inconsistencias entre la fórmula y la consulta.
- `mascotaId` se envía junto con `citaId` para validar explícitamente la correspondencia cita-mascota.
- El frontend usa un formulario reactivo con combos dependientes (Cliente -> Mascota -> Cita atendida), valida la fecha de emisión también del lado del cliente y muestra el detalle completo en un modal "Ver", distinto del listado resumido.
- El listado usa el DataTable reutilizable (`TablaDatos` + `app-paginador`): búsqueda, ordenamiento por columna (inicial: creación descendente), paginación y estados de carga.


---

# Ajustes transversales (cierre del parcial)

## Uso de métodos HTTP
- `GET` consultas, `POST` creación, `PUT` actualización (`/actualizar` de citas, mascotas, historias, anotaciones y fórmulas), `PATCH` cambio de estado (`/cita/cancelar`) y `DELETE` eliminación (`/historia-medica/eliminar`).

## Servicios agregados
- `GET /cliente/consultar?clienteId={id}` y `GET /mascota/consultar?mascotaId={id}`.
- `GET /cita/listar-atendidas-por-mascota?mascotaId={id}`, compartido por historias y fórmulas.

## Historias clínicas y anotaciones
- `historia_medica` guarda ahora la cita de origen (`cita_id`), `diagnostico` y `observaciones`. De la cita se obtienen veterinario, fecha y hora, motivo y estado para el listado.
- Reglas nuevas: cliente, cita y diagnóstico obligatorios; una cita solo puede tener una historia clínica (evita duplicidad); una historia con anotaciones no puede eliminarse (evita pérdida de información clínica).
- `anotacion_historia` guarda `observacion` y `diagnostico` (diagnóstico o evolución, obligatorio).
- El modal "Ver" de la historia muestra su detalle y el histórico de anotaciones en un DataTable con filtro, orden por fecha (más reciente primero), paginación, detalle de cada anotación y registro de nuevas anotaciones.

## Manejo de errores
- `GlobalExceptionHandler` responde 400 para reglas de negocio, parámetros faltantes o mal formados y JSON inválido; 405 para métodos HTTP no permitidos; y 500 con un mensaje genérico (el detalle técnico solo se registra en el log del servidor).
- En el frontend, `mensajeError` muestra el mensaje del backend o, si no hay respuesta (status 0), un aviso de error de comunicación.

## Frontend reutilizable
- `utils/tabla-datos.ts` (`TablaDatos`): búsqueda sin tildes, ordenamiento por columna y paginación en cliente.
- `demo/shared/paginador`: paginador común con el rango de registros visibles.
- Estados de carga en listados, combos dependientes y botones de guardar.
