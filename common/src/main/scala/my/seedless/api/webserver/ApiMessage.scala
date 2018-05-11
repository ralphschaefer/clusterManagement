package my.seedless.api.webserver

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, MediaTypes, StatusCode}
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.http.scaladsl.marshalling.{Marshaller, ToResponseMarshaller}
import org.json4s._
import org.json4s.jackson.JsonMethods._

trait ApiMessage

trait ApiResponse extends ApiMessage{
  def httpStatus:StatusCode
}

trait ApiRequest extends ApiMessage

object ApiMessage {

  implicit def apiMessageToResponse: ToResponseMarshaller[ApiResponse] = Marshaller.opaque{
    item =>
      implicit val formats = org.json4s.DefaultFormats
      HttpResponse(
        status = item.httpStatus,
        headers = List.empty,
        entity = HttpEntity(
          contentType = MediaTypes.`application/json`,
          string = compact(render(Extraction.decompose(item)))
        )
      )
  }


  implicit def requestToApiMessage[A <: ApiRequest : Manifest]: FromEntityUnmarshaller[A] = {
    implicit val formats = org.json4s.DefaultFormats
    Unmarshaller
      .byteStringUnmarshaller
      .forContentTypes(MediaTypes.`application/json`)
      .mapWithCharset {
        case (data, charset) => data.decodeString(charset.nioCharset.name)
      }
      .map(i => parse(i).extract[A])
  }

}