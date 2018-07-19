package com.baulsupp.sonar

import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import io.reactivex.Completable
import io.rsocket.android.AbstractRSocket
import io.rsocket.android.Payload
import io.rsocket.android.RSocket
import io.rsocket.android.RSocketFactory
import io.rsocket.android.transport.netty.client.TcpClientTransport
import io.rsocket.android.util.PayloadImpl
import reactor.ipc.netty.tcp.TcpClient

data class SetupData(val os: String, val app: String, val device: String, val device_id: String? = null) {
  fun toPayload() = PayloadImpl.Companion.textPayload("{\"app\": \"$app\",\"os\": \"$os\",\"device\": \"$device\"}")
}

fun handler(requesterRSocket: RSocket): RSocket {
  return object : AbstractRSocket() {
    override fun fireAndForget(payload: Payload): Completable {
      val d = payload.dataUtf8
      val md = payload.metadataUtf8
      println("FNF: " + d + " " + md)

      if (d.contains("getPlugins")) {
        val result = "{\"id\": 0, \"success\": {\"plugins\": [\"Sandbox\", \"Network\"]}}"
        println(result)
        requesterRSocket.fireAndForget(PayloadImpl.textPayload(result)).blockingAwait()
      }

      return Completable.complete()
    }
  }
}

fun main(args: Array<String>) {
  System.setProperty("javax.net.debug", "ssl")
  val sslContext2: SslContext = SslContextBuilder.forClient().trustManager(
          InsecureTrustManagerFactory.INSTANCE).build()

  val client2: TcpClient = TcpClient.create { c ->
    c.sslSupport().sslContext(sslContext2).host("localhost").port(8088)
  }

  val rSocket: RSocket = RSocketFactory
          .connect()
          .setupPayload(SetupData("o", "a", "d").toPayload())
          .acceptor { { requesterRSocket -> handler(requesterRSocket) } }  // Optional handler RSocket
          .transport(TcpClientTransport.create(client2))
          .start().blockingGet()

  Thread.sleep(60000);
}