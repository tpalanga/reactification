package com.tpalanga.reactification.a_monolith.service

import scala.concurrent.Future

object MonolithService {
  case class TicketType(id: Int)
  case class TicketStock(value: Int)
  case class TicketId(serialNo: String)
  case class DocumentId(filePath: String)
  case class LeaseId(value: String)

  case class TicketAvailabilities(stock: Map[TicketType, TicketStock])

  sealed trait LeaseConfirmation
  case class Accepted(leaseId: LeaseId) extends LeaseConfirmation
  case class Denied(msg: String) extends LeaseConfirmation

  case class UserDetails(email: String, name: String, phone: String)

}

class MonolithService {
  import MonolithService._


  def getAvailableTickets(eventId: String): Future[TicketAvailabilities] = ???

  def bookTickets(ticketType: TicketType, quantity: Int): Future[LeaseConfirmation] = ???

  def savePaymentConfirmation(transactionKey: String, leaseId: LeaseId): Future[Unit] = ???

  def getTickets(leaseId: LeaseId): Future[Seq[TicketId]] = ???

  def saveTicketData(tickets: Seq[TicketId], userDetails: UserDetails, transactionKey: String): Future[Unit] = ???

  def createPrintTicket(tickets: Seq[TicketId], userDetails: UserDetails, transactionKey: String): Future[DocumentId] = ???
}
