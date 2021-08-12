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
package org.jetbrains.teamcity.consul.kv.agent

import com.amazonaws.auth.InstanceProfileCredentialsProvider
import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.log.Loggers
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.util.ssl.SSLTrustStoreProvider
import org.jetbrains.teamcity.consul.kv.*
import org.springframework.web.client.RestTemplate
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class ConsulKvBuildFeature(dispatcher: EventDispatcher<AgentLifeCycleListener>,
                        private val trustStoreProvider: SSLTrustStoreProvider,
                        private val myConsulKvParametersResolver: ConsulKvParametersResolver) : AgentLifeCycleAdapter() {
    companion object {
        val LOG = Logger.getInstance(Loggers.AGENT_CATEGORY + "." + ConsulKvBuildFeature::class.java.name)!!
    }

    init {
            LOG.info("HashiCorp Consul  intergration")
      
        if (isJava8OrNewer()) {
            dispatcher.addListener(this)
            LOG.info("HashiCorp Consul Kv intergration enabled")
        } else {
            dispatcher.addListener(FailBuildListener())
            LOG.warn("HashiCorp Consul Kv integration disabled: agent should be running under Java 1.8 or newer")
        }
    }


    override fun afterAgentConfigurationLoaded(agent: BuildAgent) {
        agent.configuration.addConfigurationParameter(ConsulKvConstants.FEATURE_SUPPORTED_AGENT_PARAMETER, "true")
    }

    override fun buildStarted(runningBuild: AgentRunningBuild) {
        val parameters = runningBuild.sharedConfigParameters
        val namespaces = parameters.keys
                .filter { isUrlParameter(it) }
                .mapNotNull {
                    it
                            .removePrefix(ConsulKvConstants.PARAMETER_PREFIX)
                            .removeSuffix(ConsulKvConstants.URL_PROPERTY_SUFFIX)
                            .removePrefix(".")
                }.toSet()

        namespaces.forEach { namespace ->
            // namespace is either empty string or something like 'id'
            val settings = ConsulKvFeatureSettings.fromSharedParameters(parameters, namespace)

            if (settings.url.isBlank()) {
                return@forEach
            }
            val logger = runningBuild.buildLogger
            logger.activity("HashiCorp Consul Kv" + if (isDefault(namespace)) "" else " ('$namespace' namespace)", ConsulKvConstants.FeatureSettings.FEATURE_TYPE) 
            {

                logger.message("xxxxxxxxxxxxxxxx")
                myConsulKvParametersResolver.resolve(runningBuild, settings)
            }
        }
    }

   

     

    override fun beforeBuildFinish(build: AgentRunningBuild, buildStatus: BuildFinishedStatus) {
    }

    override fun buildFinished(build: AgentRunningBuild, buildStatus: BuildFinishedStatus) {
    }
}
