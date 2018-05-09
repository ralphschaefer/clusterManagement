package my.simplestore.db


// import java.util.concurrent.ConcurrentMap

import scala.collection.concurrent
import scala.collection.JavaConverters._
import my.simplestore.FromConfig
import org.mapdb._
import org.json4s._
import org.json4s.jackson.JsonMethods._

class Database private (collectionName:String) extends FromConfig {

  import Database._

  implicit val formats = org.json4s.DefaultFormats

  lazy val file = settings.getString("file")+"_"+collectionName

  protected lazy val db = DBMaker.fileDB(file).make()

  protected val map:concurrent.Map[String,String] =
    db.treeMap(collectionName,Serializer.STRING,Serializer.STRING).createOrOpen().asScala

  private def close() = db.close()

  def write[A <: DbOrm](key:String, item: A) =
    map.update(key,compact(render(Extraction.decompose(item))))

  def read[A <: DbOrm](key:String)(implicit m: Manifest[A]):Option[A] =
    try map.get(key) map (item => parse(item.toString).extract[A])
    catch {
      case e:MappingException => None
    }

  def all[A <: DbOrm]()(implicit m: Manifest[A]):Map[String,A] =
    (for ((k,v) <- map) yield
      try Some(k -> parse(v.toString).extract[A])
      catch {
        case e:MappingException => None
      }
    ).flatten.toMap

  def delete(key:String):Boolean =
    map.remove(key).isDefined

}

object Database {

  private val databases : collection.mutable.HashMap[String, Database] = collection.mutable.HashMap()

  trait DbOrm {
    val clazz:String
  }

  def apply(collectionName:String) = databases.get(collectionName) match {
      case Some(db) => db
      case _ =>
        val db = new Database(collectionName)
        databases += collectionName -> db
        db
    }

  def cleanAll = {
    for ((k, v) <- databases) {
      v.close()
      databases -= k
    }
  }
}
