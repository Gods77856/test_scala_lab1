# Reddit Scala Lab - Guía de Estudio y Ejercicios

Esta guía explica cómo mapear una consigna nueva del parcial a un patrón funcional. El código fuente Scala contiene referencias compilables y las soluciones están explicadas en los archivos `.md`.

Para el parcial efectivamente evaluado en 2026, usar [PARCIAL_EVALUADO_2026.md](./PARCIAL_EVALUADO_2026.md). Esta guía conserva las variantes del laboratorio original y de práctica.

---

## 🧠 MAPA DE RESOLUCIÓN PARA EL PARCIAL

Si en el enunciado del parcial lees:

| Si la consigna dice... | Tu acción debe ser... | Archivo / función de referencia |
|---|---|---|
| "Ignorar/descartar campos faltantes" o "si falta X no incluir el elemento" | Usar `for-comprehension` con `extractOpt`. Si aparece `None`, ese elemento se descarta automáticamente. | `RedditParser.scala` -> `parsePosts`; `GUT.md` -> Patrón 1 |
| "Si el campo falla, usar X por defecto" | Extraer fuera del `for-comprehension` con `.extractOpt[T].getOrElse(X)`. | `RedditParser.scala` -> `extractedTitle` |
| "El número puede venir como texto o entero" | Probar `extractOpt[Int]` y luego `extractOpt[String].flatMap(Try(...).toOption)`. | `FileIO.scala` -> `minScoreFrom`; `GUT.md` -> Patrón 1 |
| "No tengan texto", "solo espacios" o "sin título" | Filtrar con `.trim.nonEmpty` sobre título y selftext. | `Statistics.scala` -> `filterRelevantPosts`; tag `@FILTER_RELEVANT` |
| "Calcular el total de..." o "acumular..." | No usar `var`. Usar `foldLeft(valorInicial) { (acc, elem) => ... }`. | `Statistics.scala` -> `sumarScoresTotales`; `GUT.md` -> Patrón 3 |
| "Frecuencia de palabras", "palabras más usadas", "extraer menciones" | Usar `flatMap` para tokenizar, `filter`, `groupBy(identity)`, `map` para contar, `sortBy`, `take`. | `Statistics.scala` -> `mentionsTop`; `GUT.md` -> Patrón 2 |
| "Palabras que comiencen con mayúscula" y "stopwords" | Filtrar por `headOption.exists(_.isUpper)`, normalizar a minúscula y excluir `Stopwords`. | `Statistics.scala` -> `capitalizedWordsTop`; tag `@STOPWORDS` |
| "Rechazar strings vacíos o nulos" | Normalizar con `.map(_.trim)` y filtrar con `.filter(_.nonEmpty)`. | `GUT.md` -> Patrón 2 |
| "Convertir `Option` a otro tipo si existe" | Usar `.map()`; si la función devuelve otro `Option`, usar `.flatMap()`. | `GUT.md` -> Patrón 5 |
| "Descargar una API y tolerar fallos" | Encapsular HTTP en `try/catch` y devolver `Option[String]`. | `FileIO.scala` -> `downloadFeed`; `GUT.md` -> Patrón 4 |
| "Convertir fecha UNIX/UTC" | Usar `Instant.ofEpochSecond` y `DateTimeFormatter` con `ZoneId.of("UTC")`. | `TextProcessing.scala` -> `formatDateFromUTC` |
| "Informe final", "score total", "primeros cinco posts" | Combinar `filterRelevantPosts`, `sumarScoresTotales`, `capitalizedWordsTop` y `firstPostsSummary`. | `Statistics.scala` -> `generateLab1Report`; tag `@REPORT_LAB1` |

### Cómo Usar Este Mapa

1. Subrayá mentalmente el verbo de la consigna: parsear, filtrar, contar, acumular, descargar.
2. Buscá el tag correspondiente con `Ctrl+F`: `@JSON_PARSE`, `@TEXT_MINING`, `@FOLD_LEFT`, etc.
3. Intentá completar el skeleton sin mirar la solución.
4. Si te trabás, copiá la implementación más parecida desde esta guía, `GUT.md` o `GUIA_PARCIAL_LAB1.md`.
5. Cambiá rutas JSON, nombres de campos y posiciones de tupla.
6. Corré `sbt test` o un test pequeño propio.

---

## 📚 Estructura del Proyecto

```
test_lab1/
├── build.sbt                           # Configuración de Scala y dependencias
├── src/
│   └── main/
│       ├── scala/                      # Código fuente Scala
│       │   ├── FileIO.scala            # Ejercicio 1 (Paso 1)
│       │   ├── RedditParser.scala      # Ejercicio 3
│       │   ├── Statistics.scala        # Ejercicio 2
│       │   ├── TextProcessing.scala    # Utilidades
│       │   └── Main.scala              # Ejercicio 1 (Paso 2)
│       └── resources/
│           └── subscriptions.json      # Datos de prueba
├── EJERCICIOS.md                       # Este archivo
├── GUIA_PARCIAL_LAB1.md                # Defensa conceptual y cobertura de PDFs
└── GUT.md                              # Guía de Utilidades Técnicas y plantillas copiables
```

---

## 📌 Lab 1 Original vs Variantes de Práctica

El PDF original pide seis ejercicios: leer suscripciones, descargar posts, filtrar irrelevantes, manejar errores con `Option`, contar palabras con mayúscula y stopwords, y generar estadísticas con `fold`.

Este repo además incluye variantes de práctica para parcial, como `minScore` en suscripciones, menciones `u/usuario` y fallback `"Sin Título"`. Si el parcial copia la consigna original, usá las funciones marcadas como `@FILTER_RELEVANT`, `@STOPWORDS` y `@REPORT_LAB1`. Si el parcial pide tolerancia con defaults, usá `@TOLERANCIA_FALLOS`.

---

## 🎯 Conceptos Clave a Dominar

Antes de adaptar cualquier plantilla, internaliza estos conceptos:

### 1. **Rechaza la Mutación** 
- ❌ NO uses `var count = 0` y `for` loops
- ✅ USA `foldLeft`, `map`, `filter`, `groupBy`
- La mutación hace que código sea impredecible. En Scala, asumimos **inmutabilidad total**.

### 2. **Manejo de la Ausencia (Option)**
- Las operaciones pueden fallar: leer JSON, descargar URLs, extraer campos
- `Option[T]` es un tipo que representa "posiblemente" un valor
  - `Some(valor)` → tengo el valor
  - `None` → no tengo valor (operación falló)
- Para encadenar operaciones seguras:
  - `getOrElse(default)` → si None, usa default
  - `map(f)` → si Some, aplica f, si None devuelve None
  - `flatMap(f)` → como map, pero f devuelve Option

### 3. **Tuplas vs Case Classes**
- Tu tipo `Post` es una tupla `(String, String, String, String, Int, String)`
- Acceso: `_1`, `_2`, `_3`, `_4`, `_5`, `_6`
- **Memoriza qué posición es cada cosa:**
  - `_1` = subreddit
  - `_2` = title
  - `_3` = selftext (cuerpo del post)
  - `_4` = date
  - `_5` = score ← **IMPORTANTE para ejercicio 1**
  - `_6` = url

---

## 📋 Ejercicio 1: Extensión de Configuración y Filtrado

**Tema:** Adaptación de tipos inmutables + uso de `filter`

### Enunciado
Se agregó el campo `"minScore"` al JSON de suscripciones. Debes:
1. Modificar el tipo `Subscription` para incluir `minScore: Int`
2. Implementar `FileIO.readSubscriptions()` para parsearlo
3. En `Main.scala`, filtrar posts para conservar aquellos cuyo `score` sea mayor o igual a `minScore` (usar `post._5 >= minScore`).

### Archivos Involucrados
- [FileIO.scala](./src/main/scala/FileIO.scala) - Paso 1
- [Main.scala](./src/main/scala/Main.scala) - Paso 2

### JSON de Entrada

```json
[
  {
    "name": "Scala",
    "url": "https://www.reddit.com/r/scala/.json",
    "minScore": 50
  },
  {
    "name": "Java",
    "url": "https://www.reddit.com/r/java/.json"
  }
]
```

**Nota:** El campo `"minScore"` puede no existir → usar default 0

### Paso 1: Reconocer `FileIO.readSubscriptions()`

**Ubicación:** [FileIO.scala](./src/main/scala/FileIO.scala)

**Qué hace el patrón `@JSON_PARSE`:**
1. Lee el archivo del path
2. Parsea con json4s: `parse(contenido)`
3. Extrae la lista de objetos JSON y, para cada elemento, usa `extractOpt` para cada campo;
   maneja `minScore` de forma robusta (puede venir como `Int` o `String`).

**Pseudocódigo (extracción segura):**
```scala
import scala.util.Try

def readSubscriptions(path: String): Option[List[Subscription]] = {
  try {
    val cont = scala.io.Source.fromFile(path).mkString
    val json = parse(cont)
    // Si el root es un array, children contiene cada objeto
    val items = json.children
    val subs = items.map { item =>
      val name = (item \ "name").extractOpt[String].getOrElse("")
      val url = (item \ "url").extractOpt[String].getOrElse("")
      val minScore = (item \ "minScore").extractOpt[Int]
        .orElse((item \ "minScore").extractOpt[String].flatMap(raw => Try(raw.trim.toInt).toOption))
        .getOrElse(0)
      (name, url, minScore)
    }.toList
    Some(subs)
  } catch {
    case _: Exception => None
  }
}
```

### Paso 2: Aplicar filtro en `Main.scala`

**Ubicación:** [Main.scala](./src/main/scala/Main.scala)

**Qué hace el patrón de flujo principal:**
1. En el `flatMap` que recorre `subscriptions`
2. Después de parsear posts con `RedditParser.parsePosts()`
3. **Filtra:** `val filteredPosts = posts.filter(post => post._5 >= minScore)`
4. Imprime cantidad filtrada

**Pseudocódigo:**
```scala
val allPosts: List[Post] = subscriptions.flatMap { case (name, url, minScore) =>
  FileIO.downloadFeed(url) match {
    case Some(jsonString) =>
      RedditParser.parsePosts(jsonString) match {
        case Some(posts) =>
          val filteredPosts = posts.filter(post => post._5 >= minScore)
          println(s"  ✓ Parsed ${filteredPosts.length} posts (Score >= $minScore) from r/$name")
          filteredPosts
        case None =>
          println(s"  ✗ Failed to parse posts from r/$name")
          List.empty
      }
    case None =>
      println(s"  ✗ Failed to download feed from r/$name")
      List.empty
  }
}
```

**Concepto clave:**
- `filter` es una función de alto orden que toma un predicado (función que devuelve Boolean)
- No modifica la lista original (inmutabilidad)
- Perfectamente composable con otras funciones

---

## 📋 Ejercicio 2: Nueva Heurística de Minería de Texto

**Tema:** Composición de funciones de alto orden (map, flatMap, filter, groupBy)

### Enunciado
Implementa `mentionsTop(posts, limit)` que devuelve las N menciones a usuarios más frecuentes.
- Una mención es una palabra que comienza con `u/` (ej: `u/spez`)
- Retorna lista de tuplas `(usuario, cantidad)` ordenadas por cantidad descendente

### Archivos Involucrados
- [Statistics.scala](./src/main/scala/Statistics.scala) - Implementar `mentionsTop()`
- [Main.scala](./src/main/scala/Main.scala) - Integrar en reporte (comentado)

### Ejemplo de Entrada y Salida

**Posts:**
```
Post 1 selftext: "u/spez is the founder, u/spez also created..."
Post 2 selftext: "u/torvalds_right should check this u/spez"
```

**Salida esperada de `mentionsTop(posts, 3)`:**
```
List(
  ("u/spez", 3),
  ("u/torvalds_right", 1),
  ("u/founder", 1)
)
```

### Pipeline Funcional (7 pasos)

Este es el **corazón de la programación funcional**:

```
Posts → flatMap (extraer selftext) 
     → split (tokenizar) 
     → filter (solo u/*) 
     → filter (> 2 chars) 
     → groupBy (agrupar iguales) 
     → map (contar) 
     → sortBy (descendente) 
     → take (top N)
```

### Paso a Paso

**Paso 1: Extrae el selftext de cada post y tokeniza**
```scala
posts
  .flatMap(post => TextProcessing.tokenize(post._3))
  // Ahora tenemos: List("u/spez", "is", "the", "founder", ...)
```

**Paso 2: Filtra solo menciones (empiezan con "u/")**
```scala
  .filter(_.startsWith("u/"))
  // Ahora tenemos: List("u/spez", "u/spez", "u/torvalds_right", ...)
```

**Paso 3: Descarta "u/" sueltos**
```scala
  .filter(_.length > 2)
  // Ahora tenemos: List("u/spez", "u/spez", "u/torvalds_right", ...)
```

**Paso 4: Agrupa menciones iguales**
```scala
  .groupBy(identity)
  // Ahora tenemos: Map("u/spez" -> List("u/spez", "u/spez"), 
  //                   "u/torvalds_right" -> List("u/torvalds_right"))
```

**Paso 5: Transforma a (mención, cantidad)**
```scala
  .map { case (mention, occurrences) => (mention, occurrences.length) }
  // Ahora tenemos: List(("u/spez", 2), ("u/torvalds_right", 1))
```

**Paso 6: Convierte a List y ordena descendente**
```scala
  .toList
  .sortBy(-_._2)  // El - invierte el orden (descendente)
  // Ahora tenemos: List(("u/spez", 2), ("u/torvalds_right", 1))
```

**Paso 7: Toma top N**
```scala
  .take(limit)
  // Resultado final
```

### Implementación Completa de Referencia

**Ubicación:** [Statistics.scala](./src/main/scala/Statistics.scala), buscá `@TEXT_MINING`.

```scala
def mentionsTop(posts: List[Post], limit: Int): List[(String, Int)] = {
  posts
    .flatMap(post => TextProcessing.tokenize(post._3))
    .filter(_.startsWith("u/"))
    .filter(_.length > 2)
    .groupBy(identity)
    .map { case (mention, occurrences) => (mention, occurrences.length) }
    .toList
    .sortBy(-_._2)
    .take(limit)
}
```

### Integración en Reporte

**Ubicación:** [Statistics.scala](./src/main/scala/Statistics.scala) - función `generateSubredditReport()`

Descomenta y completa este código en la función:

```scala
val topMentions = mentionsTop(posts, 3)
report.append("\nTop User Mentions:\n")
topMentions.foreach { case (user, count) =>
  report.append(s"  - $user: $count\n")
}
```

**Concepto clave:**
- `flatMap` = map + flatten (unifica sublistas)
- `groupBy` devuelve `Map`, no `List`
- Encadenar transformaciones sin bucles ni variables mutables

---

## 📋 Ejercicio 3: Tolerancia a Fallos en el Parseo

**Tema:** Manejo declarativo de errores con `Option` y `getOrElse`

### Enunciado
La API cambió: algunos posts no tienen campo `"title"`. 
- Si falta `"title"`, usar `"Sin Título"` en lugar de descartar el post
- Debes mantener TODOS los otros campos requeridos

### Archivos Involucrados
- [RedditParser.scala](./src/main/scala/RedditParser.scala) - Implementar tolerancia

### Conceptualmente: ¿Por qué falla el parsing actual?

El for-comprehension tradicional:
```scala
for {
    subreddit <- (data \ "subreddit").extractOpt[String]
    title <- (data \ "title").extractOpt[String]  // Si falla, TODA la operación falla
    selftext <- (data \ "selftext").extractOpt[String]
    // ...
} yield { ... }
```
Se plantea usar flatMap

Si `title` devuelve `None`, el `for` completo devuelve `None`.

### Solución: Extrae `title` FUERA del for

```scala
// Extrae title CON fallback
val extractedTitle = (data \ "title").extractOpt[String].getOrElse("Sin Título")

// CASO for-comprehension
for {
    subreddit <- (data \ "subreddit").extractOpt[String]
    // NO incluir title en el for
    selftext <- (data \ "selftext").extractOpt[String]
    createdUtc <- (data \ "created_utc").extractOpt[Double]
    score <- (data \ "score").extractOpt[Int]
    url <- (data \ "url").extractOpt[String]
} yield {
    // Usar extractedTitle en el yield
    val date = TextProcessing.formatDateFromUTC(created_utc.toLong)
    (subreddit, extractedTitle, selftext, date, score, url)
}

// CASO flatmap
(data \ "subreddit").extractOpt[String].flatMap {subreddit =>
  (data \ "selftext").extractOpt[String].flatMap { selftext =>
    (data \ "created_utc").extractOpt[Double].flatMap { created_utc =>
      (data \ "score").extractOpt[Int].flatMap { score =>
        (data \ "url").extractOpt[String].map { url =>
          val date = TextProcessing.formatDateFromUTC(created_utc.toLong)
          (subreddit, ext_title, selftext, date, score, url)
        }
      }
    }
  }
}
```

### Implementación de Referencia

**Ubicación:** [RedditParser.scala](./src/main/scala/RedditParser.scala), buscá `@TOLERANCIA_FALLOS`.

```scala
def parsePosts(jsonString: String): Option[List[Post]] = {
  try {
    val json = parse(jsonString)
    val children = (json \ "data" \ "children").children
    
    Some(children.flatMap { child =>
      val data = child \ "data"
      
      // TOLERANCIA A FALLOS: Extrae title con fallback
      val extractedTitle = (data \ "title").extractOpt[String].getOrElse("Sin Título")
      
      for {
        subreddit <- (data \ "subreddit").extractOpt[String]
        selftext <- (data \ "selftext").extractOpt[String]
        createdUtc <- (data \ "created_utc").extractOpt[Double]
        score <- (data \ "score").extractOpt[Int]
        url <- (data \ "url").extractOpt[String]
      } yield {
        val date = TextProcessing.formatDateFromUTC(createdUtc.toLong)
        (subreddit, extractedTitle, selftext, date, score, url)
      }
    })
    
  } catch {
    case _: Exception => None
  }
}
```

**Concepto clave:**
- `Option[T]` es una mónada: encadena operaciones seguras
- `getOrElse(default)` desenvuelve `Some(v)` a `v`, o devuelve `default` si es `None`
- Dentro de un `for-comprehension`, si es `None`, aborta (comportamiento esperado)
- Fuera del `for`, puedes manejar `None` explícitamente

---

## 🛠️ Cómo Compilar y Ejecutar

### Requisitos
- Scala 2.13+ instalado
- `sbt` (Scala Build Tool) instalado

### Compilar
```bash
cd test_lab1
sbt compile
```

### Ejecutar
```bash
sbt run
```

### Ejecutar con recompilación automática
```bash
sbt "~run"
```

### Testear
```bash
sbt "test"
```

---

## 📝 Checklist de Práctica

- [ ] **Ejercicio 1 - Paso 1:** Entender y poder recrear `FileIO.readSubscriptions()`
  - [ ] Leer archivo JSON
  - [ ] Parsear con json4s
  - [ ] Extraer lista de mapas
  - [ ] Transformar a tuplas con minScore
  
- [ ] **Ejercicio 1 - Paso 2:** Aplicar filtro en `Main.scala`
  - [ ] Descomentar el pseudocódigo
  - [ ] Implementar filtro `.filter(post => post._5 >= minScore)`
  - [ ] Verificar impresión de cantidad
  
- [ ] **Ejercicio 2:** Entender y poder recrear `Statistics.mentionsTop()`
  - [ ] Armar pipeline completo (7 pasos)
  - [ ] Integrar en `generateSubredditReport()`
  - [ ] Descomentar impresión de top mentions
  
- [ ] **Ejercicio 3:** Entender y poder recrear tolerancia en `RedditParser.parsePosts()`
  - [ ] Extraer title con fallback
  - [ ] Mantener for-comprehension limpio
  - [ ] Usar extractedTitle en yield

---

## 💡 Consejos de Estudio

1. **Antes de codificar:** Lee el pseudocódigo al menos 2 veces
2. **Tipos primero:** Asegúrate que los tipos coincidan (Scala es fuertemente tipado)
3. **Descomposición:** Si una función es larga, extrae sub-funciones
4. **Testing mental:** Simula manualmente qué hacer con datos pequeños
5. **Revisa los errores de compilación:** Scala dará mensajes muy claros

### Errores Comunes

| Error | Causa | Solución |
|-------|-------|----------|
| `value _5 is not a member of Option` | Intentaste acceder a tupla dentro de Option | Usa `map(_._5)` o desenvuelve con `match` |
| `type mismatch` | Los tipos no coinciden | Revisa qué tipos espera y qué devuelves |
| `No viable alternative` | Sintaxis incorrecta | Revisa paréntesis, llaves, punto y coma |
| `ambiguous reference` | Existe el mismo nombre en dos módulos | Califica: `FileIO.downloadFeed()` |

---

## 📚 Referencias Rápidas

- [Scala Collections Cheatsheet](https://docs.scala-lang.org/overviews/collections-2.13/overview.html)
- [Option Monad Documentation](https://www.scala-lang.org/api/2.13.x/scala/Option.html)
- [JSON4s Documentation](https://github.com/json4s/json4s)

---

¡Éxito en tu aprendizaje! 🚀
