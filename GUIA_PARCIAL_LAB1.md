# Guía de Parcial y Defensa - Lab 1

Esta guía resume los dos PDFs revisados:

- `Consigna Lab 1.pdf`: laboratorio "Curador Funcional de Feeds de Reddit".
- `programación funcional en Scala.pdf`: conceptos base de programación funcional vistos en clase.

El objetivo del parcial no es memorizar este repo, sino defender que entendés por qué la solución es funcional y poder adaptar un skeleton parecido.

---

## Veredicto de Cobertura

Con los agregados actuales, este repo sí puede usarse como guía detallada para resolver variantes razonables del Lab 1:

- Tiene código de referencia que compila para lectura JSON, descarga HTTP, parsing, filtros, conteos, acumulación y reportes.
- Tiene tags buscables con `Ctrl+F` para llegar rápido a cada patrón.
- Cubre tanto la consigna original del PDF como variantes típicas de parcial, por ejemplo tolerar campos faltantes con defaults.
- Mantiene separados el núcleo funcional (`RedditParser`, `Statistics`, `TextProcessing`) y la capa con efectos (`FileIO`, `Main`).

Único límite: el punto estrella interactivo está cubierto como diseño recomendado, no como feature principal. Si el parcial pidiera exactamente eso, se puede implementar con el patrón indicado al final de esta guía.

---

## Mapa de Consigna Original a Repo

| PDF | Qué pide | Dónde está en el repo |
|---|---|---|
| Ej. 1 | Leer suscripciones desde JSON relativo, cerrar recursos, crear `Subscription` con funciones funcionales | `FileIO.readSubscriptions`, tag `@JSON_PARSE` |
| Ej. 2 | Descargar JSON, extraer `title`, `selftext`, `created_utc`, canonicalizar fecha y crear `Post` | `FileIO.downloadFeed`, `RedditParser.parsePosts`, `TextProcessing.formatDateFromUTC` |
| Ej. 3 | Eliminar posts sin texto, con espacios o sin título usando alto orden | `Statistics.filterRelevantPosts`, tag `@FILTER_RELEVANT`; variante estricta en `RedditParser.parsePostsStrictTitle` |
| Ej. 4 | Manejar fallas de descarga, campos faltantes y JSON inválido con `Option` | `downloadFeed`, `readSubscriptions`, `parsePosts`; tags `@API_FETCH`, `@TOLERANCIA_FALLOS`, `@STRICT_FIELDS` |
| Ej. 5 | Contar palabras que empiezan con mayúscula y no son stopwords | `Statistics.capitalizedWordsTop`, tag `@STOPWORDS` |
| Ej. 6 | Calcular score total con `fold`, no solo `map`; imprimir informe con score, palabras frecuentes y primeros cinco posts | `Statistics.sumarScoresTotales`, `Statistics.generateLab1Report`, tags `@FOLD_LEFT`, `@REPORT_LAB1` |
| Punto estrella | Menú interactivo sin romper diseño funcional | Ver "Patrón para el punto estrella" en esta guía |

---

## Tipos del Dominio

La consigna original empieza con tipos chicos y luego los extiende. Este repo usa un tipo final que es un superset:

```scala
type Subscription = (String, String, Int)
// _1 name, _2 url, _3 minScore

type Post = (String, String, String, String, Int, String)
// _1 subreddit, _2 title, _3 selftext, _4 date, _5 score, _6 url
```

Si el skeleton del parcial usa menos campos, eliminá posiciones. Si usa más campos, agregalas manteniendo el mismo patrón de extracción y filtrado.

---

## Decisiones Que Hay Que Saber Defender

### Por qué no usar `var`

Una solución con `var` cambia estado a lo largo del tiempo. Eso rompe la independencia declarativa y hace más difícil razonar sobre el resultado.

```scala
// Imperativo
var total = 0
for (post <- posts) {
  total = total + post._5
}

// Funcional
val total = posts.foldLeft(0) { (acc, post) => acc + post._5 }
```

### Por qué usar `Option`

Las fallas esperables forman parte del tipo de retorno:

```scala
def downloadFeed(url: String): Option[String]
def parsePosts(jsonString: String): Option[List[Post]]
```

Eso evita propagar excepciones como mecanismo principal. Internamente puede haber `try/catch` porque JSON, archivos y HTTP son APIs imperativas, pero hacia afuera la función devuelve `Some` o `None`.

### Cuándo usar campo estricto o campo con fallback

Campo estricto: si falta, se descarta el elemento.

```scala
for {
  title <- (data \ "title").extractOpt[String]
  url <- (data \ "url").extractOpt[String]
} yield (title, url)
```

Campo laxo: si falta, se conserva el elemento con default.

```scala
val title = (data \ "title").extractOpt[String].getOrElse("Sin Título")
```

En el PDF original, "no tengan título" se descarta: usá `parsePostsStrictTitle` o `filterRelevantPosts`.

### Por qué `flatMap` aparece tanto

`flatMap` transforma y aplana. Sirve para:

- Convertir `List[Post]` en `List[String]` de palabras.
- Convertir `List[Option[Post]]` en `List[Post]` descartando `None`.
- Encadenar operaciones que pueden fallar.

```scala
posts.flatMap(post => TextProcessing.tokenize(post._3))
```

### Cómo defender `foldLeft`

`foldLeft` reemplaza un acumulador mutable por un acumulador inmutable que se pasa de paso en paso.

```scala
posts.foldLeft(0) { (acc, post) =>
  acc + post._5
}
```

El `0` define el valor inicial y el tipo del acumulador. El resultado final es un `Int`.

---

## Plantillas Exactas Para El Parcial

### Descargar y Parsear

```scala
val allPosts: List[Post] = subscriptions.flatMap { case (name, url, minScore) =>
  FileIO.downloadFeed(url) match {
    case Some(jsonString) =>
      RedditParser.parsePosts(jsonString) match {
        case Some(posts) => posts.filter(post => post._5 >= minScore)
        case None => List.empty
      }
    case None => List.empty
  }
}
```

### Filtrar Posts Irrelevantes

```scala
def isRelevantPost(post: Post): Boolean = {
  val title = post._2.trim
  val selftext = post._3.trim
  title.nonEmpty && title != "Sin Título" && selftext.nonEmpty
}

val cleanPosts = posts.filter(isRelevantPost)
```

### Contar Palabras Con Mayúscula y Sin Stopwords

```scala
def capitalizedWordsTop(posts: List[Post], limit: Int): List[(String, Int)] = {
  posts
    .flatMap(post => TextProcessing.tokenize(s"${post._2} ${post._3}"))
    .map(_.trim)
    .filter(_.nonEmpty)
    .filter(word => word.headOption.exists(_.isUpper))
    .map(_.toLowerCase)
    .filterNot(Statistics.Stopwords.contains)
    .groupBy(identity)
    .map { case (word, occurrences) => (word, occurrences.length) }
    .toList
    .sortBy { case (word, count) => (-count, word) }
    .take(limit)
}
```

### Informe Final del Ejercicio 6

```scala
val cleanPosts = Statistics.filterRelevantPosts(posts)
val totalScore = Statistics.sumarScoresTotales(cleanPosts)
val topWords = Statistics.capitalizedWordsTop(cleanPosts, 10)
val firstFive = Statistics.firstPostsSummary(cleanPosts, 5)
```

---

## Preguntas Típicas de Defensa

| Pregunta | Respuesta corta defendible |
|---|---|
| ¿Qué hace que tu solución sea funcional? | Evita estado mutable, separa efectos de lógica pura y usa transformaciones sobre colecciones. |
| ¿Dónde hay efectos secundarios? | En `FileIO` y `Main`: leer archivos, HTTP e imprimir. El análisis está en funciones puras. |
| ¿Por qué `val` y no `var`? | `val` preserva inmutabilidad; `var` introduce asignación destructiva. |
| ¿Por qué `Option`? | Hace explícita la posibilidad de fallo en el tipo. |
| ¿Por qué `try/catch` aparece igual? | Porque algunas APIs externas lanzan excepciones; las encapsulamos y devolvemos `Option`. |
| ¿Qué diferencia `map` de `flatMap`? | `map` transforma uno a uno; `flatMap` transforma y aplana listas u opciones. |
| ¿Qué hace `groupBy(identity)`? | Agrupa elementos iguales en un `Map[elemento, List[elemento]]`. |
| ¿Por qué `foldLeft` y no `map(...).sum`? | La consigna pide combinar con acumulador inmutable; `foldLeft` muestra explícitamente ese acumulador. |
| ¿Qué es pattern matching? | Seleccionar comportamiento según la forma/tipo del dato, por ejemplo `Some/None` o tuplas. |
| ¿Qué es transparencia referencial? | Una función con los mismos parámetros produce siempre el mismo resultado y no depende del contexto externo. |

---

## Pattern Matching Útil

```scala
option match {
  case Some(value) => value
  case None => defaultValue
}

post match {
  case (subreddit, title, selftext, date, score, url) =>
    s"$subreddit | $title | $score"
}
```

Usalo cuando el skeleton pida manejar casos explícitos. Para pipelines simples, `map`, `flatMap` y `getOrElse` suelen ser más compactos.

---

## Patrón Para El Punto Estrella

Separá selección interactiva de lógica de dominio:

```scala
def postAt(posts: List[Post], index: Int): Option[Post] = {
  posts.lift(index)
}

def renderPost(post: Post): String = {
  s"""Title: ${post._2}
     |Date: ${post._4}
     |URL: ${post._6}
     |
     |${post._3}
     |""".stripMargin
}
```

La lectura de teclado (`scala.io.StdIn.readLine`) pertenece a `Main.scala`. `postAt` y `renderPost` son funciones puras y testeables.

---

## Soluciones de Referencia Para Chequear

El codigo Scala del repo queda como skeleton. Cuando termines tu intento, compara contra estas soluciones.

### `FileIO.scala`

```scala
import scalaj.http.Http
import scala.util.Try

private def minScoreFrom(item: JValue): Int = {
  (item \ "minScore").extractOpt[Int]
    .orElse((item \ "minScore").extractOpt[String].flatMap(raw => Try(raw.trim.toInt).toOption))
    .getOrElse(0)
}

private def subscriptionFrom(item: JValue): Subscription = {
  val name = (item \ "name").extractOpt[String].getOrElse("Unknown")
  val url = (item \ "url").extractOpt[String].getOrElse("")
  val minScore = minScoreFrom(item)
  (name, url, minScore)
}

def readSubscriptions(path: String): Option[List[Subscription]] = {
  try {
    val source = scala.io.Source.fromFile(path)
    val content = try {
      source.mkString
    } finally {
      source.close()
    }
    val json = parse(content)
    Some(json.children.map(subscriptionFrom))
  } catch {
    case _: Exception => None
  }
}

def downloadFeed(url: String): Option[String] = {
  try {
    val response = Http(url)
      .header("User-Agent", "RedditScalaLab/1.0")
      .timeout(connTimeoutMs = 5000, readTimeoutMs = 10000)
      .asString

    if (response.isSuccess) Some(response.body) else None
  } catch {
    case _: Exception => None
  }
}

def loadSubscriptionsFromResources(): Option[List[Subscription]] = {
  try {
    val inputStream = getClass.getResourceAsStream("/subscriptions.json")
    if (inputStream == null) {
      None
    } else {
      val content = scala.io.Source.fromInputStream(inputStream).mkString
      inputStream.close()
      val json = parse(content)
      Some(json.children.map(subscriptionFrom))
    }
  } catch {
    case _: Exception => None
  }
}
```

### `RedditParser.scala`

```scala
private def parsePostsWithTitlePolicy(jsonString: String, fallbackTitle: Option[String]): Option[List[Post]] = {
  try {
    val json = parse(jsonString)
    val children = (json \ "data" \ "children").children

    Some(children.flatMap { child =>
      val data = child \ "data"
      val titleOpt = fallbackTitle match {
        case Some(defaultTitle) => Some((data \ "title").extractOpt[String].getOrElse(defaultTitle))
        case None => (data \ "title").extractOpt[String]
      }

      for {
        subreddit <- (data \ "subreddit").extractOpt[String]
        title <- titleOpt
        selftext <- (data \ "selftext").extractOpt[String]
        createdUtc <- (data \ "created_utc").extractOpt[Double]
        score <- (data \ "score").extractOpt[Int]
        url <- (data \ "url").extractOpt[String]
      } yield {
        val date = TextProcessing.formatDateFromUTC(createdUtc.toLong)
        (subreddit, title, selftext, date, score, url)
      }
    })
  } catch {
    case _: Exception => None
  }
}

def parsePosts(jsonString: String): Option[List[Post]] = {
  parsePostsWithTitlePolicy(jsonString, fallbackTitle = Some("Sin Título"))
}

def parsePostsStrictTitle(jsonString: String): Option[List[Post]] = {
  parsePostsWithTitlePolicy(jsonString, fallbackTitle = None)
}
```

### `TextProcessing.scala`

```scala
def formatDateFromUTC(utcTimestamp: Long): String = {
  val instant = Instant.ofEpochSecond(utcTimestamp)
  val formatter = DateTimeFormatter
    .ofPattern("yyyy-MM-dd HH:mm")
    .withZone(ZoneId.of("UTC"))
  formatter.format(instant)
}

def tokenize(text: String): List[String] = {
  val mentionPattern = """(u/\w+|r/\w+|\w+)""".r
  mentionPattern.findAllIn(text).toList.filter(_.nonEmpty)
}
```

### `Statistics.scala`

```scala
def basicStats(posts: List[Post]): String = {
  if (posts.isEmpty) "No posts available"
  else {
    val avgScore = posts.map(_._5).sum.toDouble / posts.length
    f"Total: ${posts.length} posts | Avg Score: $avgScore%.2f"
  }
}

def sumarScoresTotales(posts: List[Post]): Int = {
  posts.foldLeft(0) { (acumulador, post) =>
    acumulador + post._5
  }
}

def isRelevantPost(post: Post): Boolean = {
  val title = post._2.trim
  val selftext = post._3.trim
  title.nonEmpty && title != "Sin Título" && selftext.nonEmpty
}

def filterRelevantPosts(posts: List[Post]): List[Post] = {
  posts.filter(isRelevantPost)
}

def capitalizedWordsTop(posts: List[Post], limit: Int): List[(String, Int)] = {
  posts
    .flatMap(post => TextProcessing.tokenize(s"${post._2} ${post._3}"))
    .map(_.trim)
    .filter(_.nonEmpty)
    .filter(word => word.headOption.exists(_.isUpper))
    .map(_.toLowerCase)
    .filterNot(Stopwords.contains)
    .groupBy(identity)
    .map { case (word, occurrences) => (word, occurrences.length) }
    .toList
    .sortBy { case (word, count) => (-count, word) }
    .take(limit)
}

def capitalizedWordsBySubreddit(posts: List[Post], limit: Int): Map[String, List[(String, Int)]] = {
  groupBySubreddit(posts).map { case (subreddit, subredditPosts) =>
    subreddit -> capitalizedWordsTop(subredditPosts, limit)
  }
}

def firstPostsSummary(posts: List[Post], limit: Int): List[(String, String, String)] = {
  posts.take(limit).map { post =>
    (post._2, post._4, post._6)
  }
}

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

def generateSubredditReport(subreddit: String, posts: List[Post]): String = {
  val report = new StringBuilder
  report.append(s"=== Subreddit: $subreddit ===\n")
  report.append(s"Total posts: ${posts.length}\n")

  if (posts.nonEmpty) {
    val avgScore = posts.map(_._5).sum.toDouble / posts.length
    val maxScore = posts.map(_._5).max
    report.append(f"Avg Score: $avgScore%.2f\n")
    report.append(s"Max Score: $maxScore\n")

    val topMentions = mentionsTop(posts, 3)
    report.append("\nTop User Mentions:\n")
    topMentions.foreach { case (user, count) =>
      report.append(s"  - $user: $count\n")
    }
  }

  report.toString()
}

def generateLab1Report(subreddit: String, posts: List[Post]): String = {
  val cleanPosts = filterRelevantPosts(posts)
  val totalScore = sumarScoresTotales(cleanPosts)
  val topWords = capitalizedWordsTop(cleanPosts, 10)
  val firstPosts = firstPostsSummary(cleanPosts, 5)
  val report = new StringBuilder

  report.append(s"## $subreddit\n")
  report.append(s"- Total score: $totalScore\n")
  report.append("- Palabras frecuentes:\n")
  topWords.foreach { case (word, count) =>
    report.append(s"  - $word: $count\n")
  }
  report.append("- Primeros posts:\n")
  firstPosts.foreach { case (title, date, url) =>
    report.append(s"  - $title | $date | $url\n")
  }

  report.toString()
}

def groupBySubreddit(posts: List[Post]): Map[String, List[Post]] = {
  posts.groupBy(_._1)
}
```

### `Main.scala`

```scala
val allPosts: List[Post] = subscriptions.flatMap { case (name, url, minScore) =>
  FileIO.downloadFeed(url) match {
    case Some(jsonString) =>
      RedditParser.parsePosts(jsonString) match {
        case Some(posts) =>
          val filteredPosts = posts.filter(post => post._5 >= minScore)
          println(s"  ✓ Parsed ${filteredPosts.length}/${posts.length} posts (score >= $minScore) from $name")
          filteredPosts

        case None =>
          println(s"  ✗ Failed to parse posts from $name")
          List.empty[Post]
      }

    case None =>
      println(s"  ✗ Failed to download feed from $name")
      List.empty[Post]
  }
}
```
