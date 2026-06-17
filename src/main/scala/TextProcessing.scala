package reddit

import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter

/**
 * TextProcessing.scala
 *
 * Utilidades para procesamiento de texto y conversión de fechas.
 */
object TextProcessing {

  /**
   * Convierte timestamp UTC a string formateado.
   *
   * @param utcTimestamp Timestamp en segundos desde epoch
   * @return String con formato "YYYY-MM-DD HH:mm"
   */
  def formatDateFromUTC(utcTimestamp: Long): String = {
    // @FECHAS_UTC
    // TODO: Convertir timestamp UNIX a fecha UTC "yyyy-MM-dd HH:mm".
    ""
  }

  /**
   * Tokeniza un texto en palabras (elimina puntuación).
   * Preserva menciones de usuarios (u/usuario) y subreddits (r/subreddit).
   *
   * @param text Texto a tokenizar
   * @return Lista de palabras
   */
  def tokenize(text: String): List[String] = {
    // TODO: Dividir texto en palabras preservando menciones u/... y r/...
    List.empty[String]
  }
}
