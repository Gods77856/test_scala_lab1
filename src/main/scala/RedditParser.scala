package reddit

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.DefaultFormats._

/**
 * RedditParser.scala
 *
 * Responsable de parsear JSON de Reddit y extraer posts.
 * Define el tipo Post como tupla inmutable.
 */
object RedditParser {

  implicit val formats: org.json4s.DefaultFormats.type = org.json4s.DefaultFormats

  // ===== TIPOS INMUTABLES =====
  /**
   * Tipo Post basado en estructura de Reddit.
   * 
   * Posiciones (acceso con _1, _2, etc.):
   * _1: subreddit (String) - ej: "scala"
   * _2: title (String) - ej: "Ask r/scala: Monads explained"
   * _3: selftext (String) - ej: "¿Alguien puede explicar..."
   * _4: date (String) - ej: "2025-03-15 14:30"
   * _5: score (Int) - ej: 127
   * _6: url (String) - ej: "https://reddit.com/r/..."
  */
  type Post = (String, String, String, String, Int, String)

  /**
   * Parsea un string JSON de Reddit y extrae los posts.
   *
   * Referencia de parcial - Tolerancia a fallos:
   * esta función conserva posts sin title usando "Sin Título".
   * Para la consigna original del PDF, usar parsePostsStrictTitle.
   *
   * El JSON de Reddit tiene estructura:
   * {
   *   "data": {
   *     "children": [
   *       {
   *         "data": {
   *           "subreddit": "scala",
   *           "title": "...",
   *           "selftext": "...",
   *           "created_utc": 1234567890,
   *           "score": 50,
   *           "url": "https://..."
   *         }
   *       }
   *     ]
   *   }
   * }
   *
   * Pasos:
   * 1. Parsea el JSON string con json4s
   * 2. Extrae el array "data" -> "children"
   * 3. Para cada child, usa un for-comprehension o flatMap para:
   *    - Extraer subreddit (requerido)
   *    - Extraer title CON tolerancia a fallos (EJERCICIO 3):
   *      Si title no existe, usar "Sin Título"
   *    - Extraer selftext (requerido)
   *    - Extraer created_utc como Double y convertir a date con TextProcessing.formatDateFromUTC
   *    - Extraer score como Int (requerido)
   *    - Extraer url (requerido)
   *
   * @param jsonString JSON de Reddit como string
   * @return Some(List[Post]) si parsing tiene éxito, None si falla
   */
  def parsePosts(jsonString: String): Option[List[Post]] = {
    // @TOLERANCIA_FALLOS
    // TODO: Parsear JSON de Reddit conservando posts sin title con "Sin Título".
    // Pista: extraer title fuera del for-comprehension con getOrElse.
    None
  }

  /**
   * Lab 1 original - Ejercicio 3: si falta title, el post se descarta.
   */
  def parsePostsStrictTitle(jsonString: String): Option[List[Post]] = {
    // @STRICT_FIELDS
    // TODO: Parsear JSON descartando posts sin title.
    // Pista: title debe ir dentro del for-comprehension como campo obligatorio.
    None
  }
}
