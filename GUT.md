# Guía de Utilidades Técnicas - Scala y Paradigmas Funcionales

Este documento es una referencia rápida de conceptos y herramientas que usarás en los ejercicios.

---

## 🚀 PLANTILLAS DE PARCIAL (COPIAR, PEGAR Y ADAPTAR)

> Para el parcial efectivamente tomado en 2026 (`count`, `before`, `author` y palabras censuradas), ir primero a [PARCIAL_EVALUADO_2026.md](./PARCIAL_EVALUADO_2026.md). Incluye una plantilla para un único `Main.scala`, casos de borde y código cubierto por tests.

Usa estas plantillas cambiando solo lo que está en `MAYUSCULAS_SNAKE_CASE`. La idea es que durante el parcial reconozcas el patrón, copies el bloque y ajustes nombres de campos, rutas JSON y posiciones de tuplas.

### Patrón 1: Lectura de JSON Seguro con Fallbacks (`@JSON_PARSE`, `@TOLERANCIA_FALLOS`)

**Cuándo usarlo:** cuando una API o archivo JSON devuelve datos incompletos, o cuando un campo puede venir en más de un tipo, por ejemplo `minScore` como número o como texto.

```scala
import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.util.Try

implicit val formats: org.json4s.DefaultFormats.type = org.json4s.DefaultFormats

def intSeguro(item: JValue, campo: String, default: Int): Int = {
  (item \ campo).extractOpt[Int]
    .orElse((item \ campo).extractOpt[String].flatMap(raw => Try(raw.trim.toInt).toOption))
    .getOrElse(default)
}

def parsearEntidadSegura(jsonString: String): Option[List[TUPLE_TYPE]] = {
  try {
    val json = parse(jsonString)
    val elementos = (json \ "RUTA" \ "A" \ "ARRAY").children

    Some(elementos.flatMap { item =>
      val campoString = (item \ "CAMPO_TEXTO").extractOpt[String].getOrElse("DEFAULT")
      val campoInt = intSeguro(item, "CAMPO_NUM", 0)

      for {
        req1 <- (item \ "CAMPO_REQ_1").extractOpt[String]
        req2 <- (item \ "CAMPO_REQ_2").extractOpt[Double]
      } yield {
        (campoString, campoInt, req1, req2)
      }
    })
  } catch {
    case _: Exception => None
  }
}
```

**Regla para adaptar:** lo que va fuera del `for` es laxo y tiene fallback; lo que va dentro del `for` es obligatorio y descarta el elemento si falta.

### Patrón 2: Minería de Texto y Frecuencias (`@TEXT_MINING`)

**Cuándo usarlo:** cuando te pidan contar palabras frecuentes, extraer menciones, agrupar tokens, filtrar stopwords o devolver un top N.

```scala
def obtenerFrecuenciasTop(elementos: List[TUPLE_TYPE], limite: Int): List[(String, Int)] = {
  val stopwords = Set("el", "la", "los", "las", "un", "una")

  elementos
    .flatMap(e => e._CAMPO_TEXTO.split("\\W+").toList)
    .map(_.toLowerCase)
    .map(_.trim)
    .filter(_.nonEmpty)
    .filter(_.length > 2)
    .filterNot(stopwords.contains)
    .filter(_.startsWith("PREFIJO_OPCIONAL"))
    .groupBy(identity)
    .map { case (palabra, apariciones) => (palabra, apariciones.length) }
    .toList
    .sortBy(-_._2)
    .take(limite)
}
```

**Regla para adaptar:** cambia `_CAMPO_TEXTO`, el prefijo y los filtros; la secuencia `flatMap -> filter -> groupBy -> map -> sortBy -> take` se conserva casi siempre.

### Patrón 3: Reducción/Acumulación con `foldLeft` (`@FOLD_LEFT`)

**Cuándo usarlo:** cuando te pidan calcular totales, acumular scores, combinar elementos en una estructura o resolverlo sin `var`.

```scala
def calcularAcumulado(elementos: List[TUPLE_TYPE]): Int = {
  elementos.foldLeft(0) { (acumulador, item) =>
    val valorASumar = item._CAMPO_A_SUMAR
    acumulador + valorASumar
  }
}

def agruparSumando(elementos: List[TUPLE_TYPE]): Map[String, Int] = {
  elementos.foldLeft(Map.empty[String, Int]) { (mapaAcc, item) =>
    val clave = item._CAMPO_CLAVE
    val valorPrevio = mapaAcc.getOrElse(clave, 0)
    mapaAcc.updated(clave, valorPrevio + item._CAMPO_VALOR)
  }
}
```

**Regla para adaptar:** el primer argumento de `foldLeft` es el valor inicial; el tipo del acumulador final queda determinado por ese valor.

### Patrón 3B: Filtrar Posts Relevantes (`@FILTER_RELEVANT`)

**Cuándo usarlo:** cuando el enunciado diga "eliminar posts sin texto", "solo espacios" o "sin título".

```scala
def esRelevante(post: Post): Boolean = {
  val title = post._2.trim
  val selftext = post._3.trim

  title.nonEmpty && title != "Sin Título" && selftext.nonEmpty
}

val postsLimpios = posts.filter(esRelevante)
```

**Regla para adaptar:** si tu tupla tiene otras posiciones, cambiá `_2` y `_3` por los índices del título y el cuerpo.

### Patrón 3C: Palabras Con Mayúscula y Stopwords (`@STOPWORDS`)

**Cuándo usarlo:** Lab 1 original, ejercicio 5.

```scala
val stopwords = Set("the", "and", "or", "in", "to", "of", "a")

def palabrasMayusculaTop(posts: List[Post], limite: Int): List[(String, Int)] = {
  posts
    .flatMap(post => TextProcessing.tokenize(s"${post._2} ${post._3}"))
    .map(_.trim)
    .filter(_.nonEmpty)
    .filter(word => word.headOption.exists(_.isUpper))
    .map(_.toLowerCase)
    .filterNot(stopwords.contains)
    .groupBy(identity)
    .map { case (word, occurrences) => (word, occurrences.length) }
    .toList
    .sortBy { case (word, count) => (-count, word) }
    .take(limite)
}
```

**Regla para adaptar:** primero se decide si la palabra original empieza con mayúscula; después se normaliza a minúscula para contar equivalencias.

### Patrón 3D: Informe Final del Lab 1 (`@REPORT_LAB1`)

**Cuándo usarlo:** cuando pidan imprimir por suscripción el score total, palabras frecuentes y primeros cinco posts.

```scala
def generarInforme(subreddit: String, posts: List[Post]): String = {
  val cleanPosts = filterRelevantPosts(posts)
  val totalScore = sumarScoresTotales(cleanPosts)
  val topWords = capitalizedWordsTop(cleanPosts, 10)
  val firstFive = firstPostsSummary(cleanPosts, 5)
  val report = new StringBuilder

  report.append(s"## $subreddit\n")
  report.append(s"- Total score: $totalScore\n")
  report.append("- Palabras frecuentes:\n")
  topWords.foreach { case (word, count) =>
    report.append(s"  - $word: $count\n")
  }
  report.append("- Primeros posts:\n")
  firstFive.foreach { case (title, date, url) =>
    report.append(s"  - $title | $date | $url\n")
  }

  report.toString()
}
```

### Patrón 4: Descarga HTTP Segura a `Option` (`@API_FETCH`)

**Cuándo usarlo:** cuando haya que consumir una API externa y el enunciado pida manejar fallos sin romper el programa.

```scala
import scalaj.http.Http

def descargarDatos(url: String): Option[String] = {
  try {
    val response = Http(url)
      .header("User-Agent", "ScalaLab/1.0")
      .timeout(connTimeoutMs = 5000, readTimeoutMs = 10000)
      .asString

    if (response.isSuccess) Some(response.body) else None
  } catch {
    case _: Exception => None
  }
}
```

### Patrón 5: Convertir y Encadenar `Option`

**Cuándo usarlo:** cuando tengas un valor opcional y quieras transformarlo solo si existe.

```scala
import scala.util.Try

val scoreOpt: Option[Int] = Some(10)

val duplicado: Option[Int] =
  scoreOpt.map(score => score * 2)

val textoAInt: Option[Int] =
  Some("42").flatMap(raw => Try(raw.toInt).toOption)

val valorFinal: Int =
  textoAInt.getOrElse(0)
```

### Patrón 6: Fechas UTC (`@FECHAS_UTC`)

**Cuándo usarlo:** cuando la API entregue timestamps UNIX en segundos.

```scala
import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter

def formatDateFromUTC(utcTimestamp: Long): String = {
  val formatter = DateTimeFormatter
    .ofPattern("yyyy-MM-dd HH:mm")
    .withZone(ZoneId.of("UTC"))

  formatter.format(Instant.ofEpochSecond(utcTimestamp))
}
```

### Mini-Reglas de Examen

| Si te piden... | Usá... |
|---|---|
| Campo obligatorio | `for { campo <- extractOpt[T] } yield ...` |
| Campo con default | `extractOpt[T].getOrElse(default)` |
| Campo numérico mixto | `extractOpt[Int].orElse(extractOpt[String].flatMap(Try(...).toOption))` |
| Contar apariciones | `groupBy(identity).map { case (k, xs) => (k, xs.length) }` |
| Top N | `.toList.sortBy(-_._2).take(n)` |
| Acumular total | `foldLeft(0)` |
| Evitar strings vacíos | `.map(_.trim).filter(_.nonEmpty)` |
| Post relevante | `title.trim.nonEmpty && selftext.trim.nonEmpty` |
| Palabra con mayúscula | `word.headOption.exists(_.isUpper)` |

---

## 📦 Tipos de Datos Inmutables

### Option[T] - Manejando la Ausencia

`Option` es un contenedor que puede tener dos valores:
- `Some(valor)` - Contiene un valor
- `None` - No contiene nada

#### Crear Option

```scala
val x: Option[Int] = Some(5)
val y: Option[Int] = None

val z: Option[String] = "hello".take(5).isEmpty match {
  case true => None
  case false => Some("hello")
}
```

#### Extraer Valores

**getOrElse:** Si es None, devuelve default

```scala
val x: Option[Int] = Some(10)
val result1 = x.getOrElse(0)  // 10

val y: Option[Int] = None
val result2 = y.getOrElse(0)  // 0
```

**map:** Aplica función si es Some, devuelve None si es None

```scala
val x: Option[Int] = Some(5)
val result = x.map(_ * 2)  // Some(10)

val y: Option[Int] = None
val result2 = y.map(_ * 2)  // None
```

**flatMap:** Como map, pero la función devuelve Option

```scala
def divide(a: Int, b: Int): Option[Double] =
  if (b == 0) None else Some(a.toDouble / b)

val x: Option[Int] = Some(10)
val result = x.flatMap(v => divide(v, 2))  // Some(5.0)
```

**pattern matching:** Desenvuelve manualmente

```scala
val x: Option[String] = Some("hello")
x match {
  case Some(value) => println(s"Got: $value")
  case None => println("No value")
}
```

---

### Tuplas - Grupos de Datos Inmutables

Una tupla es una colección de elementos heterogéneos (pueden ser de tipos diferentes).

#### Crear Tuplas

```scala
val t1: (String, Int) = ("Scala", 2025)
val t2: (String, String, Int) = ("Scala", "lang", 50)

// Acceso por posición (_1 indexado desde 1, no 0)
println(t1._1)  // "Scala"
println(t1._2)  // 2025
```

#### Acceso a Elementos

```scala
val post: (String, String, String, String, Int, String) =
  ("scala", "Learning Scala", "Great language", "2025-03-15", 125, "https://...")

println(post._1)  // subreddit: "scala"
println(post._2)  // title: "Learning Scala"
println(post._5)  // score: 125
```

#### Pattern Matching con Tuplas

```scala
val (subreddit, title, _, _, score, _) = post

// O en funciones lambda
subscriptions.foreach { case (name, url, minScore) =>
  println(s"$name: score must be >= $minScore")
}
```

---

## 🔄 Colecciones y Funciones de Alto Orden

### map - Transforma cada elemento

```scala
val numbers = List(1, 2, 3)
val doubled = numbers.map(_ * 2)  // List(2, 4, 6)

val posts = List(post1, post2, post3)
val titles = posts.map(_._2)  // Extrae todos los títulos
```

### filter - Mantiene elementos que cumplen predicado

```scala
val numbers = List(1, 2, 3, 4, 5)
val evens = numbers.filter(_ % 2 == 0)  // List(2, 4)

val posts = List(post1, post2, post3)
val highScore = posts.filter(_._5 >= 50)  // Posts con score >= 50
```

### flatMap - Map + Flatten

`flatMap` es crucial para encadenar operaciones que devuelven List/Option.

```scala
val lists = List(List(1, 2), List(3, 4), List(5))
val flat = lists.flatMap(identity)  // List(1, 2, 3, 4, 5)

val words = List("hello world", "scala is great")
val allWords = words.flatMap(_.split(" ").toList)
// List("hello", "world", "scala", "is", "great")
```

Con Option:
```scala
val options = List(Some(1), None, Some(3))
val values = options.flatMap(identity)  // List(1, 3)
```

En for-comprehension (azúcar sintáctico):
```scala
// Esto es lo mismo:
for {
  x <- List(1, 2)
  y <- List("a", "b")
} yield (x, y)

// Que esto:
List(1, 2).flatMap { x =>
  List("a", "b").map { y =>
    (x, y)
  }
}
// Resultado: List((1, "a"), (1, "b"), (2, "a"), (2, "b"))
```

### groupBy - Agrupa por criterio

```scala
val numbers = List(1, 2, 3, 4, 5, 6)
val byParity = numbers.groupBy(_ % 2)
// Map(1 -> List(1, 3, 5), 0 -> List(2, 4, 6))

val posts = List(post1, post2, post3)
val bySubreddit = posts.groupBy(_._1)
// Map("scala" -> [post1, post3], "java" -> [post2])
```

### foldLeft - Acumulador Funcional

```scala
val numbers = List(1, 2, 3, 4)
val sum = numbers.foldLeft(0) { (acc, n) =>
  acc + n
}  // 10

// También:
val sum2 = numbers.foldLeft(0)(_ + _)  // 10
```

Con tuplas:
```scala
val tuples = List(("a", 1), ("b", 2), ("a", 3))
val count = tuples.foldLeft(Map[String, Int]()) { (acc, tuple) =>
  acc + (tuple._1 -> (acc.getOrElse(tuple._1, 0) + tuple._2))
}
// Map("a" -> 4, "b" -> 2)
```

### sortBy - Ordena por criterio

```scala
val numbers = List(3, 1, 4, 1, 5)
val sorted = numbers.sortBy(identity)  // List(1, 1, 3, 4, 5)

val desc = numbers.sortBy(-_)  // List(5, 4, 3, 1, 1), descendente

val tuples = List(("z", 1), ("a", 5), ("m", 2))
val sorted2 = tuples.sortBy(_._1)  // Ordena por primer elemento
```

### take, drop - Subcollecciones

```scala
val numbers = List(1, 2, 3, 4, 5)
val first3 = numbers.take(3)    // List(1, 2, 3)
val rest = numbers.drop(3)      // List(4, 5)
```

---

## 🧵 For-Comprehension (Syntactic Sugar)

Un for-comprehension es azúcar sintáctico para encadenar `flatMap` y `map`.

### Sintaxis Básica

```scala
for {
  x <- List(1, 2, 3)
  y <- List("a", "b")
} yield (x, y)

// Equivalente a:
List(1, 2, 3).flatMap { x =>
  List("a", "b").map { y =>
    (x, y)
  }
}
```

### Con Option

```scala
val a: Option[Int] = Some(5)
val b: Option[String] = Some("hello")

val result = for {
  x <- a
  y <- b
} yield (x, y)  // Some((5, "hello"))

// Si alguno es None, resultado es None:
val result2 = for {
  x <- a
  y <- None: Option[String]
} yield (x, y)  // None
```

### Con Filtros

```scala
for {
  x <- List(1, 2, 3, 4, 5)
  if x > 2
  y <- List("a", "b")
} yield (x, y)

// Equivalente a:
List(1, 2, 3, 4, 5)
  .filter(_ > 2)
  .flatMap { x =>
    List("a", "b").map { y =>
      (x, y)
    }
  }
```

---

## 📝 Strings y Procesamiento de Texto

### split - Divide por delimitador

```scala
val text = "hello world scala programming"
val words = text.split(" ")           // Array("hello", "world", "scala", "programming")
val wordList = text.split(" ").toList  // List("hello", "world", "scala", "programming")

// Con regex
val tokens = text.split("\\W+").toList  // Divide por no-palabras
```

### startsWith, endsWith - Predicados

```scala
val word = "u/spez"
word.startsWith("u/")   // true
word.endsWith("ez")     // true
word.length > 2         // true
```

### Lambdas con Syntactic Sugar

```scala
// Completo:
List(1, 2, 3).map { n => n * 2 }

// Shorthand (_):
List(1, 2, 3).map(_ * 2)

// Con múltiples args:
List((1, 2), (3, 4)).map { case (a, b) => a + b }
```

---

## 🎯 JSON4s - Parseando JSON

### Estructura Básica

```scala
import org.json4s._
import org.json4s.jackson.JsonMethods._

val jsonString = """{"name": "Scala", "score": 50}"""
val json = parse(jsonString)
```

### Extracción

**extractOpt** - Intenta extraer, devuelve Option

```scala
val json = parse("""{"name": "Scala", "score": 50}""")

val name: Option[String] = (json \ "name").extractOpt[String]
val score: Option[Int] = (json \ "score").extractOpt[Int]
val missing: Option[String] = (json \ "nonexistent").extractOpt[String]  // None

// En for-comprehension:
for {
  n <- (json \ "name").extractOpt[String]
  s <- (json \ "score").extractOpt[Int]
} yield (n, s)  // Some(("Scala", 50))
```

**extract** - Extrae o lanza excepción

```scala
val name: String = (json \ "name").extract[String]  // "Scala"
// Si no existe, lanza excepción
```

### Arrays

```scala
val jsonString = """{"items": [{"id": 1}, {"id": 2}]}"""
val json = parse(jsonString)

val items = (json \ "items").children
// items es una List[JValue]

items.foreach { item =>
  val id = (item \ "id").extractOpt[Int]
  println(id)
}
```

---

## 🛠️ Utilidades Prácticas

### Leer Archivo

```scala
val path = "/home/user/file.json"

// Fuente segura: Using siempre ejecuta close(), incluso ante una excepción.
import scala.io.Source
import scala.util.Using

val content: Option[String] =
  Using(Source.fromFile(path, "UTF-8"))(_.mkString).toOption

// Opción 2: con manejo de excepciones
try {
  val content = scala.io.Source.fromFile(path).mkString
  println(content)
} catch {
  case e: Exception => println(s"Error: ${e.getMessage}")
}
```

### Imprimir Formateado

```scala
val avg = 45.6789
println(f"Average: $avg%.2f")  // "Average: 45.68"

// Con String Interpolation:
val name = "Scala"
val score = 50
println(s"$name: $score points")  // "Scala: 50 points"
```

### Manejo de Listas Vacías

```scala
val list = List.empty[String]
list.isEmpty           // true
list.nonEmpty          // false

val result = if (list.nonEmpty) list.head else "default"
```

---

## 🚨 Errores Comunes y Soluciones

| Código | Error | Problema | Solución |
|--------|-------|----------|----------|
| `json.extract[String]` | `matcherror` | Estructura de JSON es diferente | Usa `extractOpt` en lugar de `extract` |
| `post._5 > 50` | `value > is not a member of Option` | Intentaste operar en Option directamente | Desenvuelve: `post._5.map(_ > 50)` |
| `for { x <- None } yield x` | Tipos fuerzan None | Uno de los `<-` devuelve None | Usa `.getOrElse()` antes del for |
| `words.filter(w => w.startsWith("u/"))` | Compila pero no funciona | Lógica incorrecta | Verifica predicado con datos reales |
| `list.groupBy(_._1).map(_._2)` | Tipo retorna Map, no List | `groupBy` devuelve Map | Convierte: `.toList` o `.values.toList` |

---

## 💡 Principios Clave

1. **Inmutabilidad:** Nunca modifiques valores originales
2. **Composición:** Encadena funciones pequeñas
3. **Tipos:** Scala infiere bien, pero sé explícito cuando sea confuso
4. **Option:** Úsalo para manejar "posibles" valores
5. **Tuplas:** Memoriza posiciones (_1, _2, etc.)

---

¡Listo para empezar! 🚀

## ⚠️ Secciones Extra (No Crucial)

Las siguientes secciones son material adicional útil para el laboratorio, pero no son estrictamente necesarias para completar los 3 ejercicios. Se incluyen como referencia para situaciones reales y buenas prácticas.

## 🌐 I/O y Manejo Funcional de Recursos (`scala.util.Using`)

El manejo imperativo utiliza `try/catch/finally` para cerrar archivos o conexiones de red. En Scala moderno, preferimos `Using`, que cierra automáticamente los recursos (como un bloque `try-with-resources` en Java) y se integra perfectamente con colecciones funcionales devolviendo un `Try` que podemos convertir a `Option`.

### Descargar desde una URL de forma segura

```scala
import scala.util.Using
import scala.io.Source

// Devuelve Some(contenido) si tiene éxito, o None si falla la red.
def download(url: String): Option[String] = {
  Using(Source.fromURL(url)) { source =>
    source.mkString
  }.toOption
}
```

---

## 🗃️ Colecciones Optimizadas: `Set`

Mientras que `List` es genial para secuencias, `Set` (Conjunto) es la estructura funcional ideal para verificar membresía (por ejemplo, buscar *stopwords*). Las búsquedas en un `Set` son de tiempo constante $O(1)$.

```scala
val stopwords: Set[String] = Set("the", "and", "or", "in")

val words = List("the", "scala", "and", "data")

// filterNot es lo opuesto a filter. Mantiene lo que NO cumple la condición.
val validWords = words.filterNot(word => stopwords.contains(word))
// List("scala", "data")
```

---

## 🧹 Limpieza Avanzada de Strings

En el procesamiento de feeds, los textos vienen sucios. Debes dominar la limpieza de cadenas sin usar variables temporales.

### `trim` y `nonEmpty`
Ideal para filtrar posts vacíos que solo contienen espacios.

```scala
val text1 = "   "
val text2 = "Scala"

text1.trim.isEmpty   // true (quita los espacios y revisa si está vacío)
text2.trim.nonEmpty  // true
```

### Expresiones Regulares en `split`
Para tokenizar texto ignorando puntuación y considerando caracteres del español:

```scala
val text = "¡Hola, mundo! ¿Cómo están en 2025?"
// Divide el texto usando todo lo que NO sea una letra (incluyendo acentos) como separador
val tokens = text.split("[^A-Za-zÁÉÍÓÚÑáéíóúñ]+").toList.filter(_.nonEmpty)
// List("Hola", "mundo", "Cómo", "están", "en")
```

---

## ⏱️ Interoperabilidad con Java: Fechas y Tiempos

Scala utiliza las robustas bibliotecas de Java (`java.time`) para manejar fechas. Los JSON de Reddit proveen el tiempo en formato UNIX (segundos desde 1970). 

```scala
import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter

val utcFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))

val unixTimestamp: Long = 1711718400L // Viene del JSON (created_utc)
val dateString: String = utcFormatter.format(Instant.ofEpochSecond(unixTimestamp))
// "2024-03-29 00:00:00"
```

---

## 🏛️ Diseño Funcional: "Functional Core, Imperative Shell"

Para resolver el laboratorio correctamente, debes dividir tu código mentalmente en dos áreas:

1. **El Núcleo Funcional (Functional Core):** - Archivos: `RedditParser.scala`, `Statistics.scala`, `TextProcessing.scala`.
   - **Regla:** Cero efectos secundarios. NO usar `println`, NO leer la red, NO variables `var`. Todas las funciones reciben datos (`String`, `List[Post]`) y devuelven datos modificados. Si algo falla, devuelven `Option`.

2. **La Capa Imperativa (Imperative Shell):**
   - Archivos: `Main.scala`, `FileIO.scala`.
   - **Regla:** Aquí ocurre la "suciedad". Es donde lees el archivo `subscriptions.json`, donde haces la petición HTTP para descargar los feeds, y donde finalmente imprimes los resultados a la consola (`println`).

**Ejemplo de Flujo:**
`Main.scala` pide a `FileIO` que descargue un String (Imperativo) → `Main` pasa el String a `RedditParser` para que lo convierta en `List[Post]` (Funcional) → `Main` pasa los posts a `Statistics` para contarlos (Funcional) → `Main` imprime el reporte en pantalla (Imperativo).
