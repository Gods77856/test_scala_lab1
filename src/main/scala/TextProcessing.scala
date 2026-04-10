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
    val instant = Instant.ofEpochSecond(utcTimestamp)
    val formatter = DateTimeFormatter
      .ofPattern("yyyy-MM-dd HH:mm")
      .withZone(ZoneId.systemDefault())
    formatter.format(instant)
  }

  /**
   * Tokeniza un texto en palabras (elimina puntuación).
   * Preserva menciones de usuarios (u/usuario) y subreddits (r/subreddit).
   *
   * @param text Texto a tokenizar
   * @return Lista de palabras
   */
  def tokenize(text: String): List[String] = {
    // Primero extrae menciones de usuarios (u/...) y subreddits (r/...)
    val mentionPattern = """(u/\w+|r/\w+|\w+)""".r
    mentionPattern.findAllIn(text).toList.filter(_.nonEmpty)
  }
}
