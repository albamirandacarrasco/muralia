# Muralia

Plataforma moderna de gestión y compartición de imágenes construida con React y Spring Boot.

## Descripción del Proyecto

Muralia es una aplicación web full-stack que permite a los usuarios registrarse, autenticarse y gestionar una galería de imágenes. El proyecto implementa autenticación segura con JWT, almacenamiento de imágenes, y una interfaz de usuario moderna y responsiva.

## Arquitectura

El proyecto está dividido en dos componentes principales:

### Frontend (`muralia-frontend/`)
- **Framework**: React 19 con Vite
- **Routing**: React Router DOM v6
- **HTTP Client**: Axios
- **Estilos**: Tailwind CSS
- **Estado**: Context API

### Backend (`muralia-backend/`)
- **Framework**: Spring Boot 3.2.1
- **Base de Datos**: PostgreSQL
- **Autenticación**: JWT (JSON Web Tokens)
- **Seguridad**: Spring Security
- **Migraciones**: Liquibase
- **API Documentation**: OpenAPI 3 / Swagger
- **Testing**: JUnit 5, Testcontainers

## Características Principales

- **Autenticación y Autorización**
  - Registro de usuarios con validación
  - Login/Logout con tokens JWT
  - Sesiones persistentes
  - Rutas protegidas

- **Gestión de Imágenes**
  - Subida de imágenes (JPEG, PNG, GIF, WebP)
  - Almacenamiento en servidor
  - Galería con paginación
  - Visualización de metadatos
  - Eliminación de imágenes propias

- **Interfaz de Usuario**
  - Diseño responsivo (móvil, tablet, desktop)
  - Navegación intuitiva
  - Vista previa antes de subir
  - Carga dinámica de contenido

## Stack Tecnológico Completo

### Frontend
```json
{
  "react": "19.2.0",
  "react-router-dom": "6.22.0",
  "axios": "1.6.0",
  "vite": "5.4.11",
  "tailwindcss": "3.4.19"
}
```

### Backend
```gradle
{
  "spring-boot": "3.2.1",
  "java": "17",
  "postgresql": "latest",
  "liquibase": "latest",
  "jwt": "0.12.3",
  "springdoc-openapi": "2.3.0"
}
```

## Estructura del Proyecto

```
muralia/
├── muralia-frontend/          # Aplicación React
│   ├── src/
│   │   ├── components/       # Componentes reutilizables
│   │   ├── context/          # Contextos de React
│   │   ├── pages/            # Páginas de la aplicación
│   │   ├── services/         # Servicios API
│   │   └── App.jsx           # Componente principal
│   ├── package.json
│   └── vite.config.js
│
├── muralia-backend/           # Aplicación Spring Boot
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/muralia/
│   │   │   │   ├── config/          # Configuración
│   │   │   │   ├── controller/      # Controladores REST
│   │   │   │   ├── entity/          # Entidades JPA
│   │   │   │   ├── repository/      # Repositorios
│   │   │   │   ├── service/         # Lógica de negocio
│   │   │   │   └── security/        # Configuración de seguridad
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── db/changelog/    # Migraciones Liquibase
│   │   └── test/
│   ├── build.gradle
│   └── openapi/              # Especificación API
│
├── Dockerfile.combined        # Build multi-stage
├── build-and-push-ecr.sh     # Script de deploy a AWS ECR
├── .env.production.example   # Variables de entorno de ejemplo
└── README.md                 # Este archivo
```

## Requisitos Previos

- **Java 17+** - Para el backend
- **Node.js 18+** y **npm** - Para el frontend
- **PostgreSQL 14+** - Base de datos
- **Docker** (opcional) - Para contenedores
- **Git** - Control de versiones

## Instalación y Configuración

### 1. Clonar el Repositorio

```bash
git clone <repository-url>
cd muralia
```

### 2. Configurar Base de Datos

Crear una base de datos PostgreSQL:

```sql
CREATE DATABASE muralia;
CREATE USER muralia_user WITH PASSWORD 'tu_password';
GRANT ALL PRIVILEGES ON DATABASE muralia TO muralia_user;
```

### 3. Configurar Backend

```bash
cd muralia-backend

# Crear archivo de configuración
cat > src/main/resources/application-local.properties << EOF
spring.datasource.url=jdbc:postgresql://localhost:5432/muralia
spring.datasource.username=muralia_user
spring.datasource.password=tu_password

jwt.secret=tu_clave_secreta_muy_larga_y_segura
jwt.expiration=86400000

image.upload.dir=./uploads
EOF

# Compilar y ejecutar
./gradlew bootRun --args='--spring.profiles.active=local'
```

El backend estará disponible en `http://localhost:8080`

### 4. Configurar Frontend

```bash
cd muralia-frontend

# Instalar dependencias
npm install

# Crear archivo de entorno
cat > .env.local << EOF
VITE_API_BASE_URL=http://localhost:8080
EOF

# Ejecutar en modo desarrollo
npm run dev
```

El frontend estará disponible en `http://localhost:5173`

## Uso en Desarrollo

### Backend
```bash
cd muralia-backend
./gradlew bootRun --args='--spring.profiles.active=local'
```

### Frontend
```bash
cd muralia-frontend
npm run dev
```

### Acceder a la Aplicación
1. Abrir `http://localhost:5173` en el navegador
2. Registrar un nuevo usuario
3. Iniciar sesión
4. Subir y gestionar imágenes

## Endpoints API

### Autenticación
```
POST   /api/auth/register    - Registrar nuevo usuario
POST   /api/auth/login       - Iniciar sesión
POST   /api/auth/logout      - Cerrar sesión
GET    /api/auth/me          - Obtener usuario actual
```

### Imágenes
```
POST   /api/images           - Subir imagen (multipart/form-data)
GET    /api/images           - Listar imágenes (con paginación)
GET    /api/images/{id}      - Obtener metadata de imagen
GET    /api/images/{id}/file - Descargar archivo de imagen
DELETE /api/images/{id}      - Eliminar imagen
```

### Documentación API
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

## Despliegue con Docker

### Build de Imagen Combinada

El proyecto incluye un Dockerfile multi-stage que empaqueta frontend y backend en una sola imagen:

```bash
# Build local
docker build -f Dockerfile.combined -t muralia:latest .

# Ejecutar
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/muralia \
  -e SPRING_DATASOURCE_USERNAME=muralia_user \
  -e SPRING_DATASOURCE_PASSWORD=tu_password \
  -e JWT_SECRET=tu_clave_secreta \
  muralia:latest
```

### Deploy a AWS ECR

```bash
# Build y push a ECR
./build-and-push-ecr.sh latest

# O con tag específico
./build-and-push-ecr.sh v1.0.0
```

El script:
1. Construye la imagen usando `Dockerfile.combined`
2. Se autentica con AWS ECR
3. Tagea la imagen
4. La sube al repositorio ECR

## Testing

### Backend - Tests Unitarios
```bash
cd muralia-backend
./gradlew test
```

### Backend - Tests de Integración
```bash
cd muralia-backend
./gradlew integrationTest
```

Los tests de integración usan Testcontainers para levantar PostgreSQL automáticamente.

### Frontend - Linting
```bash
cd muralia-frontend
npm run lint
```

## Variables de Entorno

### Backend (application.properties)
```properties
# Base de datos
spring.datasource.url=jdbc:postgresql://localhost:5432/muralia
spring.datasource.username=muralia_user
spring.datasource.password=tu_password

# JWT
jwt.secret=clave_secreta_minimo_256_bits
jwt.expiration=86400000

# Uploads
image.upload.dir=./uploads
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### Frontend (.env)
```bash
VITE_API_BASE_URL=http://localhost:8080
```

## Configuración de Producción

### Seguridad
- Cambiar `jwt.secret` a un valor seguro y aleatorio
- Usar HTTPS en producción
- Configurar CORS apropiadamente
- Usar variables de entorno para secretos
- Configurar límites de rate limiting

### Base de Datos
- Usar connection pool optimizado
- Configurar backups automáticos
- Monitorear performance de queries

### Almacenamiento de Imágenes
- Considerar usar S3 o similar para producción
- Implementar CDN para servir imágenes
- Configurar políticas de retención

## Monitoreo

El backend incluye Spring Boot Actuator:

```bash
# Health check
curl http://localhost:8080/actuator/health

# Métricas
curl http://localhost:8080/actuator/metrics

# Info
curl http://localhost:8080/actuator/info
```

## Solución de Problemas

### Backend no arranca
- Verificar que PostgreSQL esté corriendo
- Comprobar credenciales de base de datos
- Revisar logs: `./gradlew bootRun`

### Frontend no conecta con Backend
- Verificar `VITE_API_BASE_URL` en `.env`
- Comprobar que el backend esté corriendo
- Revisar configuración CORS en el backend

### Errores de subida de imágenes
- Verificar tamaño máximo de archivo
- Comprobar permisos del directorio `uploads/`
- Revisar formato de imagen soportado

## Roadmap

- [ ] Soporte para álbumes/colecciones
- [ ] Sistema de likes y comentarios
- [ ] Búsqueda y filtrado avanzado
- [ ] Integración con servicios de almacenamiento en la nube (S3)
- [ ] Procesamiento de imágenes (thumbnails, optimización)
- [ ] Compartición social
- [ ] API pública con rate limiting
- [ ] Panel de administración

## Contribuir

1. Fork el proyecto
2. Crear una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir un Pull Request

## Licencia

Proyecto privado - Todos los derechos reservados

## Contacto

Para preguntas o soporte, contactar al equipo de desarrollo.

---

**Muralia** - Comparte tus momentos visuales
