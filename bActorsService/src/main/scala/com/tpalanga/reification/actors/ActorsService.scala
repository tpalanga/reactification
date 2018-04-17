package com.tpalanga.reification.actors

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import com.tpalanga.reification.actors.ActorsService.{BookTickets, ConfirmPayment, GetAvailableTickets}
import com.tpalanga.reification.common.Protocol.{LeaseId, TicketType, UserDetails}

import scala.concurrent.Future
import akka.pattern.pipe

object ActorsService {

  sealed trait ActorsServiceCommand
  case class GetAvailableTickets(eventId: String) extends ActorsServiceCommand
  case class BookTickets(ticketType: TicketType, quantity: Int) extends ActorsServiceCommand
  case class ConfirmPayment(transactionKey: String, leaseId: LeaseId, userDetails: UserDetails) extends ActorsServiceCommand

  def props: Props = Props(new ActorsService)

}

class ActorsService extends Actor with ActorLogging {
  import com.tpalanga.reification.common.Protocol._

  import context.dispatcher

  override def receive = {

    case GetAvailableTickets(eventId) =>
      getAvailableTickets(eventId) pipeTo sender()


    case BookTickets(ticketType, quantity) =>
      bookTickets(ticketType, quantity) pipeTo sender()


    case ConfirmPayment(transactionKey, leaseId, userDetails) =>
      val documentIdFuture = for {
        _ <- savePaymentConfirmation(transactionKey, leaseId)
        tickets <- getTickets(leaseId)
        _ <- saveTicketData(tickets, userDetails, transactionKey)
        documentId <- createPrintTicket(tickets, userDetails, transactionKey) // create pdf
      } yield documentId

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

  private def createPrintTicket(tickets: Seq[TicketId], userDetails: UserDetails, transactionKey: String): Future[DocumentId] = {
    log.info(s"createPrintTicket - $tickets, $userDetails, $transactionKey")
    Future.successful(DocumentId("tickets/000578.pdf"))
  }


}
