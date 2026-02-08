## MercadoLibros REST API

Este repositorio contiene una API REST hecha con Spring Boot que provee un CRUD de libros.

<img src="browser-screenshot.jpg" width="65%">

### Programas necesarios para ejecutarlo

1. JDK de Java 21
2. Docker

### Para ejecutarlo localmente

1. Clonar el repositorio

```bash
git clone git@github.com:agusbattista/mercadolibros-springboot.git
```

2. Entrar a la carpeta generada

```bash
cd mercadolibros-springboot
```

3. Levantar la base de datos con Docker

> [!IMPORTANT]
> Docker Desktop debe estar corriendo antes de ejecutar el siguiente comando

```bash
docker compose up -d
```

4. Ejecutar la aplicación

```bash
./mvnw spring-boot:run
```

### Corroborar que funciona

La API estará disponible en:
<http://localhost:8080/api/books>

El panel phpMyAdmin estará disponible en:
<http://localhost:8081>

- Usuario: `root`
- Contraseña: `root`

### Endpoints disponibles

- `GET /api/books` - Listar todos los libros
- `GET /api/books/{uuid}` - Obtener un libro por su UUID
- `GET /api/books/isbn/{isbn}` - Obtener un libro por ISBN
- `GET /api/books/search` - Buscar libros por criterios (título, autores, género, editorial)
  - **Parámetros opcionales (query params):**
    - `title` - Buscar por título (búsqueda parcial, no es sensible a mayúsculas y minúsculas)
    - `authors` - Buscar por autores (búsqueda parcial, no es sensible a mayúsculas y minúsculas)
    - `genre` - Buscar por género (búsqueda exacta, no es sensible a mayúsculas y minúsculas)
    - `publisher` - Buscar por editorial (búsqueda parcial, no es sensible a mayúsculas y minúsculas)
  - **Ejemplos:**
    - `/api/books/search?title=Design%20Patterns` - Libros cuyo título contenga "design patterns"
    - `/api/books/search?genre=FANTASÍA&authors=Patrick` - Libros de "patrick" del género "fantasía"
    - `/api/books/search` - Sin parámetros devuelve todos los libros
- `POST /api/books` - Crear un libro
- `PUT /api/books/{uuid}` - Actualizar un libro
- `DELETE /api/books/{uuid}` - Eliminar un libro (borrado lógico / soft delete)

> [!NOTE] 
> La API utiliza UUID como identificador para las operaciones de modificación y búsqueda específica.
>
> Al crear un libro, el sistema le asigna un UUID (que se incluye en la respuesta) para futuras consultas o modificaciones.
>
> Si intenta crear un libro con un ISBN que existe pero fue eliminado lógicamente, el sistema lo restaurará y actualizará sus datos.

### Paginación y ordenamiento

Todos los endpoints que devuelven listas de libros (`GET /api/books` y `GET /api/books/search`) soportan paginación y ordenamiento mediante query params.

#### Parámetros de paginación

- `page` - Número de página (comenzando desde 0). Por defecto: 0
- `size` - Cantidad de elementos por página. Por defecto: 5

#### Parámetros de ordenamiento

- `sort` - Campo y dirección de ordenamiento en formato `campo,direccion`. Por defecto: sin ordenación
  - Dirección puede ser `asc` (ascendente) o `desc` (descendente)
  - Se pueden aplicar múltiples ordenamientos agregando múltiples parámetros `sort`
  - Se puede utilizar cualquiera de los campos de un libro para el ordenamiento. Por defecto es ascendente

#### Ejemplos de uso

#### Paginación básica:

- `/api/books?page=0&size=2` - Primera página con 2 elementos
- `/api/books?page=1&size=5` - Segunda página con 5 elementos

#### Ordenamiento simple:

- `/api/books?sort=title,asc` - Ordenar por título ascendente
- `/api/books?sort=price,desc` - Ordenar por precio descendente

#### Ordenamiento múltiple:

- `/api/books?sort=genre,asc&sort=price,desc` - Ordenar por género ascendente, luego por precio descendente

#### Combinando paginación y ordenamiento:

- `/api/books?page=0&size=3&sort=price,asc` - Primera página con 3 elementos, ordenados por precio ascendente
- `/api/books/search?genre=Fantasía&page=0&size=2&sort=price,desc` - Búsqueda de género "Fantasía", primera página con 2 elementos, ordenados por precio descendente

### Formato de un libro (ejemplo)

La API devuelve el libro incluyendo su identificador único (uuid).

```JSON
{
  "uuid": "15226072-bbe0-402c-8691-2da79692be1b",
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

### Datos de prueba

El proyecto contiene un **CommandLineRunner** que carga diez libros con datos de prueba. Esto ocurre la primera vez que se ejecuta la aplicación o que se corren los test.

Si la base de datos ya contiene elementos (aunque estén eliminados lógicamente), no se agregarán libros.

> [!WARNING]
> Evite editar o borrar el archivo "src/main/resources/data/books.json" si no está seguro de cómo hacerlo o qué clases debe ajustar.
>
> Si desea evitar que se carguen datos de prueba, elimine los siguientes archivos para no generar ningún conflicto:

```text
src/main/java/io/github/agusbattista/mercadolibros_springboot/config/BookDataLoader.java
```

```text
src/test/java/io/github/agusbattista/mercadolibros_springboot/config/BookDataLoaderTest.java
```

```text
src/main/resources/data/books.json
```

### Test

El proyecto contiene tests para las distintas capas de la aplicación. Se pueden ejecutar con el siguiente comando:

```bash
./mvnw test
```

### Colección Postman

El archivo **"mercadolibros-springboot.postman_collection.json"** provee una colección para Postman que puede ser importada y utilizada. La ventaja de este enfoque es que ya tiene los endpoints disponibles y ejemplos de que se espera para cada uno.

### Detener los servicios

```bash
# Detener la aplicación: Ctrl + C

# Detener Docker Compose manteniendo los datos
docker compose down

# Detener Docker Compose y eliminar volúmenes (limpieza completa de los datos)
docker compose down -v
```

### Sobre el proyecto

> [!IMPORTANT]
> Este proyecto está en su primera versión.
>
> Se pretende comenzar por lo más básico para luego poder iterar sobre ello, construyendo nuevas funcionalidades y mejorando las existentes.
