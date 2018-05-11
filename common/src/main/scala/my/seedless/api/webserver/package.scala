package my.seedless.api

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import org.json4s._

package object webserver {

  case class All(list:JValue) extends ApiMessage with ApiResponse {
    def httpStatus: StatusCode = StatusCodes.OK
  }

  case class Item(result: Option[JValue]) extends ApiMessage with ApiResponse {
    def httpStatus: StatusCode = StatusCodes.OK
  }

  case class WriteResult(id:String) extends ApiMessage with ApiResponse {
     def httpStatus: StatusCode = StatusCodes.OK
  }

  case class DeleteResult(success:Boolean) extends ApiMessage with ApiResponse {
    def httpStatus: StatusCode = if (success)
      StatusCodes.OK
    else
      StatusCodes.InternalServerError
  }

  case class WriteRequest(item:JValue) extends ApiMessage with ApiRequest

}
