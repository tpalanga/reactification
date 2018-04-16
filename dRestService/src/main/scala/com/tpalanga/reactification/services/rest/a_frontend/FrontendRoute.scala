package com.tpalanga.reactification.services.rest.a_frontend

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.LazyLogging
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.util.{Failure, Success}


class FrontendRoute(service: ActorRef) extends SprayJsonSupport with LazyLogging {

  import FrontendService._
  import com.tpalanga.reification.common.Protocol.JsonFormats._
  import com.tpalanga.reification.common.Protocol._

  implicit val askTimeout = Timeout(3.seconds)

  val route: Route =
    pathPrefix("events" / "tickets" / Segment) { eventId =>
      pathEnd {
        get {
          onComplete((service ? GetAvailableTickets(eventId)).mapTo[TicketAvailabilities]) {
            case Success(availabilities: TicketAvailabilities) =>
              complete(availabilities)
            case Failure(th) =>
              complete(StatusCodes.InternalServerError, s"Getting ticket availabilities for event $eventId failed")
          }
        }
      } ~
        pathPrefix("book") {
          pathEnd {
            post {
              entity(as[TicketRequest]) { request =>
                onComplete((service ? BookTickets(TicketType(request.ticketType), request.quantity)).mapTo[LeaseConfirmation]) {
                  case Success(Accepted(leaseId)) =>
                    complete(LeaseResponse(leaseId.value))
                  case Success(Denied(msg)) =>
                    complete(StatusCodes.NotFound, msg)
                  case Failure(th) =>
                    complete(StatusCodes.InternalServerError, s"Ticket booking request failed")
                }
              }
            }
          }
        } ~
        pathPrefix("paymentDone") {
          pathEnd {
            post {
              val userDetails: UserDetails = UserDetails("john.doe@gmail.com", "John Doe", "012345678") // retrieve from session

              entity(as[PaymentConfirmation]) { confirmation =>
                val transactionKey = confirmation.transactionKey
                val lease = LeaseId(confirmation.leaseId)

                onComplete((service ? ConfirmPayment(transactionKey, lease, userDetails)).mapTo[Either[String, DocumentId]]) {
                  case Success(Right(documentId)) =>
                    complete(DownloadResponse(s"/downloads/${documentId.filePath}"))
                  case Success(Left(error)) =>
                    complete(StatusCodes.InternalServerError, s"Ticket download request failed: $error")
                  case Failure(th) =>
                    logger.error("Ticket download request failed", th)
                    complete(StatusCodes.InternalServerError, "Ticket download request failed")
                }
              }
            }
          }
        }
    }
}
