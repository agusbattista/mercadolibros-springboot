## MercadoLibros REST API

Este repositorio contiene una API REST hecha con Spring Boot que provee un CRUD de libros.

### Programas necesarios para ejecutarlo

1) JDK de Java 21
2) Docker

### Para ejecutarlo localmente

1) Clonar el repositorio

```bash
  git clone git@github.com:agusbattista/mercadolibros-springboot.git
```

2) Entrar a la carpeta generada

```bash
  cd mercadolibros-springboot
```

3) Levantar la base de datos con Docker

> [!IMPORTANT]
> Docker Desktop debe estar corriendo antes de ejecutar el siguiente comando

```bash
  docker-compose up -d
```

4) Ejecutar la aplicación
```bash
  ./mvnw spring-boot:run
```

### Corroborar que funciona

La API estará disponible en:
<http://localhost:8080/api/books>

phpMyAdmin estará disponible en:
<http://localhost:8081/>
- Usuario: `root`
- Contraseña: `root`

### Endpoints disponibles

- `GET /api/books` - Listar todos los libros
- `GET /api/books/{id}` - Obtener un libro por ID
- `POST /api/books` - Crear un libro
- `PUT /api/books/{id}` - Actualizar un libro
- `DELETE /api/books/{id}` - Eliminar un libro

### Detener los servicios
```bash
# Detener la aplicación: Ctrl + C

# Detener Docker Compose manteniendo los datos
  docker-compose down

# Detener Docker Compose y eliminar volúmenes (limpieza completa de los datos)
  docker-compose down -v
```
