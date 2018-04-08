package com.tpalanga.reactification.a_monolith.route

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.tpalanga.reactification.a_monolith.service.MonolithService
import com.tpalanga.reactification.a_monolith.service.MonolithService.{TicketStock, TicketType, _}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.util.{Failure, Success}


object MonolithRoute {

  case class TicketRequest(ticketType: Int, quantity: Int)

  case class LeaseResponse(leaseId: String)

  case class DownloadResponse(downloadLink: String)

  case class LeaseError(msg: String)

  case class PaymentConfirmation(transactionKey: String, leaseId: String)


  object JsonFormats extends DefaultJsonProtocol {
    implicit val ticketTypeFormat: RootJsonFormat[TicketType] = jsonFormat1(TicketType)
    implicit val ticketStockFormat: RootJsonFormat[TicketStock] = jsonFormat1(TicketStock)
    implicit val ticketAvailabilitiesFormat: RootJsonFormat[TicketAvailabilities] = jsonFormat1(TicketAvailabilities)

    implicit val ticketRequestFormat: RootJsonFormat[TicketRequest] = jsonFormat2(TicketRequest)
    implicit val leaseResponseFormat: RootJsonFormat[LeaseResponse] = jsonFormat1(LeaseResponse)
    implicit val leaseErrorFormat: RootJsonFormat[LeaseError] = jsonFormat1(LeaseError)
    implicit val paymentConfirmationFormat: RootJsonFormat[PaymentConfirmation] = jsonFormat2(PaymentConfirmation)
    implicit val downloadResponseFormat: RootJsonFormat[DownloadResponse] = jsonFormat1(DownloadResponse)
  }

}

class MonolithRoute(service: MonolithService) extends SprayJsonSupport {

  import MonolithRoute.JsonFormats._
  import MonolithRoute._

  val route: Route =
    pathPrefix("events" / "tickets" / Segment) { eventId =>
      pathEnd {
        get {
          onComplete(service.getAvailableTickets(eventId)) {
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
                onComplete(service.bookTickets(TicketType(request.ticketType), request.quantity)) {
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
              val userDetails: UserDetails = ??? // retrieve from session

              entity(as[PaymentConfirmation]) { confirmation =>
                val transactionKey = confirmation.transactionKey
                val lease = LeaseId(confirmation.leaseId)

                import scala.concurrent.ExecutionContext.Implicits.global // TODO - use other EC
              val documentIdFuture = for {
                _ <- service.savePaymentConfirmation(transactionKey, lease)
                tickets <- service.getTickets(lease)
                _ <- service.saveTicketData(tickets, userDetails, transactionKey)
                documentId <- service.createPrintTicket(tickets, userDetails, transactionKey) // create pdf
              } yield documentId

                onComplete(documentIdFuture) {
                  case Success(documentId) =>
                    complete(DownloadResponse(s"/downloads/tickets/$documentId"))
                  case Failure(th) =>
                    complete(StatusCodes.InternalServerError, s"Ticket download request failed")
                }
              }
            }
          }
        }
    }
}
