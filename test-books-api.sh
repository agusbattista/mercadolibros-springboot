#!/bin/bash

BASE_URL="http://localhost:8080/api/books"
COLOR='\033[38;5;62m'
RESET='\033[0m'
ALICIA_ISBN="9877184931"
ALICIA_JSON='{
  "isbn": "9877184931",
  "title": "Alicia en el país de las maravillas",
  "authors": "Lewis Carroll",
  "price": 4.99,
  "description": "La historia de Alicia y su increíble viaje a un mundo fantástico lleno de personajes extraños y aventuras inolvidables.",
  "publisher": "Ediciones LEA",
  "genre": "Fantasía",
  "imageUrl": "https://books.google.com/books/publisher/content?id=oDzZDgAAQBAJ&printsec=frontcover&img=1&zoom=4&edge=curl&imgtk=AFLRE73FY6i2kA8yM02QLs0Ym79IU6ei_8Kp-0xqqfJ2NnY85PAXixaVMV1oKzrwQ7OKTWl2ZxuE6npi6S-6E3cGkbOQfr0eID63-EMrdQVT42AKshoDdJQXCTH_xGRpCTMN29rECIla&source=gbs_api"
}'
ALICIA_JSON_MODIFIED='{
  "title": "Alicia en el país de las maravillas MODIFICADO",
  "authors": "Lewis Carroll",
  "price": 4.99,
  "description": "La historia de Alicia y su increíble viaje a un mundo fantástico lleno de personajes extraños y aventuras inolvidables.",
  "publisher": "Ediciones LEA",
  "genre": "Fantasía",
  "imageUrl": "https://books.google.com/books/publisher/content?id=oDzZDgAAQBAJ&printsec=frontcover&img=1&zoom=4&edge=curl&imgtk=AFLRE73FY6i2kA8yM02QLs0Ym79IU6ei_8Kp-0xqqfJ2NnY85PAXixaVMV1oKzrwQ7OKTWl2ZxuE6npi6S-6E3cGkbOQfr0eID63-EMrdQVT42AKshoDdJQXCTH_xGRpCTMN29rECIla&source=gbs_api"
}'
BAD_JSON='{
  "title": "Alicia en el país de las maravillas",
  "authors": "Lewis Carroll",
  "price": -4.999,
  "description": "La historia de Alicia y su increíble viaje a un mundo fantástico lleno de personajes extraños y aventuras inolvidables.",
  "publisher": "Ediciones LEA",
  "genre": "Fantasía",
  "imageUrl": "https://books.google.com/books/publisher/content?id=oDzZDgAAQBAJ&printsec=frontcover&img=1&zoom=4&edge=curl&imgtk=AFLRE73FY6i2kA8yM02QLs0Ym79IU6ei_8Kp-0xqqfJ2NnY85PAXixaVMV1oKzrwQ7OKTWl2ZxuE6npi6S-6E3cGkbOQfr0eID63-EMrdQVT42AKshoDdJQXCTH_xGRpCTMN29rECIla&source=gbs_api"
}'

print_test() {
  printf "${COLOR}==>$RESET %s\n" "$1"
}

separator() {
  echo ""
}

# CLEAN DB
curl -s -o /dev/null -X DELETE "$BASE_URL/$ALICIA_ISBN"

# TESTS
separator

print_test "Petición GET a /api/books (paginación por defecto)"
curl -s "$BASE_URL" | jq

separator

print_test "Petición GET a /api/books con paginación (página 1)"
curl -s "$BASE_URL?page=1" | jq

separator

print_test "Petición GET a /api/books con paginación (página 0, tamaño 2)"
curl -s "$BASE_URL?page=0&size=2" | jq

separator

print_test "Petición GET a /api/books con ordenamiento (por título ascendente)"
curl -s "$BASE_URL?sort=title,asc" | jq

separator

print_test "Petición GET a /api/books con ordenamiento (por precio descendente)"
curl -s "$BASE_URL?sort=price,desc" | jq

separator

print_test "Petición GET a /api/books con ordenamiento múltiple (por género ascendente, luego precio descendente)"
curl -s "$BASE_URL?sort=genre,asc&sort=price,desc" | jq

separator

print_test "Petición GET a /api/books con paginación y ordenamiento (página 0, tamaño 3, por precio ascendente)"
curl -s "$BASE_URL?page=0&size=3&sort=price,asc" | jq

separator

print_test "Petición POST exitosa a /api/books"
curl -s -X POST "$BASE_URL" -H 'Content-type:application/json' -d "$ALICIA_JSON" | jq

separator

print_test "Petición POST fallida a /api/books (ISBN duplicado)"
curl -s -X POST "$BASE_URL" -H 'Content-type:application/json' -d "$ALICIA_JSON" | jq

separator

print_test "Petición POST fallida a /api/books (JSON inválido)"
curl -s -X POST "$BASE_URL" -H 'Content-type:application/json' -d "$BAD_JSON" | jq

separator

print_test "Petición GET ISBN existente a /api/books/$ALICIA_ISBN (ISBN de Alicia en el país de las maravillas)"
curl -s "$BASE_URL/$ALICIA_ISBN" | jq

separator

print_test "Petición GET ISBN inexistente a /api/books/0000000000000"
curl -s "$BASE_URL/0000000000000" | jq

separator

print_test "Petición PUT exitosa a /api/books/$ALICIA_ISBN (ISBN de Alicia en el país de las maravillas)"
curl -s -X PUT "$BASE_URL/$ALICIA_ISBN" -H 'Content-type:application/json' -d "$ALICIA_JSON_MODIFIED" | jq

separator

print_test "Petición PUT fallida a /api/books/$ALICIA_ISBN (JSON inválido)"
curl -s -X PUT "$BASE_URL/$ALICIA_ISBN" -H 'Content-type:application/json' -d "$BAD_JSON" | jq

separator

print_test "Petición GET a /api/books/search con género 'Fantasía' y editorial 'Janés'"
curl -s -G "$BASE_URL/search" --data-urlencode "genre=Fantasía" --data-urlencode "publisher=Janés" | jq

separator

print_test "Petición GET a /api/books/search con género 'Fantasía', paginación y ordenamiento (página 0, tamaño 2, por precio descendente)"
curl -s -G "$BASE_URL/search" --data-urlencode "genre=Fantasía" --data-urlencode "page=0" --data-urlencode "size=2" --data-urlencode "sort=price,desc" | jq

separator

print_test "Petición GET a /api/books/search con título parcial (Alicia)"
curl -s -G "$BASE_URL/search" --data-urlencode "title=Alicia" | jq

separator

print_test "Petición GET a /api/books/search con autor parcial (Carroll)"
curl -s -G "$BASE_URL/search" --data-urlencode "authors=Carroll" | jq

separator

print_test "Petición DELETE a /api/books/$ALICIA_ISBN (ISBN de Alicia en el país de las maravillas)"
curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" -X DELETE "$BASE_URL/$ALICIA_ISBN"

separator

print_test "Petición DELETE a /api/books/$ALICIA_ISBN (ISBN de libro eliminado)"
curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" -X DELETE "$BASE_URL/$ALICIA_ISBN"
