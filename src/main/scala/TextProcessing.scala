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
   *
   * @param text Texto a tokenizar
   * @return Lista de palabras
   */
  def tokenize(text: String): List[String] = {
    text.split("\\W+").filter(_.nonEmpty).toList
  }
}
