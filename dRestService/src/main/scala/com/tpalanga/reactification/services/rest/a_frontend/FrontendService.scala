package com.tpalanga.reactification.services.rest.a_frontend

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import com.tpalanga.reification.common.Protocol.{LeaseId, TicketType, UserDetails}
import akka.pattern.pipe
import com.softwaremill.sttp.json4s.asJson
import com.softwaremill.sttp._
import com.softwaremill.sttp.akkahttp._
import com.softwaremill.sttp.json4s._


import scala.concurrent.Future

object FrontendService {

  sealed trait ActorsServiceCommand

  case class GetAvailableTickets(eventId: String) extends ActorsServiceCommand

  case class BookTickets(ticketType: TicketType, quantity: Int) extends ActorsServiceCommand

  case class ConfirmPayment(transactionKey: String, leaseId: LeaseId, userDetails: UserDetails) extends ActorsServiceCommand

  def props: Props = Props(new FrontendService)

}

class FrontendService extends Actor with ActorLogging {

  import com.tpalanga.reification.common.Protocol._
  import FrontendService._

  import context.dispatcher

  override def receive = {

    case GetAvailableTickets(eventId) =>
      getAvailableTickets(eventId) pipeTo sender()


    case BookTickets(ticketType, quantity) =>
      bookTickets(ticketType, quantity) pipeTo sender()


    case ConfirmPayment(transactionKey, leaseId, userDetails) =>
      implicit val backend = AkkaHttpBackend()

      val documentIdFuture = for {
        _ <- savePaymentConfirmation(transactionKey, leaseId)
        tickets <- getTickets(leaseId)
        _ <- saveTicketData(tickets, userDetails, transactionKey)

        // post to another service via REST API
        documentId <- sttp.body(TicketOrder(tickets, userDetails, transactionKey))
          .post(uri"http://localhost:8184/ticketorder")
          .response(asJson[DocumentId])
          .send()
        _ = backend.close()
      } yield documentId.body

      documentIdFuture pipeTo sender()
  }

  private def getAvailableTickets(eventId: String): Future[TicketAvailabilities] = {
    log.info("getAvailableTickets")
    Future.successful(
      TicketAvailabilities(Map(
        TicketType(1) -> TicketStock(100),
        TicketType(2) -> TicketStock(200))
      )
    )
  }

  private def bookTickets(ticketType: TicketType, quantity: Int): Future[LeaseConfirmation] = {
    val uuid = UUID.randomUUID().toString
    log.info(s"bookTickets - $uuid")
    Future.successful(Accepted(LeaseId(uuid)))
  }

  private def savePaymentConfirmation(transactionKey: String, leaseId: LeaseId): Future[Unit] = {
    log.info(s"savePaymentConfirmation - $transactionKey, $leaseId")
    Future.successful(())
  }

  private def getTickets(leaseId: LeaseId): Future[Seq[TicketId]] = {
    log.info(s"getTickets - $leaseId")
    Future.successful(Seq(TicketId("000123"), TicketId("000124"), TicketId("000125")))
  }

  private def saveTicketData(tickets: Seq[TicketId], userDetails: UserDetails, transactionKey: String): Future[Unit] = {
    log.info(s"saveTicketData - $tickets, $userDetails, $transactionKey")
    Future.successful(())
  }


}


