# Guía de Utilidades Técnicas - Scala y Paradigmas Funcionales

Este documento es una referencia rápida de conceptos y herramientas que usarás en los ejercicios.

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

// Opción 1: fuente segura (recomendado)
val content = scala.io.Source.fromFile(path).mkString

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

1. **El Núcleo Funcional (Functional Core):** - Archivos: `RedditParser.scala`, `Statistics.scala`, `WordProcessor.scala`.
   - **Regla:** Cero efectos secundarios. NO usar `println`, NO leer la red, NO variables `var`. Todas las funciones reciben datos (`String`, `List[Post]`) y devuelven datos modificados. Si algo falla, devuelven `Option`.

2. **La Capa Imperativa (Imperative Shell):**
   - Archivos: `Main.scala`, `FileIO.scala`.
   - **Regla:** Aquí ocurre la "suciedad". Es donde lees el archivo `subscriptions.json`, donde haces la petición HTTP para descargar los feeds, y donde finalmente imprimes los resultados a la consola (`println`).

**Ejemplo de Flujo:**
`Main.scala` pide a `FileIO` que descargue un String (Imperativo) → `Main` pasa el String a `RedditParser` para que lo convierta en `List[Post]` (Funcional) → `Main` pasa los posts a `Statistics` para contarlos (Funcional) → `Main` imprime el reporte en pantalla (Imperativo).

