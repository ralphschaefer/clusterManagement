package my.seedless.simplestore.db

import my.seedless.simplestore.FromConfig
import org.mapdb._

object DatabaseEngine extends DatabaseEngine

class DatabaseEngine extends FromConfig  {
  lazy val file = settings.getString("file")
  lazy val db = DBMaker.fileDB(file).make()

  def close() = db.close()
}
