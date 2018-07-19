package com.baulsupp.sonar

import com.baulsupp.okurl.security.InsecureHostnameVerifier
import com.baulsupp.okurl.security.InsecureTrustManager
import io.rsocket.android.RSocket
import io.rsocket.android.RSocketFactory
import io.rsocket.transport.okhttp.client.OkhttpWebsocketClientTransport
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.platform.Platform

fun main(args: Array<String>) {
  System.setProperty("javax.net.debug", "all")

  val sslContext = Platform.get().sslContext
  val tm = InsecureTrustManager
  sslContext.init(null, arrayOf(tm), null)
  val sslSocketFactory = sslContext.socketFactory
  val client = OkHttpClient.Builder().hostnameVerifier(InsecureHostnameVerifier).sslSocketFactory(sslSocketFactory, tm).build()

  val request = Request.Builder().url("http://localhost:8088").build()

  val rSocket: RSocket = RSocketFactory
          .connect()
          .setupPayload(SetupData("o", "a", "d").toPayload())
          .acceptor { { requesterRSocket -> handler(requesterRSocket) } }  // Optional handler RSocket
          .transport(OkhttpWebsocketClientTransport.create(client, request))
          .start().blockingGet()

  Thread.sleep(60000);
}