# Parcial evaluado 2026: análisis y solución adaptable

Esta guía corresponde a `parcial_lab_1.pdf`, no al laboratorio original. El código probado está en `src/main/scala/Parcial2026.scala`; buscar `@PARCIAL_2026_SUBSCRIPTIONS`, `@URL_BUILDER`, `@PARCIAL_2026_POSTS`, `@KEYWORD_MATCH` y `@CUSTOM_REPORT`.

## Veredicto honesto

Antes de esta ampliación el repositorio cubría aproximadamente un 70 % del parcial: explicaba `Option`, JSON, HTTP y pipelines, pero sus ejemplos correspondían principalmente al Lab 1 original (`minScore`, fechas, score y menciones). No tenía una solución exacta y probada para `count`, `before`, `author` y palabras censuradas.

Ahora están cubiertos los tres ejercicios y sus casos de borde. Ningún repositorio garantiza el 100 % de un recuperatorio desconocido, pero sí quedan listas las piezas conceptuales y copiables para variantes razonables del mismo laboratorio.

## Mapa exacto del parcial

| Punto | Qué evalúa | Solución | Detalle esencial |
|---|---|---|---|
| 1.a | Parsear `name`, `url`, `count`, `before` | `parseSubscriptions` | Son requeridos: si alguno falta, el resultado es `None`. |
| 1.b | Pureza y recursos | `parseSubscriptions` + `readFile` | Parsear es puro. Leer es un efecto aislado y `Using` garantiza `close`. |
| 1.b | Construir URL | `buildUrl` | Debe haber un solo `?`; no duplicar `count` ni `before`. |
| 2.a | Imperativo a funcional | `parsePosts`, `downloadAll` | `flatMap`, `for` y `Option`; sin `var`. |
| 2.b | URL inexistente/403 | `fetchFeed` | `User-Agent`, estado HTTP y excepción se convierten a `None`. |
| 3.a | Nuevo tipo | `case class Post` | Exactamente `title`, `selftext`, `author`. |
| 3.b | Conteo | `countCensoredWords` | Título + cuerpo, tokens exactos, case-insensitive. |
| 3.c | Informe | `renderPost` | El render es puro; `println` queda en el borde. |

## Correcciones a las recomendaciones de Gemini

1. `getOrElse(10)` y `getOrElse("")` no respetan la política mostrada: el PDF pide manejar campos faltantes y la captura imprime `Could not load subscriptions`. La solución principal debe ser estricta; usar defaults sólo si el nuevo enunciado lo exige.
2. Concatenar `s"$url?count=..."` falla si la URL ya tiene parámetros. En la captura aparece `?count=10?count=10...`. `buildUrl` reemplaza `count` y `before` sin duplicarlos.
3. `try/catch` no vuelve pura una operación de I/O. La defensa correcta es **núcleo funcional, borde imperativo**: parsear un `String` es puro; abrir archivos o hacer HTTP es un efecto aislado.
4. `Source.fromURL` puede seguir dando 403. Hay que enviar `User-Agent` y convertir estados HTTP fallidos en `None`.
5. `split(" ")` no cuenta `AI,`. La expresión regular separa puntuación y compara tokens completos: `email` no cuenta como `AI`.
6. `take(50)` altera la salida y no figura en la consigna; imprimir el cuerpo completo salvo indicación contraria.

## Política de errores

```scala
// Toda la configuración es válida o no se carga.
def sequence[A](xs: List[Option[A]]): Option[List[A]] =
  xs.foldRight(Option(List.empty[A])) { (x, acc) =>
    for { value <- x; rest <- acc } yield value :: rest
  }

// Un post defectuoso se descarta, los válidos continúan.
val posts: List[Post] = children.flatMap(postFrom)

// Una URL fallida se omite, las demás continúan.
val downloaded = subscriptions.flatMap(fetchSubscription)
```

Si el recuperatorio dice “descartar sólo la suscripción incompleta”, cambiar `sequence(items.map(...))` por `Some(items.flatMap(...))`. Es una decisión semántica.

## Plantilla para un único `Main.scala`

El examen sólo permitía entregar `Main.scala`. Conservar el `package` y los imports ya presentes en el esqueleto. La versión completa y compilable de estas funciones está en `Parcial2026.scala`.

```scala
import java.net.URI
import java.util.Locale
import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.io.Source
import scala.util.{Try, Using}
import scalaj.http.Http

implicit val formats: DefaultFormats.type = DefaultFormats

case class Subscription(name: String, url: String, count: Int, before: String)
case class Post(title: String, selftext: String, author: String)

val censored = Set("llm", "llms", "ai", "chatgpt", "copilot",
  "claude", "ml", "gemini", "agent", "agentic")

def requiredString(json: JValue, field: String): Option[String] =
  (json \ field).extractOpt[String].map(_.trim).filter(_.nonEmpty)

def subscriptionFrom(json: JValue): Option[Subscription] =
  for {
    name <- requiredString(json, "name")
    url <- requiredString(json, "url")
    count <- (json \ "count").extractOpt[Int].filter(_ >= 0)
    before <- requiredString(json, "before")
  } yield Subscription(name, url, count, before)

def sequence[A](xs: List[Option[A]]): Option[List[A]] =
  xs.foldRight(Option(List.empty[A])) { (x, acc) =>
    for { value <- x; rest <- acc } yield value :: rest
  }

def parseSubscriptions(text: String): Option[List[Subscription]] =
  Try(parse(text)).toOption.flatMap {
    case JArray(items) => sequence(items.map(subscriptionFrom))
    case _ => None
  }

def buildUrl(sub: Subscription): Option[String] = Try {
  val uri = new URI(sub.url)
  val old = Option(uri.getRawQuery).toList.flatMap(_.split("&").toList)
    .filter(_.nonEmpty).filterNot { p =>
      val key = p.takeWhile(_ != '=').toLowerCase(Locale.ROOT)
      key == "count" || key == "before"
    }
  val query = (old ++ List(s"count=${sub.count}", s"before=${sub.before}")).mkString("&")
  new URI(uri.getScheme, uri.getRawAuthority, uri.getRawPath, query, uri.getRawFragment).toASCIIString
}.toOption

def parsePosts(text: String): Option[List[Post]] = Try {
  (parse(text) \ "data" \ "children").children.flatMap { child =>
    val data = child \ "data"
    for {
      title <- requiredString(data, "title")
      selftext <- (data \ "selftext").extractOpt[String]
      author <- requiredString(data, "author")
    } yield Post(title, selftext, author)
  }
}.toOption

def fetchFeed(url: String): Option[String] = Try {
  Http(url).header("User-Agent", "Mozilla/5.0 ScalaLab/1.0")
    .timeout(connTimeoutMs = 5000, readTimeoutMs = 10000).asString
}.toOption.filter(_.isSuccess).map(_.body)

def countCensored(post: Post): Int =
  """[\p{L}\p{N}_]+""".r.findAllIn(s"${post.title} ${post.selftext}")
    .map(_.toLowerCase(Locale.ROOT)).count(censored.contains)

def render(post: Post): String =
  s"""${post.title} by **${post.author}**
     |Contenido: ${post.selftext}
     |Palabras censuradas: ${countCensored(post)}
     |-------------------------""".stripMargin

// Borde imperativo: adaptar el path al esqueleto.
val subscriptions = Using(Source.fromFile("subscriptions.json", "UTF-8"))(_.mkString)
  .toOption.flatMap(parseSubscriptions).getOrElse(List.empty)

subscriptions.flatMap { sub =>
  for {
    url <- buildUrl(sub)
    json <- fetchFeed(url)
    posts <- parsePosts(json)
  } yield (url, posts)
}.foreach { case (url, posts) =>
  println(s"Posts from: $url")
  posts.foreach(post => println(render(post)))
}
```

## Qué estudiar, además de copiar

- `map` frente a `flatMap`; un `for` sobre `Option` corta en el primer `None`.
- Error de un elemento frente a error de toda la colección: `flatMap` frente a `sequence`.
- `Option` modela el fallo, pero no elimina los efectos de I/O.
- `Using` para todo `Source`, tanto en éxito como en excepción.
- Tokenizar, normalizar case y consultar un `Set`.
- Probar JSON inválido, campo faltante, URL con query previa, 403/404, puntuación, mayúsculas y listas vacías.

## Checklist de cinco minutos

- [ ] El tipo `Subscription` coincide con la nueva consigna.
- [ ] Los campos obligatorios están dentro del `for`.
- [ ] La URL contiene un único `?`, un `count` y un `before`.
- [ ] No hay `Source` abierto sin `Using` o `finally`.
- [ ] La descarga envía `User-Agent` y devuelve `None` en error.
- [ ] `Post` coincide con el JSON y la salida pedida.
- [ ] El conteo une título y cuerpo y compara tokens completos sin case.
- [ ] No hay `var`; se usan funciones de alto orden.
- [ ] `sbt test` termina en verde.
