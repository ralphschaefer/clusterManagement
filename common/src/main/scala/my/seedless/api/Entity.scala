package my.seedless.api

case class Entity[P <:Payload](clazz:String,payload:P) extends DbOrm {
  def this(payload: P) = this(payload.clazzName,payload)
}