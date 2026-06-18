package reddit

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.DefaultFormats._
import scalaj.http.Http
import scala.io.Source
import scala.util.{Try, Using}

/**
 * FileIO.scala
 * 
 * Módulo para manejo de entrada/salida: lectura de JSON y descargas HTTP.
 * Define tipos inmutables para Subscription.
 */
object FileIO {

  implicit val formats: org.json4s.DefaultFormats.type = org.json4s.DefaultFormats

  /**
   * Tipo Subscription extendido con campo minScore.
   * Referencia de parcial: este tipo ya incluye el nuevo campo minScore.
   *
   * Estructura: (name, url, minScore)
   * Ejemplo: ("Scala", "https://www.reddit.com/r/scala/.json", 50)
   */
  type Subscription = (String, String, Int)

  private def minScoreFrom(item: JValue): Int = {
    (item \ "minScore").extractOpt[Int]
      .orElse((item \ "minScore").extractOpt[String].flatMap(raw => Try(raw.trim.toInt).toOption))
      .getOrElse(0)
  }

  private def subscriptionFrom(item: JValue): Option[Subscription] = {
    for {
      name <- (item \ "name").extractOpt[String].map(_.trim).filter(_.nonEmpty)
      url <- (item \ "url").extractOpt[String].map(_.trim).filter(_.nonEmpty)
    } yield (name, url, minScoreFrom(item))
  }

  private def parseSubscriptions(content: String): Option[List[Subscription]] = {
    Try(parse(content)).toOption.flatMap {
      case JArray(items) => Some(items.flatMap(subscriptionFrom))
      case _ => None
    }
  }

  /**
   * Lee el archivo JSON de suscripciones desde el path especificado.
   * 
   * EJERCICIO 1 - PASO 1: referencia de lectura JSON segura.
   * 
   * Pasos:
   * 1. Lee el contenido del archivo en `path` de forma segura (manejo Option)
   * 2. Parsea el JSON usando json4s
   * 3. Recorre cada objeto JSON del array raíz
   * 4. Transforma cada objeto en una Subscription:
   *    - "name": String
   *    - "url": String  
   *    - "minScore": Int o String (convertir seguro, default 0 si no existe)
   * 5. Retorna Some(List[Subscription]) si tiene éxito, None si hay error
   */
  def readSubscriptions(path: String): Option[List[Subscription]] = {
    // @JSON_PARSE
    Using(Source.fromFile(path))(_.mkString).toOption.flatMap(parseSubscriptions)
  }

  /**
   * Descarga el contenido JSON de una URL.
   * 
   * Ya implementado para referencia.
   * 
   * @param url URL de la API de Reddit
   * @return Some(String) si tiene éxito, None si falla
   */
  def downloadFeed(url: String): Option[String] = {
    // @API_FETCH
    Try {
      Http(url)
        .header("User-Agent", "RedditScalaLab/1.0")
        .timeout(connTimeoutMs = 5000, readTimeoutMs = 10000)
        .asString
    }.toOption.filter(_.isSuccess).map(_.body)
  }

  /**
   * Lee el archivo de suscripciones desde los recursos.
   */
  def loadSubscriptionsFromResources(): Option[List[Subscription]] = {
    Option(getClass.getResourceAsStream("/subscriptions.json")).flatMap { stream =>
      Using(Source.fromInputStream(stream, "UTF-8"))(_.mkString).toOption.flatMap(parseSubscriptions)
    }
  }
}
