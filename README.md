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

### 📋 Módulo de Historias Clínicas y Anotaciones (Sección 3 del Parcial)

| # | Regla de Negocio | Descripción y Criterio de Validación |
| :-: | :--- | :--- |
| **RN-HIS-01** | **Asociación de Historia** | La historia clínica debe asociarse a una mascota existente y validar que pertenezca al cliente. |
| **RN-HIS-02** | **Vínculo con Cita Atendida** | La historia clínica o consulta debe derivarse de una cita previamente marcada como `Atendida`. |
| **RN-HIS-03** | **Múltiples Anotaciones** | Una historia clínica puede contener múltiples anotaciones a lo largo del tiempo que reflejen la evolución clínica. |
| **RN-HIS-04** | **Orden Cronológico Descendente** | El histórico de anotaciones debe listarse y presentarse siempre desde **la más reciente hasta la más antigua**. |
| **RN-HIS-05** | **Datos Mínimos de Anotación** | Toda anotación exige fecha/hora, veterinario responsable, descripción clínica, observación y diagnóstico o evolución. |

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

### 📅 Citas Médicas (`/clinica/v1/cita`)
* `GET /cita/listar-por-fecha?fechaInicial={iso}&fechaFinal={iso}`: Listar citas por rango de fecha ordenadas descendente.
* `GET /cita/consultar?citaId={id}`: Consultar el detalle de una cita específica.
* `POST /cita/guardar`: Registrar nueva cita (valida solapamiento 15 min, fechas pasadas, pertenencia mascota-cliente).
* `POST /cita/actualizar`: Modificar cita existente (restringido según estado).

### 👨‍⚕️ Médicos Veterinarios (`/clinica/v1/medico`)
* `GET /medico/listar-todos`: Obtiene el listado completo de veterinarios activos.

### 👤 Clientes (`/clinica/v1/cliente`)
* `GET /cliente/listar-todos`: Listar todos los clientes registrados.

### 🐶 Mascotas (`/clinica/v1/mascota`)
* `GET /mascota/listar-mascotas`: Listar todas las mascotas.
* `GET /mascota/buscar-by-cliente?clienteId={id}`: Listar únicamente las mascotas de un cliente (combo dependiente).
* `POST /mascota/guardar`: Registrar mascota.
* `POST /mascota/actualizar`: Actualizar mascota.

### 📑 Historias Clínicas (`/clinica/v1/historia-medica`)
* `GET /historia-medica/listar`: Listar historias médicas.
* `POST /historia-medica/guardar`: Crear nueva historia clínica asociada a cita atendida.
* `POST /historia-medica/actualizar`: Modificar historia médica.
* `POST /historia-medica/eliminar?historiaId={id}`: Eliminar historia médica.

### 📝 Anotaciones de Historia (`/clinica/v1/anotacion-historia`)
* `GET /anotacion-historia/listar?historiaId={id}`: Listar anotaciones de una historia médica (reciente a antigua).
* `POST /anotacion-historia/guardar`: Registrar anotación de evolución clínica.
* `POST /anotacion-historia/actualizar`: Actualizar anotación.

### 💊 Fórmulas Médicas (`/clinica/v1/formula-medica`)
* `GET /formula-medica/listar`: Listar fórmulas médicas emitidas.
* `GET /medicamento/listar-todos`: Listar catálogo de medicamentos disponibles.

---

## 🏗️ 4. Arquitectura y Buenas Prácticas

El proyecto sigue una arquitectura en capas limpia con estricta separación de responsabilidades:
1. **`api/`**: Interfaces con los contratos REST (`@GetMapping`, `@PostMapping`, `@RequestMapping`, `@CrossOrigin`).
2. **`apicontroller/`**: Controladores `@RestController` que delegan directamente al servicio.
3. **`service/`**: Interfaces que declaran la lógica de negocio.
4. **`serviceimpl/`**: Implementaciones `@Service` con validaciones rigurosas y reglas de negocio.
5. **`repository/`**: Interfaces `JpaRepository` con consultas derivadas y optimizadas.
6. **`entity/`**: Entidades JPA con anotaciones de mapeo relacional y Lombok `@Data`.
7. **`models/`**: DTOs de entrada (`*Rq`) y respuestas homogéneas (`MiRespuestaRS`).
8. **`exception/`**: Manejador centralizado de errores (`GlobalExceptionHandler`) con respuestas estructuradas en formato JSON ante errores de validación (HTTP 400).
