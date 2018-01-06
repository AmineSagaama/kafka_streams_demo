package com.asagaama.demo.kafkastreams

import java.util
import java.util.Properties
import java.util.regex.Pattern

import com.lightbend.kafka.scala.streams.{KTableS, StreamsBuilderS}
import com.twitter.chill.KryoInjection
import org.apache.kafka.common.serialization.{Deserializer, Serde, Serdes, Serializer}
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.kstream.Produced

import scala.util.Try

object StreamApp {

  implicit val stringSerde: Serde[String] = Serdes.String()
  implicit val longSerde: Serde[Long] = new CaseClassKryoSerDe[Long]

  def main(args: Array[String]): Unit = {
    val builder = new StreamsBuilderS
    val textLines = builder.stream[String, String]("inputTopic")

    val pattern = Pattern.compile("\\W+", Pattern.UNICODE_CHARACTER_CLASS)

    val wordCounts: KTableS[String, Long] =
      textLines.flatMapValues(v => pattern.split(v.toLowerCase))
        .groupBy((_, v) => v)
        .count

    wordCounts.toStream.to("outputTopic", Produced.`with`(stringSerde, longSerde))
    val props: Properties = new Properties()
    val streams = new KafkaStreams(builder.build, props)
    streams.start()
  }

}

class CaseClassKryoSerDe[T] extends Serde[T] {
  override def deserializer(): Deserializer[T] = new CaseClassKryoDeserializer[T]

  override def serializer(): Serializer[T] = new CaseClassKryoSerializer[T]

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {}

  override def close(): Unit = {}
}

class CaseClassKryoDeserializer[T] extends Deserializer[T] {

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {}

  override def close(): Unit = {}

  override def deserialize(topic: String, bytes: Array[Byte]): T = {

    if (bytes != null) {
      val invert: Try[Object] = KryoInjection.invert(bytes)
      invert.get.asInstanceOf[T] // we want it to fail
    }
    else {
      null.asInstanceOf[T]
    }
  }

}

class CaseClassKryoSerializer[T] extends Serializer[T] {

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {}

  override def serialize(topic: String, data: T): Array[Byte] = {

    val apply = KryoInjection.apply(data)

    apply
  }

  override def close(): Unit = {}
}
