package com.baulsupp.sonar.sandbox

import com.baulsupp.sonar.kotlin.KonaPlugin
import com.baulsupp.sonar.kotlin.sonarArray
import com.baulsupp.sonar.kotlin.toSonarObject
import com.facebook.sonar.core.SonarArray
import com.facebook.sonar.core.SonarConnection
import okhttp3.internal.platform.Platform

data class Sandbox(val name: String, val host: String)

fun List<Sandbox>.toSonarArray(): SonarArray {
  val list = this
  return sonarArray {
    list.forEach {
      put(mapOf(it.name to it.host).toSonarObject())
    }
  }
}

interface SandboxStrategy {
  fun knownSandboxes(): List<Sandbox>
  fun setSandbox(sandboxName: String?)
}

class SandboxPlugin(val strategy: SandboxStrategy) : KonaPlugin("Sandbox") {
  override fun onConnect(connection: SonarConnection) {
    connection.receive("setSandbox") { params, receiver ->
      Platform.get().log(Platform.WARN, "SET: $params", null)
      strategy.setSandbox(params.getString("sandbox"))
      receiver.success(mapOf("result" to true).toSonarObject())
    }
    connection.receive("getSandbox") { params, receiver ->
      Platform.get().log(Platform.WARN, "GET: $params", null)
      receiver.success(strategy.knownSandboxes().toSonarArray())
    }
  }
}
