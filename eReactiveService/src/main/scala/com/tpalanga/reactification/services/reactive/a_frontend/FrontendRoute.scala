package com.tpalanga.reactification.services.reactive.a_frontend

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.tpalanga.reification.common.{KafkaCommon, KafkaProducer}
import com.typesafe.scalalogging.LazyLogging
import spray.json._

import scala.util.{Failure, Success}


class FrontendRoute(service: FrontendService, kafkaProducer: KafkaProducer) extends SprayJsonSupport with LazyLogging {

  import com.tpalanga.reification.common.Protocol.JsonFormats._
  import com.tpalanga.reification.common.Protocol._

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
              val userDetails: UserDetails = UserDetails("john.doe@gmail.com", "John Doe", "012345678") // retrieve from session

              entity(as[PaymentConfirmation]) { confirmation =>
                val transactionKey = confirmation.transactionKey
                val lease = LeaseId(confirmation.leaseId)

                import scala.concurrent.ExecutionContext.Implicits.global // TODO - use other EC
                val documentIdFuture = for {
                  _ <- service.savePaymentConfirmation(transactionKey, lease)
                  tickets <- service.getTickets(lease)
                  _ <- service.saveTicketData(tickets, userDetails, transactionKey)
                  msg = TicketOrder(tickets, userDetails, transactionKey).toJson.compactPrint
                  _ <- kafkaProducer.sendMessages(KafkaCommon.kafkaOrdersTopic, List(msg))
                  _ = logger.info("=== Message sent!")
                } yield ()

                onComplete(documentIdFuture) {
                  case Success(documentId) =>
                    complete("Order confirmed")
                  case Failure(th) =>
                    complete(StatusCodes.InternalServerError, s"Ticket download request failed")
                }
              }
            }
          }
        }
    }
}
