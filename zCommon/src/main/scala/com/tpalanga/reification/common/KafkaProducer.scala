package com.tpalanga.reification.common

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

import scala.concurrent.Future

object KafkaProducer {
}

class KafkaProducer(implicit system: ActorSystem, materializer: ActorMaterializer) extends LazyLogging {

  val producerSettings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
    .withBootstrapServers("localhost:9092")

  val kafkaProducer = producerSettings.createKafkaProducer()

  def sendMessages(topic: String, messages: List[String]): Future[Done] =
    Source(messages)
      .map { msg =>
        logger.info("=== Producing message")
        new ProducerRecord[Array[Byte], String](topic, msg)
      }
      .runWith(Producer.plainSink(producerSettings, kafkaProducer))


}

