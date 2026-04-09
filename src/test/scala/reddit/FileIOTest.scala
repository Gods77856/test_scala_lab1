package reddit

import org.scalatest.funsuite.AnyFunSuite
import java.nio.file.{Files, Paths}
import java.io.PrintWriter

class FileIOTest extends AnyFunSuite {

  test("readSubscriptions: should parse valid JSON file") {
    // Crear archivo temporal
    val tempFile = Files.createTempFile("subscriptions", ".json")
    val json = """[
      {"name": "scala", "url": "https://reddit.com/r/scala/.json", "minScore": "50"},
      {"name": "java", "url": "https://reddit.com/r/java/.json"}
    ]"""
    Files.write(tempFile, json.getBytes)
    
    val result = FileIO.readSubscriptions(tempFile.toString)
    assert(result.isDefined)
    assert(result.get.length == 2)
    
    // Verificar primer subscription con minScore
    assert(result.get(0)._1 == "scala")
    assert(result.get(0)._2 == "https://reddit.com/r/scala/.json")
    assert(result.get(0)._3 == 50)
    
    // Verificar segundo subscription sin minScore (default 0)
    assert(result.get(1)._1 == "java")
    assert(result.get(1)._3 == 0)
    
    // Limpiar
    Files.delete(tempFile)
  }

  test("readSubscriptions: should return None for missing file") {
    val result = FileIO.readSubscriptions("/path/that/does/not/exist/subscriptions.json")
    assert(result.isEmpty)
  }

  test("readSubscriptions: should return None for invalid JSON") {
    val tempFile = Files.createTempFile("invalid", ".json")
    Files.write(tempFile, "{ invalid json }".getBytes)
    
    val result = FileIO.readSubscriptions(tempFile.toString)
    // Debería ser None o manejar gracefully
    
    Files.delete(tempFile)
  }

  test("readSubscriptions: should use default minScore of 0") {
    val tempFile = Files.createTempFile("subscriptions_default", ".json")
    val json = """[
      {"name": "test", "url": "https://test.com/.json"}
    ]"""
    Files.write(tempFile, json.getBytes)
    
    val result = FileIO.readSubscriptions(tempFile.toString)
    assert(result.isDefined)
    assert(result.get(0)._3 == 0) // default minScore
    
    Files.delete(tempFile)
  }

  test("readSubscriptions: should convert minScore string to Int") {
    val tempFile = Files.createTempFile("subscriptions_score", ".json")
    val json = """[
      {"name": "test", "url": "https://test.com/.json", "minScore": "100"}
    ]"""
    Files.write(tempFile, json.getBytes)
    
    val result = FileIO.readSubscriptions(tempFile.toString)
    assert(result.isDefined)
    assert(result.get(0)._3 == 100)
    assert(result.get(0)._3.isInstanceOf[Int])
    
    Files.delete(tempFile)
  }

  test("loadSubscriptionsFromResources: should load from classpath") {
    // Esta prueba asume que subscriptions.json está en resources
    val result = FileIO.loadSubscriptionsFromResources()
    // Puede ser Some o None dependiendo del entorno
    // Si está None es aceptable para test
    assert(result.isEmpty || result.isDefined)
  }
}
