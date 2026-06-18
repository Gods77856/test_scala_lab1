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
    val formatter = DateTimeFormatter
      .ofPattern("yyyy-MM-dd HH:mm")
      .withZone(ZoneId.of("UTC"))
    formatter.format(Instant.ofEpochSecond(utcTimestamp))
  }

  /**
   * Tokeniza un texto en palabras (elimina puntuación).
   * Preserva menciones de usuarios (u/usuario) y subreddits (r/subreddit).
   *
   * @param text Texto a tokenizar
   * @return Lista de palabras
   */
  def tokenize(text: String): List[String] = {
    val tokenPattern = """(u/\w+|r/\w+|[\p{L}\p{N}_]+)""".r
    tokenPattern.findAllIn(text).toList
  }
}
