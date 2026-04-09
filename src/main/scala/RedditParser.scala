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

  implicit val formats = org.json4s.DefaultFormats

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
   * EJERCICIO 3 - Tolerancia a Fallos: Implementar esta función
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
   * 3. Para cada children, usa un for-comprehension o flatMap para:
   *    - Extraer subreddit (requerido)
   *    - Extraer title CON tolerancia a fallos (EJERCICIO 3):
   *      Si title no existe, usar "Sin Título"
   *    - Extraer selftext (requerido)
   *    - Extraer created_utc como Double y convertir a date con TextProcessing.formatDateFromUTC
   *    - Extraer score como Int (requerido)
   *    - Extraer url (requerido)
   *
   * Hint: Para tolerancia a fallos, extrae title FUERA del for-comprehension:
   *       val extractedTitle = (data \ "title").extractOpt[String].getOrElse("Sin Título")
   *       Luego úsalo en el yield.
   *
   * @param jsonString JSON de Reddit como string
   * @return Some(List[Post]) si parsing tiene éxito, None si falla
   */
  def parsePosts(jsonString: String): Option[List[Post]] = {
    try {
      val json = parse(jsonString)
      
      // Extrae los children del JSON
      val children = (json \ "data" \ "children").children
      
      // TODO: Implementar el flatMap que transforma children en Posts
      // Recuerda: aplicar tolerancia a fallos para "title"
      
      // TODO: EJERCICIO 3 - Implementar parseo con tolerancia a fallos
      //
      // Pasos recomendados:
      // 1. Usa flatMap sobre children para transformar cada child en Option[Post]
      // 2. Extrae title FUERA del for-comprehension con .getOrElse("Sin Título")
      //    (esto es clave para tolerancia a fallos - EJERCICIO 3)
      // 3. Usa for-comprehension para extraer campos requeridos:
      //    - subreddit, selftext, created_utc, score, url
      // 4. En el yield, construye la tupla Post con:
      //    (subreddit, title_con_fallback, selftext, date_formateado, score, url)
      // 5. Retorna Some(lista_de_posts)
      //
      // Referencia: Ver EJERCICIOS.md Ejercicio 3
      // Hint: TextProcessing.formatDateFromUTC(utcTimestamp.toLong) formatea la fecha
      
      Some(List.empty[Post])  // Reemplaza con tu implementación
      
    } catch {
      case _: Exception => None
    }
  }
}
