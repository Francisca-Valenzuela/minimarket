# 🛒 MiniMarket Plus — Backend con Spring Security + JWT + OpenAPI + HATEOAS

Proyecto backend de la cadena de minimarkets **MiniMarket Plus**, implementado con Spring Boot.  
Incluye autenticación JWT, autorización basada en roles, documentación interactiva con OpenAPI/Swagger, navegación hipermedia con HATEOAS, protección contra amenazas comunes y un suite completo de pruebas unitarias con JaCoCo.

---

## 📋 Descripción

Sistema de gestión para minimarket que cubre inventario, productos, ventas, carritos y usuarios.  
La seguridad está implementada con **Spring Security + JWT (JJWT 0.12.3)**, arquitectura **stateless** y control de acceso por roles mediante `@PreAuthorize`.  
Todos los endpoints están documentados con **springdoc-openapi** (esquemas, ejemplos, códigos de error) y las respuestas incluyen enlaces **HATEOAS** (`_links`) para que un cliente pueda navegar entre recursos relacionados sin conocer de antemano la estructura de URLs.  
La calidad del código se valida con **166 pruebas unitarias e integración** distribuidas en 24 clases de test, con cobertura medida con JaCoCo (ver tabla de cobertura más abajo).

---

## ⚙️ Tecnologías

| Tecnología | Versión |
|---|---|
| Java | 17 |
| Spring Boot | 3.4.1 |
| Spring Security | (incluido en Boot) |
| Spring HATEOAS | (spring-boot-starter-hateoas) |
| JJWT | 0.12.3 |
| H2 Database | (en memoria) |
| Lombok | (incluido en Boot) |
| Maven | 3.x |
| JUnit 5 | (incluido en spring-boot-starter-test) |
| Mockito | (incluido en spring-boot-starter-test) |
| Spring Security Test | (spring-security-test) |
| JaCoCo | 0.8.11 |
| springdoc-openapi | 2.8.9 |

---

## 🚀 Cómo levantar el proyecto

### Prerequisitos
- Java 17 instalado (`java -version`)
- Maven instalado (`mvn -version`) — o usar el wrapper `./mvnw` incluido

### 1. Clonar el repositorio

```bash
git clone https://github.com/Francisca-Valenzuela/minimarket.git
cd minimarket
```

### 2. Variables de entorno (opcional)

El proyecto funciona sin configuración adicional usando valores por defecto para desarrollo local.  
Para producción, define estas variables de entorno:

```bash
export JWT_SECRET=TuClaveSecretaMuyLargaYSegura2024  # mínimo 32 caracteres
export JWT_EXPIRATION=14400000                        # milisegundos (4 horas por defecto)
export CORS_ORIGINS=http://localhost:3000             # orígenes permitidos
```

### 3. Compilar y ejecutar

```bash
./mvnw spring-boot:run
```

O con Maven instalado:

```bash
mvn spring-boot:run
```

La aplicación levanta en `http://localhost:8080`.

### 📖 Documentación de la API (Swagger / OpenAPI)

Una vez levantado el proyecto, la documentación interactiva está disponible en:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **JSON OpenAPI (para importar en Postman u otras herramientas):** http://localhost:8080/v3/api-docs

Para probar los endpoints protegidos desde Swagger UI:
1. Autentícate en `POST /api/auth/login` con un usuario válido.
2. Copia el `token` de la respuesta.
3. Haz clic en el botón **Authorize** (🔒) en la parte superior de Swagger UI e ingresa el token con el formato `Bearer <tu_token>`.
4. Ya puedes ejecutar cualquier endpoint protegido directamente desde la interfaz.

### 🔗 Navegación HATEOAS

Las respuestas de los endpoints de colección y de recurso individual incluyen un bloque `_links` con las rutas relacionadas, siguiendo el formato HAL (`application/hal+json`). Por ejemplo, al consultar un carrito:

```json
{
  "id": 3,
  "cantidad": 2,
  "_links": {
    "self": { "href": "http://localhost:8080/api/carrito/3" },
    "producto": { "href": "http://localhost:8080/api/productos/7" },
    "carritos": { "href": "http://localhost:8080/api/carrito" }
  }
}
```

Esto permite a un cliente descubrir y navegar los recursos relacionados (por ejemplo, el producto asociado a un ítem del carrito) siguiendo los enlaces en vez de construir URLs manualmente. Los enlaces se generan con `linkTo(methodOn(...))` en clases `*ModelAssembler` dedicadas, por lo que si cambia la ruta de un endpoint, el enlace se actualiza automáticamente o falla en tiempo de compilación.

> ⚠️ El archivo `openapi.json` de la raíz del proyecto **no se regenera solo**. Si modificas anotaciones OpenAPI (`@Schema`, `@ApiResponses`, etc.), vuelve a exportarlo desde `http://localhost:8080/v3/api-docs` con la app corriendo antes de subir cambios, para que no quede desactualizado respecto al código.

### 4. Ejecutar pruebas y generar reporte de cobertura

```bash
mvn test
```

El reporte HTML de JaCoCo se genera en:

```
target/site/jacoco/index.html
```

### 5. Consola H2 (base de datos en memoria)

Disponible en `http://localhost:8080/h2-console`

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:mem:testdb` |
| Usuario | `sa` |
| Contraseña | *(vacío)* |

---

## 👥 Usuarios de prueba

El `DataInitializer` crea automáticamente estos usuarios al iniciar:

| Usuario | Contraseña | Rol | Acceso |
|---|---|---|---|
| `gerente` | `gerente123` | `ROLE_GERENTE` | Total (inventario, usuarios, todas las operaciones) |
| `empleado` | `empleado123` | `ROLE_EMPLEADO` | Productos, categorías, ventas, detalle ventas, carrito |
| `cliente` | `cliente123` | `ROLE_CLIENTE` | Solo carrito |

---

## 🔐 Endpoints de autenticación

### Login

```
POST /api/auth/login
Content-Type: application/json

{
  "username": "gerente",
  "password": "gerente123"
}
```

**Respuesta exitosa (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "username": "gerente",
  "roles": "[ROLE_GERENTE]"
}
```

### Registro

```
POST /api/auth/registro
Content-Type: application/json

{
  "username": "nuevo_cliente",
  "password": "mi_password",
  "nombre": "Juan",
  "apellido": "Pérez",
  "email": "juan@email.com",
  "direccion": "Av. Siempre Viva 742"
}
```

Asigna `ROLE_CLIENTE` por defecto.

### Crear usuario como GERENTE

A diferencia de `/api/auth/registro` (público, siempre asigna `ROLE_CLIENTE`), un usuario con `ROLE_GERENTE` puede crear cuentas con cualquier rol desde `POST /api/usuarios`, incluyendo el campo opcional `roles`:

```
POST /api/usuarios
Authorization: Bearer <token-de-gerente>
Content-Type: application/json

{
  "username": "nuevo_empleado",
  "password": "empleado123",
  "nombre": "Ana",
  "apellido": "Soto",
  "email": "ana.soto@minimarket.cl",
  "direccion": "Av. Siempre Viva 742",
  "roles": ["ROLE_EMPLEADO"]
}
```

Si se omite `roles`, se asigna `ROLE_CLIENTE` por defecto, igual que en el registro público. La respuesta usa `UsuarioResponseDTO` (sin exponer el password) con sus enlaces HATEOAS correspondientes.

---

## 🗂️ Endpoints por rol

Incluir en todas las requests protegidas:  
`Authorization: Bearer <token>`

| Endpoint | Método | Roles permitidos |
|---|---|---|
| `/api/auth/**` | POST | Público |
| `/api/productos/**` | GET, POST | EMPLEADO, GERENTE |
| `/api/categorias/**` | GET, POST, PUT, DELETE | EMPLEADO, GERENTE |
| `/api/inventario/**` | GET | EMPLEADO, GERENTE |
| `/api/inventario/**` | POST, PUT, DELETE | GERENTE |
| `/api/ventas/**` | GET, POST | EMPLEADO, GERENTE |
| `/api/detalle-ventas/**` | GET, POST, PUT, DELETE | EMPLEADO, GERENTE |
| `/api/carrito/**` | GET, POST, PUT, DELETE | CLIENTE, EMPLEADO, GERENTE |
| `/api/usuarios/**` | GET, POST, PUT, DELETE | GERENTE |

---

## 🧪 Pruebas unitarias

El proyecto incluye 166 pruebas distribuidas en 24 clases de test, todas en verde (`mvn test` → `Tests run: 166, Failures: 0, Errors: 0`).

> **Nota de mejora aplicada:** durante la revisión final se detectó que 4 pruebas (`InventarioControllerSecurityTest`, `InventarioControllerTest`, `VentaControllerSecurityTest`) fallaban porque enviaban payloads incompletos frente a validaciones `@NotNull`/`@NotEmpty` agregadas posteriormente en las entidades `Inventario` y `Venta`. Se corrigieron los payloads de prueba (y se ajustó una expectativa que ya no correspondía) para que la suite completa pase sin fallos.

### Pruebas de servicio (Mockito + JUnit 5)

| Clase | Descripción |
|---|---|
| `VentaServiceTest` | 27 pruebas, incluyendo casos límite de stock con `@CsvSource` |
| `InventarioServiceTest` | 26 pruebas de movimientos de entrada/salida |
| `CarritoServiceTest` | Validación de disponibilidad de stock en carrito |
| `UsuarioServiceTest` | Registro, autenticación y roles |
| `ProductoServiceTest` | CRUD de productos y validaciones |
| `CategoriaServiceTest` | Gestión de categorías |
| `DetalleVentaServiceTest` | Detalle de productos por venta |
| `RolServiceTest` | Gestión de roles |
| `VentaServiceTransaccionalidadIT` | Prueba de integración con H2 real para verificar rollback transaccional |

### Pruebas de controlador (MockMvc)

| Clase | Descripción |
|---|---|
| `ProductoControllerTest` | Endpoints CRUD con contexto aislado |
| `InventarioControllerTest` | Endpoints de inventario |
| `VentaControllerTest` | Endpoints de ventas |
| `CarritoControllerTest` | Endpoints de carrito |
| `CategoriaControllerTest` | Endpoints de categorías |
| `DetalleVentaControllerTest` | Endpoints de detalle venta |
| `UsuarioControllerTest` | Endpoints de usuarios |

### Pruebas de seguridad (`@SpringBootTest` + contexto real)

| Clase | Casos cubiertos |
|---|---|
| `ProductoControllerSecurityTest` | EMPLEADO lista ✔, GERENTE crea ✔, CLIENTE bloqueado 403 ✔, sin auth 401 ✔ |
| `InventarioControllerSecurityTest` | GERENTE registra movimiento ✔, EMPLEADO bloqueado 403 ✔ |
| `VentaControllerSecurityTest` | EMPLEADO genera venta ✔, CLIENTE bloqueado 403 ✔ |
| `AuthControllerTest` | Login exitoso ✔, credenciales inválidas 401 ✔, registro ✔, usuario existente ✔ |
| `JwtUtilTest` | Generación de token ✔, extracción de username ✔, validación ✔, token de otro usuario ✔ |
| `CustomUserDetailsServiceTest` | Carga de usuario por username ✔ |

### Cobertura JaCoCo

| Paquete | Cobertura |
|---|---|
| `service.impl` | Alta (capa crítica de negocio) |
| `controller` | Alta (incluyendo pruebas de seguridad) |
| `security.util / filter` | Media (algunas ramas de error no forzadas) |
| **Global** | **_(completar con el % exacto de `target/site/jacoco/index.html` tras ejecutar `mvn clean test jacoco:report`)_** |

---

## 🏗️ Estructura del proyecto

```
src/
├── main/java/com/minimarket/
│   ├── config/
│   │   └── DataInitializer.java          # Usuarios de prueba iniciales
│   ├── controller/                        # Controladores REST por entidad
│   ├── dto/                               # Data Transfer Objects
│   ├── entity/                            # Entidades JPA
│   ├── exception/
│   │   └── GlobalExceptionHandler.java   # Manejo centralizado de errores
│   ├── repository/                        # Repositorios Spring Data JPA
│   ├── security/
│   │   ├── config/SecurityConfig.java    # Spring Security + CORS parametrizado
│   │   ├── config/OpenApiConfig.java     # Configuración de OpenAPI / Swagger UI
│   │   ├── controller/AuthController.java # Login y registro
│   │   ├── filter/JwtAuthFilter.java      # Filtro de validación JWT
│   │   ├── model/                         # CustomUserDetails, LoginRequest
│   │   ├── service/CustomUserDetailsService.java
│   │   └── util/JwtUtil.java             # Generación y validación de tokens
│   └── service/                           # Lógica de negocio (interfaces + impl)
└── test/java/com/minimarket/
    ├── *ServiceTest.java                  # Pruebas unitarias de servicios
    ├── VentaServiceTransaccionalidadIT.java # Prueba de integración
    ├── controller/
    │   ├── *ControllerTest.java           # Pruebas de controladores
    │   ├── ProductoControllerSecurityTest.java
    │   ├── InventarioControllerSecurityTest.java
    │   └── VentaControllerSecurityTest.java
    └── security/
        ├── controller/AuthControllerTest.java
        ├── service/CustomUserDetailsServiceTest.java
        └── util/JwtUtilTest.java
```

---

## 🛡️ Seguridad implementada

| Amenaza | Mecanismo de protección |
|---|---|
| Acceso no autorizado | JWT + Spring Security + `@PreAuthorize` |
| SQL Injection | Spring Data JPA con Prepared Statements (Hibernate) |
| XSS | Arquitectura stateless sin cookies de sesión; CORS configurado |
| CSRF | `csrf.disable()` justificado por arquitectura stateless + JWT en header |
| Exposición de errores | `GlobalExceptionHandler` con mensajes genéricos al cliente |
| Sesiones persistentes | `SessionCreationPolicy.STATELESS` + expiración configurable del token |
| CORS no controlado | Orígenes permitidos parametrizados vía `app.cors.allowed-origins` |

---

## 📚 Asignatura

**PBY2202 – Desarrollo Backend II**  
Duoc UC — Analista Programador Computacional  
Semana 9 — Evaluación Final Transversal (EFT): microservicios, seguridad, pruebas unitarias, OpenAPI (springdoc-openapi + Swagger UI) y HATEOAS