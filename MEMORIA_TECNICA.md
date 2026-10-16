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