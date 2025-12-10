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

El panel phpMyAdmin estará disponible en:
<http://localhost:8081/>
- Usuario: `root`
- Contraseña: `root`

### Endpoints disponibles

- `GET /api/books` - Listar todos los libros
- `GET /api/books/{isbn}` - Obtener un libro por ISBN
- `GET /api/books/search` - Buscar libros por criterios (título, autores, género, editorial)
  - **Parámetros opcionales (query params):**
    - `title` - Buscar por título (búsqueda parcial, no es sensible a mayúsculas y minúsculas)
    - `authors` - Buscar por autores (búsqueda parcial, no es sensible a mayúsculas y minúsculas)
    - `genre` - Buscar por género (búsqueda exacta, no es sensible a mayúsculas y minúsculas)
    - `publisher` - Buscar por editorial (búsqueda parcial, no es sensible a mayúsculas y minúsculas)
  - **Ejemplos:**
    - `/api/books/search?title=eternauta` - Libros cuyo título contenga "eternauta"
    - `/api/books/search?authors=poe&genre=terror` - Libros de "poe" del género "terror"
    - `/api/books/search` - Sin parámetros devuelve todos los libros
- `POST /api/books` - Crear un libro
- `PUT /api/books/{isbn}` - Actualizar un libro
- `DELETE /api/books/{isbn}` - Eliminar un libro

### Detener los servicios
```bash
# Detener la aplicación: Ctrl + C

# Detener Docker Compose manteniendo los datos
  docker-compose down

# Detener Docker Compose y eliminar volúmenes (limpieza completa de los datos)
  docker-compose down -v
```

### Formato de un libro (ejemplo)
```JSON
{
  "isbn": "9788466357562",
  "title": "1984",
  "authors": "George Orwell",
  "price": 8.99,
  "description": "La novela distópica más famosa sobre el totalitarismo y la vigilancia masiva.",
  "publisher": "Debolsillo",
  "genre": "Ficción",
  "imageUrl": "https://books.google.com/books/publisher/content?id=H8Y1EQAAQBAJ&printsec=frontcover&img=1&zoom=4&edge=curl&source=gbs_api"
}
```

### Sobre el proyecto
> [!IMPORTANT]
> Este proyecto está en su primera versión. 
> 
> Se pretende comenzar por lo más básico para luego poder iterar sobre ello, construyendo nuevas funcionalidades y mejorando las existentes.
