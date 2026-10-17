# 🐾 Sistema de Gestión de Clínica Veterinaria - Backend

Backend desarrollado con **Spring Boot**, **Spring Data JPA**, **Hibernate** y **MySQL** para el Segundo Parcial de Programación Web. Permite la administración integral de clientes, mascotas, citas médicas, historias clínicas y fórmulas médicas.

---

## 📌 1. Reglas de Negocio Oficiales del Sistema

A continuación se detallan las reglas de negocio exigidas en el documento oficial del parcial (*Docente: Ing. Giovanni Mora Jaimes*):

### 📅 Módulo de Citas Médicas (Sección 2 del Parcial)

| # | Regla de Negocio | Descripción y Criterio de Validación |
| :-: | :--- | :--- |
| **RN-CIT-01** | **Existencia de Cliente** | No se puede registrar ni actualizar una cita para un cliente inexistente en el sistema. |
| **RN-CIT-02** | **Pertenencia Cliente - Mascota** | La mascota seleccionada **debe pertenecer obligatoriamente** al cliente indicado (`mascota.cliente.clienteId == cliente.clienteId`). No es válido asignar una mascota de otro cliente. |
| **RN-CIT-03** | **Existencia de Veterinario** | El médico veterinario asignado debe existir y estar registrado en el sistema. |
| **RN-CIT-04** | **Regla de 15 Minutos del Veterinario** | Un veterinario **no podrá tener dos citas en el mismo horario**, considerando un margen de **15 minutos antes y 15 minutos después** (`[fechaHora - 15min, fechaHora + 15min]`). Citas canceladas no se consideran conflicto. |
| **RN-CIT-05** | **No Citas Simultáneas para la Mascota** | Una misma mascota no puede tener dos citas programadas en la misma fecha y hora exacta. |
| **RN-CIT-06** | **Restricción de Fechas Pasadas** | No se permite programar ni registrar citas en fechas u horas anteriores a la fecha y hora actual del sistema (`fechaHora >= now()`). |
| **RN-CIT-07** | **Ciclo de Vida y Estados Permitidos** | Toda cita debe pertenecer estrictamente a uno de los siguientes estados: `Pendiente`, `Programada`, `Confirmada`, `Atendida`, `Cancelada`. |
| **RN-CIT-08** | **Protección de Cita Cancelada** | Una cita con estado `Cancelada` no podrá ser modificada como si continuara activa. No se permite reactivarla ni cambiar datos sin una operación explícita. |
| **RN-CIT-09** | **Inmutabilidad Histórica de Cita Atendida** | Una cita que ya fue marcada como `Atendida` no permite modificar arbitrariamente su información histórica clave (cliente, mascota, veterinario, fecha y hora). |
| **RN-CIT-10** | **Validación en Backend Obligatoria** | Todas las reglas críticas anteriores se validan obligatoriamente en el backend mediante excepciones estructuradas (`BadRequestException` HTTP 400); no es suficiente con validarlas en Angular. |

---

### 📋 Módulo de Historias Clínicas y Anotaciones (Sección 3 del Parcial - Integrante 2 / Camilo)

A continuación se detallan las reglas de negocio implementadas en el commit de Historias Clínicas y Anotaciones (`c68343f`), con validaciones a nivel de base de datos, DTO y servicios de negocio:

#### 🐾 Historias Médicas (`HistoriaMedicaServiceImpl`)

| # | Regla de Negocio | Descripción y Criterio de Validación | Excepción / Respuesta |
| :-: | :--- | :--- | :--- |
| **RN-HIS-01** | **Existencia de Mascota** | La historia clínica debe asociarse a una mascota existente en el sistema. El identificador `mascotaId` es obligatorio y debe ser mayor a cero. | `BadRequestException`: *"El id de la mascota no puede ser nulo o negativo"* / *"La mascota seleccionada no es valida"* (HTTP 400). |
| **RN-HIS-02** | **Pertenencia Mascota - Cliente** | Cuando se proporciona `clienteId` (procedente del selector dependiente), se valida que el cliente exista y que la mascota seleccionada **pertenezca obligatoriamente** a dicho cliente (`mascota.cliente.clienteId == clienteId`). Previene la alteración cruzada de historias entre clientes. | `BadRequestException`: *"La mascota seleccionada no pertenece al cliente indicado"* (HTTP 400). |
| **RN-HIS-03** | **Vínculo Estricto con Cita Atendida** | Cuando se crea una historia clínica asociada a una cita (`citaId`), el sistema valida tres condiciones: <br>1. La cita debe existir en el sistema.<br>2. La cita debe corresponder a la misma mascota indicada (`cita.mascota.mascotaId == mascota.mascotaId`).<br>3. La cita debe encontrarse en estado estrictamente **`atendida`** (`cita.estado.equalsIgnoreCase("atendida")`). Citas pendientes, canceladas o programadas no permiten abrir historia. | `BadRequestException`: *"La cita seleccionada no corresponde a la mascota indicada"* / *"La cita asociada debe estar en estado atendida"* (HTTP 400). |
| **RN-HIS-04** | **Validación de Rango de Fechas** | En el listado de historias médicas (`/historia-medica/listar`), los parámetros `fechaInicial` y `fechaFinal` son opcionales pero acoplados: si se envía uno, deben enviarse ambos. Además, `fechaInicial` no puede ser posterior a `fechaFinal`. | `BadRequestException`: *"Debe proporcionar tanto la fecha inicial como la fecha final, o ninguna"* / *"La fecha inicial no puede ser posterior a la fecha final"* (HTTP 400). |
| **RN-HIS-05** | **Orden Cronológico Inverso por Defecto** | Las historias clínicas se listan ordenadas cronológicamente de forma descendente por `fechaCreacion DESC`, de la más reciente a la más antigua. | Consulta JPA derivada: `findAllByOrderByFechaCreacionDesc()`. |
| **RN-HIS-06** | **Integridad en Actualización y Eliminación** | Toda operación de actualización (`/actualizar`), consulta (`/consultar`) o eliminación (`/eliminar`) exige un `historiaId` válido y existente. No se permite operar sobre historias inexistentes. | `BadRequestException`: *"El id de la historia medica no puede ser nulo o negativo"* / *"La historia medica seleccionada no es valida"* (HTTP 400). |
| **RN-HIS-07** | **Asignación Automática de Marca Temporal** | La fecha de apertura de la historia clínica se asigna automáticamente en el backend con la fecha/hora del servidor (`LocalDateTime.now()`), impidiendo manipulación manual de fechas de apertura. | Generación automática en `guardarHistoriaMedica`. |

#### 📝 Anotaciones de Historia (`AnotacionHistoriaServiceImpl`)

| # | Regla de Negocio | Descripción y Criterio de Validación | Excepción / Respuesta |
| :-: | :--- | :--- | :--- |
| **RN-ANO-01** | **Asociación a Historia Médica Válida** | Toda anotación o evolución clínica debe asociarse obligatoriamente a una historia médica existente (`historiaId > 0`). | `BadRequestException`: *"El id de la historia medica no puede ser nulo o negativo"* / *"La historia medica seleccionada no es valida"* (HTTP 400). |
| **RN-ANO-02** | **Existencia de Médico Veterinario Responsable** | Toda anotación exige la identificación de un médico veterinario responsable (`medicoId > 0`), validando su existencia en la base de datos antes de registrar o modificar la nota. | `BadRequestException`: *"El id del medico no puede ser nulo o negativo"* / *"El medico seleccionado no es valido"* (HTTP 400). |
| **RN-ANO-03** | **Obligatoriedad de la Descripción Clínica** | La descripción de la evolución, diagnóstico u observación médica es un campo obligatorio y no puede ser nulo ni estar vacío. | `BadRequestException`: *"La descripcion de la anotacion no puede ser nula o vacía"* (HTTP 400). |
| **RN-ANO-04** | **Secuencia Cronológica Descendente de Notas** | El listado de anotaciones de un paciente (`/anotacion-historia/listar`) se entrega ordenado cronológicamente de forma descendente por `fecha DESC` (de la más reciente a la más antigua), permitiendo al profesional ver de inmediato la última evolución médica registrada. | Consulta JPA: `findAllByHistoriaMedicaOrderByFechaDesc()`. |
| **RN-ANO-05** | **Estampa de Tiempo Inmutable en Registro** | Al crear una nueva anotación, la fecha y hora de la nota se captura automáticamente del servidor (`LocalDateTime.now()`), garantizando trazabilidad cronológica fidedigna. | Generación automática en `guardarAnotacionHistoria`. |
| **RN-ANO-06** | **Consulta Detallada y Modificación Controlada** | Cada anotación individual puede consultarse puntualmente por su identificador (`/anotacion-historia/consultar`) y actualizarse validando que la nota, la historia y el médico existan. | `BadRequestException`: *"La anotacion seleccionada no es valida"* (HTTP 400). |

---

### 💊 Módulo de Fórmulas Médicas (Sección 1 del Parcial)

| # | Regla de Negocio | Descripción y Criterio de Validación |
| :-: | :--- | :--- |
| **RN-FOR-01** | **Existencia de Referencias** | No se puede crear una fórmula médica para una mascota o veterinario inexistente. |
| **RN-FOR-02** | **Coherencia con la Consulta** | Una fórmula no puede asociarse a una consulta perteneciente a otra mascota distinta. |
| **RN-FOR-03** | **Fecha de Emisión** | La fecha de emisión de la prescripción no podrá ser anterior a la fecha de la consulta/cita asociada. |
| **RN-FOR-04** | **Posología Válida** | La dosis, frecuencia y duración del tratamiento deben contener valores válidos y no vacíos. |
| **RN-FOR-05** | **Inactivación Lógica** | Si una fórmula ya está asociada a información clínica, el sistema debe evitar su eliminación física en base de datos implementando un mecanismo de inactivación. |

---

## 🚀 2. Requisitos y Ejecución del Proyecto

### Prerrequisitos
* **Java:** OpenJDK 26 (`C:\Users\lcfur\.jdks\openjdk-26.0.2.1`).  
  *(Nota técnica: Se recomienda OpenJDK 26 ya que Lombok presenta incompatibilidad con el compilador interno de JDK 27).*
* **Base de Datos:** MySQL 8.0 en `localhost:3306`.
  - Base de datos: `clinica`
  - Usuario: `root`
  - Contraseña por defecto: `12345` (configurable en `src/main/resources/application-dev.properties`)

### Creación de la Base de Datos
El script DDL completo con tablas, llaves foráneas y datos semilla de prueba se encuentra en:
👉 [`schema.sql`](schema.sql)

Para ejecutarlo en MySQL:
```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < schema.sql
```

### Comando para Ejecutar el Backend
En la terminal de PowerShell:
```powershell
$env:JAVA_HOME = "C:\Users\lcfur\.jdks\openjdk-26.0.2.1"; .\mvnw.cmd spring-boot:run
```

El servidor iniciará en:
`http://localhost:8080/clinica/v1/`

---

## 📡 3. Catálogo de Endpoints REST Implementados

> Convención HTTP: `GET` para consultas, `POST` para crear, `PUT` para actualizar,
> `PATCH` para cambios parciales de estado y `DELETE` para eliminar. Las violaciones de
> reglas de negocio responden `400` con un mensaje comprensible; los errores no
> controlados responden `500` con un mensaje genérico (el detalle queda en el log).

### 📅 Citas Médicas (`/clinica/v1/cita`)
* `GET /cita/listar-por-fecha?fechaInicial={iso}&fechaFinal={iso}`: Listar citas (todas o por rango) ordenadas descendente.
* `GET /cita/consultar?citaId={id}`: Consultar el detalle de una cita específica.
* `GET /cita/listar-atendidas-por-mascota?mascotaId={id}`: Citas atendidas de una mascota (combo dependiente de historias y fórmulas).
* `POST /cita/guardar`: Registrar nueva cita (valida solapamiento 15 min, fechas pasadas, pertenencia mascota-cliente).
* `PUT /cita/actualizar`: Modificar cita existente (restringido según estado).
* `PATCH /cita/cancelar?citaId={id}`: Cancelar una cita de forma explícita.

### 👨‍⚕️ Médicos Veterinarios (`/clinica/v1/medico`)
* `GET /medico/listar-todos`: Obtiene el listado completo de veterinarios activos.

### 👤 Clientes (`/clinica/v1/cliente`)
* `GET /cliente/listar-todos`: Listar todos los clientes registrados.
* `GET /cliente/consultar?clienteId={id}`: Consultar un cliente.

### 🐶 Mascotas (`/clinica/v1/mascota`)
* `GET /mascota/listar-mascotas`: Listar todas las mascotas.
* `GET /mascota/buscar-by-cliente?clienteId={id}`: Listar únicamente las mascotas de un cliente (combo dependiente).
* `GET /mascota/consultar?mascotaId={id}`: Consultar una mascota.
* `POST /mascota/guardar`: Registrar mascota.
* `PUT /mascota/actualizar`: Actualizar mascota.

### 📑 Historias Clínicas (`/clinica/v1/historia-medica`)
* `GET /historia-medica/listar?fechaInicial={iso}&fechaFinal={iso}`: Listar historias médicas (todas o por rango de fechas ISO), ordenadas descendente.
* `GET /historia-medica/consultar?historiaId={id}`: Consultar el detalle de una historia clínica.
* `POST /historia-medica/guardar`: Crear historia clínica (cliente, mascota, cita atendida, diagnóstico y observaciones). Valida pertenencia mascota-cliente, cita atendida de la misma mascota y que la cita no tenga ya otra historia.
* `PUT /historia-medica/actualizar`: Modificar historia médica con las mismas reglas.
* `DELETE /historia-medica/eliminar?historiaId={id}`: Eliminar historia médica, solo si no tiene anotaciones.

### 📝 Anotaciones de Historia (`/clinica/v1/anotacion-historia`)
* `GET /anotacion-historia/listar?historiaId={id}`: Listar anotaciones de una historia médica, de la más reciente a la más antigua.
* `GET /anotacion-historia/consultar?anotacionId={id}`: Consultar detalle de una anotación.
* `POST /anotacion-historia/guardar`: Registrar anotación (veterinario, descripción, observación, diagnóstico/evolución; fecha y hora automática).
* `PUT /anotacion-historia/actualizar`: Actualizar una anotación existente.

### 💊 Fórmulas Médicas (`/clinica/v1/formula-medica`)
* `GET /formula-medica/listar`: Listar fórmulas médicas emitidas (más reciente primero).
* `GET /formula-medica/consultar?formulaId={id}`: Consultar el detalle completo de una fórmula.
* `POST /formula-medica/guardar`: Registrar fórmula (valida mascota existente con cliente, cita atendida de la misma mascota con veterinario, medicamento vigente, dosis/frecuencia/duración y fecha de emisión ≥ fecha de la consulta).
* `PUT /formula-medica/actualizar`: Actualizar fórmula con las mismas reglas. No existe eliminación física: la fórmula hace parte del historial clínico.
* `GET /medicamento/listar-todos`: Listar catálogo de medicamentos disponibles.

### 🗄️ Base de datos
* `schema.sql`: script completo (DDL + datos de prueba) para una instalación nueva.
* `migracion_parcial.sql`: agrega las columnas nuevas de `formula_medica`, `historia_medica` y `anotacion_historia` en una base creada con la versión anterior.

---

## 🏗️ 4. Arquitectura y Buenas Prácticas

El proyecto sigue una arquitectura en capas limpia con estricta separación de responsabilidades:
1. **`api/`**: Interfaces con los contratos REST (`@GetMapping`, `@PostMapping`, `@PutMapping`, `@PatchMapping`, `@DeleteMapping`, `@RequestMapping`, `@CrossOrigin`).
2. **`apicontroller/`**: Controladores `@RestController` que delegan directamente al servicio.
3. **`service/`**: Interfaces que declaran la lógica de negocio.
4. **`serviceimpl/`**: Implementaciones `@Service` con validaciones rigurosas y reglas de negocio.
5. **`repository/`**: Interfaces `JpaRepository` con consultas derivadas y optimizadas.
6. **`entity/`**: Entidades JPA con anotaciones de mapeo relacional y Lombok `@Data`.
7. **`models/`**: DTOs de entrada (`*Rq`) y respuestas homogéneas (`MiRespuestaRS`).
8. **`exception/`**: Manejador centralizado de errores (`GlobalExceptionHandler`) con respuestas estructuradas en formato JSON ante errores de validación (HTTP 400).
