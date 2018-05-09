package my.simplestore

import java.lang.reflect.InvocationTargetException

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.util.ByteString

import org.json4s._
import org.json4s.jackson.JsonMethods._

object JsonMarshalling extends JsonMarshalling

trait JsonMarshalling {

  /**
    * HTTP entity => JSON String
    *
    * @return unmarshaller for JSON String
    */
  val jsonStringUnmarshaller: FromEntityUnmarshaller[String] =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(`application/json`)
      .mapWithCharset {
        case (ByteString.empty, _) => throw Unmarshaller.NoContentException
        case (data, charset)       => data.decodeString(charset.nioCharset.name)
      }

  /**
    * HTTP entity => `JValue`
    *
    * @return unmarshaller for `JValue`
    */
  implicit val jsonValueUnmarshaller: FromEntityUnmarshaller[JValue] =
    jsonStringUnmarshaller
    .map(item => parse(item))

  /**
    * HTTP entity => `A`
    *
    * @tparam A type to decode
    * @return unmarshaller for `A`
    */
  def explicitJsonTypeUnmarshaller[A : Manifest](implicit formats: Formats): FromEntityUnmarshaller[A] =
    jsonValueUnmarshaller
      .map(Extraction.extract[A])
      .recover {
        _ => _ => {
          case MappingException(_, ite: InvocationTargetException) => throw ite.getCause
        }
      }

  /**
    * JSON-String => HTTP entity
    *
    * @return marshaller for JSON String
    */
  val jsonStringMarshaller: ToEntityMarshaller[String] = Marshaller.stringMarshaller(`application/json`)

  /**
    * `JValue` => HTTP entity
    *
    * @return marshaller for a `JValue`
    */
  implicit def jsonValueMarshaller(): ToEntityMarshaller[JValue] = {
    jsonStringMarshaller.compose(compactRenderOrNothing)
  }

  private def compactRenderOrNothing(json: JValue): String = json match {
    case JNothing => ""
    case _ => compact(render(json))
  }

  /**
    * `A` => HTTP entity
    *
    * @tparam A type to encode
    * @return marshaller for any `A` value
    */
  def explicitJsonTypeMarshaller[A <: AnyRef](): ToEntityMarshaller[A] = {
    implicit val formats = org.json4s.DefaultFormats
    jsonValueMarshaller.compose(Extraction.decompose)
  }

}
