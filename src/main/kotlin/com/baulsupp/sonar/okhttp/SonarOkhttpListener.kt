package com.baulsupp.sonar.okhttp

import com.facebook.sonar.plugins.network.NetworkReporter
import com.facebook.sonar.plugins.network.NetworkSonarPlugin
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.Response
import java.io.IOException
import java.util.UUID

class SonarOkhttpListener(val identifier: String, val plugin: NetworkSonarPlugin) : EventListener() {
  val responseInfo = NetworkReporter.ResponseInfo().apply { requestId = identifier }

  override fun callStart(call: Call) {
    val request = call.request()

    val info = NetworkReporter.RequestInfo()
    info.requestId = identifier
    info.timeStamp = System.currentTimeMillis()
    info.headers = request.headers().toSonar()
    info.method = request.method()
    info.uri = request.url().toString()

    plugin.reportRequest(info)
  }

  override fun responseHeadersEnd(call: Call, response: Response) {
    responseInfo.statusCode = response.code()
    responseInfo.headers = response.headers().toSonar()
  }

  override fun callEnd(call: Call) {
    responseInfo.timeStamp = System.currentTimeMillis()

    plugin.reportResponse(responseInfo)
  }

  override fun callFailed(call: Call, ioe: IOException) {
    responseInfo.statusReason = ioe.toString()
    responseInfo.timeStamp = System.currentTimeMillis()

    plugin.reportResponse(responseInfo)
  }
}

class SonarOkhttpListenerFactory(val plugin: NetworkSonarPlugin) : EventListener.Factory {
  override fun create(call: Call): EventListener = SonarOkhttpListener(UUID.randomUUID().toString(), plugin)
}