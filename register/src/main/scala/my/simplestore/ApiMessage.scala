package my.simplestore

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import org.json4s._
import JsonMarshalling._
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller

trait ApiMessage

object ApiMessage {

  implicit def jsonTypeMarshaller[A <: ApiMessage](implicit formats: Formats): ToEntityMarshaller[A] =
    explicitJsonTypeMarshaller[A]

  implicit def jsonTypeUnmarshaller[A <: ApiMessage : Manifest](implicit formats: Formats): FromEntityUnmarshaller[A] =
    explicitJsonTypeUnmarshaller[A]

}
