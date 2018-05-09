package my.simplestore.db

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

case class Entity[P <:Payload](clazz:String,payload:P) extends Database.DbOrm {
  def this(payload: P) = this(payload.clazzName,payload)
}

case class Demo(value:String) extends Payload
