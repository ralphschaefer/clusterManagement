package my.seedless.simplestore.db

import scala.collection.concurrent
import scala.collection.JavaConverters._
import org.mapdb._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import my.seedless.api._
import my.seedless.simplestore.FromConfig

class Database private (collectionName:String,
                        val databaseEngine: DatabaseEngine = DatabaseEngine
                       ) extends FromConfig {

  implicit val formats = org.json4s.DefaultFormats

  protected val map:concurrent.Map[String,String] =
    databaseEngine.db.treeMap(collectionName,Serializer.STRING,Serializer.STRING).createOrOpen().asScala

  def write[A <: DbOrm](key:String, item: A) =
    map.update(key,compact(render(Extraction.decompose(item))))

  def read[A <: DbOrm](key:String)(implicit m: Manifest[A]):Option[A] =
    try map.get(key) map (item => parse(item.toString).extract[A])
    catch {
      case e:MappingException => None
    }

  def all[A <: DbOrm]()(implicit m: Manifest[A]):Map[String,A] = {
    (for ((k, v) <- map) yield
      try Some(k -> parse(v.toString).extract[A])
      catch {
        case e: MappingException => None
      }
      ).flatten.toMap
  }

  def delete(key:String):Boolean =
    map.remove(key).isDefined

}

object Database {

  private val databases : collection.mutable.HashMap[String, Database] = collection.mutable.HashMap()

  def apply(collectionName:String) = databases.get(collectionName) match {
      case Some(db) => db
      case _ =>
        val db = new Database(collectionName)
        databases += collectionName -> db
        db
    }

  def cleanAll() = {
    for ((k, v) <- databases) {
      databases -= k
    }
  }
}
