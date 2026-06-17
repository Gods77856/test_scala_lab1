package reddit

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.DefaultFormats._

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
    // TODO: Implementar lectura segura de JSON.
    // Pista: la solución completa está en GUT.md y GUIA_PARCIAL_LAB1.md.
    None
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
    // TODO: Descargar el contenido de la URL y devolver Some(body) o None.
    // Pista: encapsular excepciones y fallas HTTP en Option.
    None
  }

  /**
   * Lee el archivo de suscripciones desde los recursos.
   */
  def loadSubscriptionsFromResources(): Option[List[Subscription]] = {
    // TODO: Leer /subscriptions.json desde resources.
    // Pista: puede resolverse reutilizando el mismo patrón de readSubscriptions.
    None
  }
}
