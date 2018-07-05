// Copyright 2004-present Facebook. All Rights Reserved.
package com.baulsupp.sonarokhttp

import com.facebook.sonar.plugins.network.NetworkReporter
import com.facebook.sonar.plugins.network.NetworkReporter.RequestInfo
import com.facebook.sonar.plugins.network.NetworkReporter.ResponseInfo
import com.facebook.sonar.plugins.network.NetworkSonarPlugin
import java.io.IOException
import java.util.ArrayList
import java.util.Random
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import java.util.UUID

class SonarOkhttpInterceptor(val plugin: NetworkSonarPlugin) : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()
    val randId = UUID.randomUUID()
    plugin.reportRequest(convertRequest(request, randId))
    val response = chain.proceed(request)
    val body = response.body()
    val responseInfo = convertResponse(response, body!!, randId)
    plugin.reportResponse(responseInfo)
    // Creating new response as can't used response.body() more than once
    return response
            .newBuilder()
            .body(ResponseBody.create(body.contentType(), responseInfo.body))
            .build()
  }

  private fun bodyToByteArray(request: Request): ByteArray {
    try {
      val copy = request.newBuilder().build()
      val buffer = Buffer()
      copy.body()!!.writeTo(buffer)
      return buffer.readByteArray()
    } catch (e: IOException) {
      return e.message!!.toByteArray()
    }
  }

  private fun convertRequest(request: Request, identifier: UUID): RequestInfo {
    val headers = convertHeader(request.headers())
    val info = RequestInfo()
    info.requestId = identifier.toString()
    info.timeStamp = System.currentTimeMillis()
    info.headers = headers
    info.method = request.method()
    info.uri = request.url().toString()
    if (request.body() != null) {
      info.body = bodyToByteArray(request)
    }

    return info
  }

  private fun convertResponse(response: Response, body: ResponseBody,
          identifier: UUID): ResponseInfo {

    val headers = convertHeader(response.headers())
    val info = ResponseInfo()
    info.requestId = identifier.toString()
    info.timeStamp = response.receivedResponseAtMillis()
    info.statusCode = response.code()
    info.headers = headers
    try {
      info.body = body.bytes()
    } catch (e: IOException) {
      //Log.e("Sonar", e.toString());
      e.printStackTrace()
    }

    return info
  }

  private fun convertHeader(headers: Headers): List<NetworkReporter.Header> {
    val list = ArrayList<NetworkReporter.Header>()

    val keys = headers.names()
    for (key in keys) {
      list.add(NetworkReporter.Header(key, headers.get(key)))
    }
    return list
  }

  private fun randInt(min: Int, max: Int): Int {
    val rand = Random()
    return rand.nextInt(max - min + 1) + min
  }
}