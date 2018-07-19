package com.baulsupp.sonar.kotlin

import com.facebook.sonar.core.SonarConnection
import com.facebook.sonar.core.SonarPlugin

open class KonaPlugin(private val pluginId: String): SonarPlugin {
  val commands: MutableMap<String, String> = mutableMapOf()

  override fun onConnect(connection: SonarConnection) {
  }

  override fun getId(): String = pluginId

  override fun onDisconnect() {
  }
}