package com.baulsupp.sonar.kotlin

import com.facebook.sonar.core.SonarArray
import com.facebook.sonar.core.SonarObject

fun sonarObject(init: SonarObject.Builder.() -> Unit): SonarObject = SonarObject.Builder().apply(init).build()

fun sonarArray(init: SonarArray.Builder.() -> Unit): SonarArray = SonarArray.Builder().apply(init).build()

fun Map<String, Any>.toSonarObject(): SonarObject {
  val m = this
  return sonarObject {
    m.forEach { k, v ->
      put(k, v)
    }
  }
}
