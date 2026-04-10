package reddit

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.DefaultFormats._
import scalaj.http.Http

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
   * EJERCICIO 1: Este tipo debe incluir el nuevo campo minScore.
   *
   * Estructura: (name, url, minScore)
   * Ejemplo: ("Scala", "https://www.reddit.com/r/scala/.json", 50)
   */
  type Subscription = (String, String, Int)

  /**
   * Lee el archivo JSON de suscripciones desde el path especificado.
   * 
   * EJERCICIO 1 - PASO 1: Implementar esta función
   * 
   * Pasos:
   * 1. Lee el contenido del archivo en `path` de forma segura (manejo Option)
   * 2. Parsea el JSON usando json4s
   * 3. Extrae la lista de mapas (Map[String, String])
   * 4. Transforma cada mapa en una Subscription:
   *    - "name": String
   *    - "url": String  
   *    - "minScore": String (convertir a Int, con valor por defecto 0 si no existe)
   * 5. Retorna Some(List[Subscription]) si tiene éxito, None si hay error
   *
   * Hint: Usa scala.io.Source para leer archivo y try/catch o match para errores.
   *       Scala permite try { ... } catch { case ... => ... }
   *       O directamente scala.io.Source.fromFile(path).getLines().mkString
   */
  def readSubscriptions(path: String): Option[List[Subscription]] = {
    // TODO: EJERCICIO 1 - PASO 1: Implementar lectura de JSON
    // 
    // Pasos recomendados:
    // 1. Lee contenido del archivo con scala.io.Source.fromFile(path).mkString
    // 2. Parsea con json4s: parse(contenido)
    // 3. Extrae lista de mapas: json.extractOpt[List[Map[String, String]]]
    // 4. Transforma cada mapa en tupla (name, url, minScore)
    //    - El minScore debe convertirse de String a Int
    //    - Si no existe minScore, usar default 0
    // 5. Retorna Some(listaDeSuscripciones) o None si hay error
    //
    // Referencia: Ver EJERCICIOS.md Ejercicio 1 - Paso 1
    
    None  // Reemplaza con tu implementación
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

  /**
   * Lee el archivo de suscripciones desde los recursos.
   */
  def loadSubscriptionsFromResources(): Option[List[Subscription]] = {
    try {
      val inputStream = getClass.getResourceAsStream("/subscriptions.json")
      if (inputStream == null) {
        None
      } else {
        val content = scala.io.Source.fromInputStream(inputStream).mkString
        inputStream.close()
        val json = parse(content)
        val items = json.children
        val subs = items.map { item =>
          val name = (item \ "name").extractOpt[String].getOrElse("")
          val url = (item \ "url").extractOpt[String].getOrElse("")
          val minScore = (item \ "minScore").extractOpt[Int]
            .orElse((item \ "minScore").extractOpt[String].map(_.toInt))
            .getOrElse(0)
          (name, url, minScore)
        }.toList
        Some(subs)
      }
    } catch {
      case _: Exception => None
    }
  }
}
