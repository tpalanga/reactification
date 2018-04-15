package com.tpalanga.reactification.a_monolith

import java.util.UUID

import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future


class MonolithService extends LazyLogging {

  import com.tpalanga.reification.common.Protocol._

  def getAvailableTickets(eventId: String): Future[TicketAvailabilities] = {
    logger.info("getAvailableTickets")
    Future.successful(
      TicketAvailabilities(Map(
        TicketType(1) -> TicketStock(100),
        TicketType(2) -> TicketStock(200))
      )
    )
  }

  def bookTickets(ticketType: TicketType, quantity: Int): Future[LeaseConfirmation] = {
    val uuid = UUID.randomUUID().toString
    logger.info(s"bookTickets - $uuid")
    Future.successful(Accepted(LeaseId(uuid)))
  }

  def savePaymentConfirmation(transactionKey: String, leaseId: LeaseId): Future[Unit] = {
    logger.info(s"savePaymentConfirmation - $transactionKey, $leaseId")
    Future.successful(())
  }

  def getTickets(leaseId: LeaseId): Future[Seq[TicketId]] = {
    logger.info(s"getTickets - $leaseId")
    Future.successful(Seq(TicketId("000123"), TicketId("000124"), TicketId("000125")))
  }

  def saveTicketData(tickets: Seq[TicketId], userDetails: UserDetails, transactionKey: String): Future[Unit] = {
    logger.info(s"saveTicketData - $tickets, $userDetails, $transactionKey")
    Future.successful(())
  }

  def createPrintTicket(tickets: Seq[TicketId], userDetails: UserDetails, transactionKey: String): Future[DocumentId] = {
    logger.info(s"saveTicketData - $tickets, $userDetails, $transactionKey")
    Future.successful(DocumentId("tickets/000578.pdf"))
  }
}
