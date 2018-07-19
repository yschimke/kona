// Copyright 2004-present Facebook. All Rights Reserved.
package com.baulsupp.sonar.okhttp

import com.facebook.sonar.plugins.network.NetworkReporter
import com.facebook.sonar.plugins.network.NetworkReporter.RequestInfo
import com.facebook.sonar.plugins.network.NetworkReporter.ResponseInfo
import com.facebook.sonar.plugins.network.NetworkSonarPlugin
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import java.io.IOException
import java.util.UUID

fun Headers.toSonar(): List<NetworkReporter.Header> = this.toMultimap().flatMap { (key, values) ->
  values.map { value -> NetworkReporter.Header(key, value) }
}

class SonarOkhttpInterceptor(val plugin: NetworkSonarPlugin) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()
    val randId = UUID.randomUUID().toString()

    plugin.reportRequest(convertRequest(request, randId))

    val responseInfo = ResponseInfo().apply { requestId = randId }
    try {
      return convertResponse(responseInfo, chain, request)
    } finally {
      plugin.reportResponse(responseInfo)
    }
  }

  fun convertResponse(responseInfo: ResponseInfo, chain: Interceptor.Chain, request: Request): Response {
    responseInfo.timeStamp = System.currentTimeMillis()

    try {
      val response = chain.proceed(request)
      val body = response.body()!!

      responseInfo.statusCode = response.code()
      responseInfo.headers = response.headers().toSonar()
      responseInfo.body = body.bytes()

      // Creating new response as can't used response.body() more than once
      return response.newBuilder().also { builder ->
        responseInfo.body?.let {
          builder.body(ResponseBody.create(body.contentType(), responseInfo.body))
        }
      }.build()
    } catch (ioe: IOException) {
      responseInfo.statusReason = ioe.toString()

      throw ioe
    }
  }

  private fun convertRequest(request: Request, identifier: String): RequestInfo {
    val info = RequestInfo()
    info.requestId = identifier
    info.timeStamp = System.currentTimeMillis()
    info.headers = request.headers().toSonar()
    info.method = request.method()
    info.uri = request.url().toString()

    info.body = request.body()?.let {
      val buffer = Buffer()
      it.writeTo(buffer)
      buffer.readByteArray()
    }

    return info
  }
}