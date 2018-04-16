package com.tpalanga.reactification.services.rest.b_backend

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success}

class BackendRoute(service: BackendService) extends SprayJsonSupport with LazyLogging {

  import com.tpalanga.reification.common.Protocol.JsonFormats._
  import com.tpalanga.reification.common.Protocol._

  val route: Route =
    pathPrefix("ticketorder") {
      pathEnd {
        post {
          entity(as[TicketOrder]) { ticketOrder =>

            onComplete(service.createPrintTicket(ticketOrder)) {
              case Success(documentId) =>
                complete(documentId)
              case Failure(th) =>
                complete(StatusCodes.InternalServerError, s"Ticket download request failed")
            }
          }
        }
      }
    }
}
