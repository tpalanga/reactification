package com.tpalanga.reification.test

import com.softwaremill.sttp._
import com.softwaremill.sttp.akkahttp._
import com.softwaremill.sttp.json4s._
import org.scalatest.{AsyncFlatSpec, EitherValues, Matchers}

object AMonolithSpec {
  val eventId = "event1234"
  val baseUrl = s"http://localhost:8081/events/tickets/$eventId"

  case class TicketStock(value: Int)

  case class TicketAvailabilities(stock: Map[String, TicketStock])

  case class TicketRequest(ticketType: Int, quantity: Int)
  case class LeaseResponse(leaseId: String)

  case class PaymentConfirmation(transactionKey: String, leaseId: String)
  case class DownloadResponse(downloadLink: String)

}

class AMonolithSpec extends AsyncFlatSpec with Matchers with EitherValues {

  import AMonolithSpec._

  implicit val backend = AkkaHttpBackend()

  "Monolith" should "process ticket requests and return download link" in {

    val responseAvailabilities = sttp.get(uri"$baseUrl")
      .response(asJson[TicketAvailabilities])
    def responseBook(ticketType: Int, quantity: Int) = sttp
      .body(TicketRequest(ticketType, quantity))
      .post(uri"$baseUrl/book")
      .response(asJson[LeaseResponse])
    def responsePayment(transactionKey: String, leaseId: String) = sttp
      .body(PaymentConfirmation(transactionKey, leaseId))
      .post(uri"$baseUrl/paymentDone")
      .response(asJson[DownloadResponse])

    for {
      r1 <- responseAvailabilities.send()
      availabilities = r1.body.right.value
      _ = availabilities shouldBe TicketAvailabilities(Map("1" -> TicketStock(100), "2" -> TicketStock(200)))
      r2 <- responseBook(1, 2).send()
      lease = r2.body.right.value
      _ = lease shouldBe a[LeaseResponse]
      leaseId = lease.leaseId
      _ = println(s"leaseId: $leaseId")
      r3 <- responsePayment("PayKey00123", leaseId).send()
      download = r3.body.right.value
      _ = download.downloadLink shouldBe "/downloads/tickets/000578.pdf"
    } yield {
      backend.close()
      1 shouldBe 1
    }

  }

}
