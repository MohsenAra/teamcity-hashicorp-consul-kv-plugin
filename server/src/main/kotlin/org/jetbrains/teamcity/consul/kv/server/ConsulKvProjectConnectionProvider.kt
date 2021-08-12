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
package org.jetbrains.teamcity.consul.kv.server

import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.serverSide.oauth.OAuthConnectionDescriptor
import jetbrains.buildServer.serverSide.oauth.OAuthProvider
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.jetbrains.teamcity.consul.kv.ConsulKvConstants
import org.jetbrains.teamcity.consul.kv.ConsulKvFeatureSettings
import org.jetbrains.teamcity.consul.kv.isDefault

class ConsulKvProjectConnectionProvider(private val descriptor: PluginDescriptor) : OAuthProvider() {
    override fun getType(): String = ConsulKvConstants.FeatureSettings.FEATURE_TYPE

    override fun getDisplayName(): String = "HashiCorp Consul Kv"

    override fun describeConnection(connection: OAuthConnectionDescriptor): String {
        val settings = ConsulKvFeatureSettings(connection.parameters)
        return "Connection to HashiCorp Consul Kv server at ${settings.url}" +
                if (isDefault(settings.namespace)) "" else ", namespace '${settings.namespace}'"
    }

    override fun getDefaultProperties(): Map<String, String> {
        return ConsulKvFeatureSettings.getDefaultParameters()
    }

    override fun getEditParametersUrl(): String? {
        return descriptor.getPluginResourcesPath("editProjectConnectionConsulKv.jsp")
    }

    override fun getPropertiesProcessor(): PropertiesProcessor? {
        return getParametersProcessor()
    }

    companion object {
        fun getParametersProcessor(): PropertiesProcessor {
            return PropertiesProcessor {
                val errors = ArrayList<InvalidProperty>()
                if (it[ConsulKvConstants.FeatureSettings.URL].isNullOrBlank()) {
                    errors.add(InvalidProperty(ConsulKvConstants.FeatureSettings.URL, "Should not be empty"))
                }
                // NAMESPACE can be empty, means default one
                val namespace = it[ConsulKvConstants.FeatureSettings.NAMESPACE] ?: ConsulKvConstants.FeatureSettings.DEFAULT_PARAMETER_NAMESPACE
                val namespaceRegex = "[a-zA-Z0-9_-]+"
                if (namespace != "" && !namespace.matches(namespaceRegex.toRegex())) {
                    errors.add(InvalidProperty(ConsulKvConstants.FeatureSettings.NAMESPACE, "Non-default namespace should match regex '$namespaceRegex'"))
                }              

                // Convert slashes if needed of add new fields
                ConsulKvFeatureSettings(it).toFeatureProperties(it)

                return@PropertiesProcessor errors
            }
        }
    }
}
