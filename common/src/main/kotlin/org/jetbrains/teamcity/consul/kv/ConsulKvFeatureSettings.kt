/*
 * Copyright 2000-2020 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.teamcity.consul.kv

 

 

data class ConsulKvFeatureSettings(val namespace: String, val url: String, val failOnError: Boolean) {

    constructor(url: String) : 
    this(ConsulKvConstants.FeatureSettings.DEFAULT_PARAMETER_NAMESPACE, url, true)

   // constructor(namespace: String, url: String, failOnError: Boolean)
   //  : this(namespace, url, failOnError)

    constructor(map: Map<String, String>) : this(
            map[ConsulKvConstants.FeatureSettings.NAMESPACE] ?: ConsulKvConstants.FeatureSettings.DEFAULT_PARAMETER_NAMESPACE,
            map[ConsulKvConstants.FeatureSettings.URL] ?: "",
            map[ConsulKvConstants.FeatureSettings.FAIL_ON_ERROR]?.toBoolean() ?: false
    )

    fun toFeatureProperties(map: MutableMap<String, String>) {
        map[ConsulKvConstants.FeatureSettings.URL] = url
        map[ConsulKvConstants.FeatureSettings.NAMESPACE] = namespace
        map[ConsulKvConstants.FeatureSettings.FAIL_ON_ERROR] = failOnError.toString()
        
    }

    fun toSharedParameters(): Map<String, String> {
        return mapOf(
                ConsulKvConstants.FAIL_ON_ERROR_PROPERTY_SUFFIX to failOnError.toString(),
                ConsulKvConstants.URL_PROPERTY_SUFFIX to url                
        ).mapKeys { getConsulKvParameterName(namespace, it.key) }
    }

    companion object {
        fun getDefaultParameters(): Map<String, String> {
            return mapOf(
                    ConsulKvConstants.FeatureSettings.NAMESPACE to ConsulKvConstants.FeatureSettings.DEFAULT_PARAMETER_NAMESPACE,
                    ConsulKvConstants.FeatureSettings.AGENT_SUPPORT_REQUIREMENT to ConsulKvConstants.FeatureSettings.AGENT_SUPPORT_REQUIREMENT_VALUE,
                    ConsulKvConstants.FeatureSettings.FAIL_ON_ERROR to "true",
                    ConsulKvConstants.FeatureSettings.ENDPOINT to ConsulKvConstants.FeatureSettings.DEFAULT_ENDPOINT_PATH,
                    ConsulKvConstants.FeatureSettings.URL to "http://localhost:8500"
            )
        }

        fun fromSharedParameters(parameters: Map<String, String>, namespace: String): ConsulKvFeatureSettings {
            val url = parameters[getConsulKvParameterName(namespace, ConsulKvConstants.URL_PROPERTY_SUFFIX)] ?: ""
                    ?: ""
            val failOnError = parameters[getConsulKvParameterName(namespace, ConsulKvConstants.FAIL_ON_ERROR_PROPERTY_SUFFIX)]?.toBoolean()
                    ?: false

            return ConsulKvFeatureSettings(namespace, url, failOnError)
        }
    }
}
