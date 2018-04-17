package com.tpalanga.reification.actors

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, complete, entity, get, onComplete, pathEnd, pathPrefix, post, _}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout

import scala.util.{Failure, Success}

class ActorsRoute(service: ActorRef) extends SprayJsonSupport {

  import ActorsService._
  import com.tpalanga.reification.common.Protocol.JsonFormats._
  import com.tpalanga.reification.common.Protocol._
  import scala.concurrent.duration._

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

                onComplete((service ? ConfirmPayment(transactionKey, lease, userDetails)).mapTo[DocumentId]) {
                  case Success(documentId) =>
                    complete(DownloadResponse(s"/downloads/${documentId.filePath}"))
                  case Failure(th) =>
                    complete(StatusCodes.InternalServerError, s"Ticket download request failed")
                }
              }
            }
          }
        }
    }
}
