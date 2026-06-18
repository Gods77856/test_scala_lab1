package reddit

import java.net.URI
import java.util.Locale
import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.io.Source
import scala.util.{Try, Using}
import scalaj.http.Http

/** Solución de referencia, modular y copiable, del parcial evaluado de 2026. */
object Parcial2026 {

  implicit val formats: DefaultFormats.type = DefaultFormats

  final case class Subscription(name: String, url: String, count: Int, before: String)
  final case class Post(title: String, selftext: String, author: String)

  val CensoredWords: Set[String] = Set(
    "llm", "llms", "ai", "chatgpt", "copilot", "claude", "ml", "gemini", "agent", "agentic"
  )

  private val WordPattern = """[\p{L}\p{N}_]+""".r

  /** @PARCIAL_2026_SUBSCRIPTIONS: transformación pura String JSON -> dominio. */
  def parseSubscriptions(jsonString: String): Option[List[Subscription]] = {
    Try(parse(jsonString)).toOption.flatMap {
      case JArray(items) => sequence(items.map(subscriptionFrom))
      case _ => None
    }
  }

  private def subscriptionFrom(item: JValue): Option[Subscription] = {
    for {
      name <- requiredString(item, "name")
      url <- requiredString(item, "url")
      count <- (item \ "count").extractOpt[Int].filter(_ >= 0)
      before <- requiredString(item, "before")
    } yield Subscription(name, url, count, before)
  }

  private def requiredString(item: JValue, field: String): Option[String] = {
    (item \ field).extractOpt[String].map(_.trim).filter(_.nonEmpty)
  }

  private def sequence[A](values: List[Option[A]]): Option[List[A]] = {
    values.foldRight(Option(List.empty[A])) { (current, accumulated) =>
      for {
        value <- current
        rest <- accumulated
      } yield value :: rest
    }
  }

  /**
   * @URL_BUILDER: agrega count y before una sola vez, preservando otros parámetros.
   * Devuelve None ante una URL inválida en vez de lanzar una excepción.
   */
  def buildUrl(subscription: Subscription): Option[String] = Try {
    val uri = new URI(subscription.url)
    val previousParams = Option(uri.getRawQuery)
      .toList
      .flatMap(_.split("&").toList)
      .filter(_.nonEmpty)
      .filterNot { parameter =>
        val key = parameter.takeWhile(_ != '=').toLowerCase(Locale.ROOT)
        key == "count" || key == "before"
      }
    val query = (previousParams ++ List(
      s"count=${subscription.count}",
      s"before=${subscription.before}"
    )).mkString("&")

    new URI(uri.getScheme, uri.getRawAuthority, uri.getRawPath, query, uri.getRawFragment).toASCIIString
  }.toOption

  /** @PARCIAL_2026_POSTS: transformación funcional del array children. */
  def parsePosts(jsonString: String): Option[List[Post]] = Try {
    val children = (parse(jsonString) \ "data" \ "children").children
    children.flatMap { child =>
      val data = child \ "data"
      for {
        title <- requiredString(data, "title")
        selftext <- (data \ "selftext").extractOpt[String]
        author <- requiredString(data, "author")
      } yield Post(title, selftext, author)
    }
  }.toOption

  /** @KEYWORD_MATCH: cuenta ocurrencias exactas, no substrings, sin distinguir mayúsculas. */
  def countCensoredWords(post: Post): Int = {
    WordPattern
      .findAllIn(s"${post.title} ${post.selftext}")
      .map(_.toLowerCase(Locale.ROOT))
      .count(CensoredWords.contains)
  }

  /** @CUSTOM_REPORT: genera texto; imprimirlo queda en la capa externa. */
  def renderPost(post: Post): String = {
    s"""${post.title} by **${post.author}**
       |Contenido: ${post.selftext}
       |Palabras censuradas: ${countCensoredWords(post)}
       |-------------------------""".stripMargin
  }

  /** Capa de efectos: cierra el archivo incluso si mkString falla. */
  def readFile(path: String): Option[String] = {
    Using(Source.fromFile(path, "UTF-8"))(_.mkString).toOption
  }

  /** Capa de efectos: User-Agent evita el 403 del Source.fromURL del esqueleto. */
  def fetchFeed(url: String): Option[String] = {
    Try {
      Http(url)
        .header("User-Agent", "Mozilla/5.0 ScalaLab/1.0")
        .timeout(connTimeoutMs = 5000, readTimeoutMs = 10000)
        .asString
    }.toOption.filter(_.isSuccess).map(_.body)
  }

  /** Pipeline funcional: las suscripciones que fallen no detienen las restantes. */
  def downloadAll(
      subscriptions: List[Subscription],
      fetch: String => Option[String]
  ): List[(String, List[Post])] = {
    subscriptions.flatMap { subscription =>
      for {
        url <- buildUrl(subscription)
        json <- fetch(url)
        posts <- parsePosts(json)
      } yield (url, posts)
    }
  }
}
