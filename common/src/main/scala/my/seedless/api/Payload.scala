package my.seedless.api

import org.json4s._
import org.json4s.jackson.JsonMethods._
import java.security.MessageDigest

trait Payload {
  implicit val formats = org.json4s.DefaultFormats
  def clazzName = {
    val c = this.getClass.getName
    if ((c takeRight 1) == "$") c dropRight 1 else c
  }
  def toJson = Extraction.decompose(this)
  def asEntity = new Entity(this)
  def hash = MessageDigest.getInstance("MD5").digest(this.toString.getBytes).map("%02X" format _).mkString
}

