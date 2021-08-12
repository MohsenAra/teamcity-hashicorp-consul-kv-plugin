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

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.log.Loggers
import jetbrains.buildServer.serverSide.BuildStartContext
import jetbrains.buildServer.serverSide.BuildStartContextProcessor
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SRunningBuild
import jetbrains.buildServer.serverSide.oauth.OAuthConstants
import org.jetbrains.teamcity.consul.kv.*

class ConsulKvBuildStartContextProcessor() : BuildStartContextProcessor {
    companion object {
        val LOG = Logger.getInstance(Loggers.SERVER_CATEGORY + "." + ConsulKvBuildStartContextProcessor::class.java.name)!!

        private fun getFeatures(build: SRunningBuild, reportProblems: Boolean): List<ConsulKvFeatureSettings> {
            val buildType = build.buildType ?: return emptyList()

            val connectionFeatures = buildType.project.getAvailableFeaturesOfType(OAuthConstants.FEATURE_TYPE).filter {
                ConsulKvConstants.FeatureSettings.FEATURE_TYPE == it.parameters[OAuthConstants.OAUTH_TYPE_PARAM]
            }

            // Two features with same prefix cannot coexist in same project
            // Though it's possible to override feature with same prefix in subproject
            val projectToFeaturePairs = connectionFeatures.map {
                it.projectId to ConsulKvFeatureSettings(it.parameters)
            }

            if (reportProblems) {
                projectToFeaturePairs.groupBy({ it.first }, { it.second }).forEach { pid, features ->
                    features.groupBy { it.namespace }
                            .filterValues { it.size > 1 }
                            .forEach { namespace, clashing ->
                                val nsDescripption = if (isDefault(namespace)) "default namespace" else "'$namespace' namespace"
                                val message = "Multiple HashiCorp Consul Kv connections with $nsDescripption present in project '$pid'"
                                build.addBuildProblem(BuildProblemData.createBuildProblem("VC_${build.buildTypeId}_${namespace}_$pid", "ConvsulKvConnection", message))
                                if (clashing.any { it.failOnError }) {
                                    build.stop(null, message)
                                }
                            }
                }
            }
            val consulKvFeature = projectToFeaturePairs.map { it.second }

            return consulKvFeature.groupBy { it.namespace }.map { (_, v) -> v.first() }
        }

        internal fun isShouldEnableConvsulKvIntegration(build: SBuild, settings: ConsulKvFeatureSettings): Boolean {
            val parameters = build.buildOwnParameters
            return isShouldSetEnvParameters(parameters, settings.namespace)
                    // Slowest part:
                    || ConsulKvReferencesUtil.hasReferences(build.parametersProvider.all, listOf(settings.namespace))
        }

    }

    override fun updateParameters(context: BuildStartContext) {
        val build = context.build

        val settingsList = getFeatures(build, true)
        if (settingsList.isEmpty())
            return

        settingsList.map { settings ->
            val ns = if (isDefault(settings.namespace)) "" else " ('${settings.namespace}' namespace)"
            if (!isShouldEnableConvsulKvIntegration(build, settings)) {
                LOG.debug("There's no need to fetch HashiCorp Consul Kv$ns parameter for build $build")
                return@map
            }

            
            settings.toSharedParameters().forEach { (key, value) ->
                context.addSharedParameter(key, value)
            }
        }
    }
}
