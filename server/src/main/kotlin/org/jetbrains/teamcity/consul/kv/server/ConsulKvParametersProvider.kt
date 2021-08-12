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

import jetbrains.buildServer.agent.Constants
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.oauth.OAuthConstants
import jetbrains.buildServer.serverSide.parameters.AbstractBuildParametersProvider
import org.jetbrains.teamcity.consul.kv.*
import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.log.Loggers


class ConsulKvParametersProvider : AbstractBuildParametersProvider() {
    companion object {
        internal fun isFeatureEnabled(build: SBuild): Boolean {
            val buildType = build.buildType ?: return false
            val project = buildType.project

            if (project.getAvailableFeaturesOfType(OAuthConstants.FEATURE_TYPE).any {
                        ConsulKvConstants.FeatureSettings.FEATURE_TYPE == it.parameters[OAuthConstants.OAUTH_TYPE_PARAM]
                    }) 
                        return true

            return false
        }

    }

    override fun getParametersAvailableOnAgent(build: SBuild): Collection<String> {
        val LOG = Logger.getInstance(Loggers.AGENT_CATEGORY + "." + ConsulKvParametersProvider::class.java.name)!!

        LOG.info("buildType"+ build.buildType);
        val buildType = build.buildType ?: return emptyList()


        if (build.isFinished) return emptyList()

        LOG.info("isFeatureEnabled "+ isFeatureEnabled(build));

        if (!isFeatureEnabled(build)) return emptyList()


        val exposed = HashSet<String>()
        val connectionFeatures = buildType.project.getAvailableFeaturesOfType(OAuthConstants.FEATURE_TYPE).filter {
            ConsulKvConstants.FeatureSettings.FEATURE_TYPE == it.parameters[OAuthConstants.OAUTH_TYPE_PARAM]
        }
        val consulKvFeature = connectionFeatures.map {
            ConsulKvFeatureSettings(it.parameters)
        }
        val parameters = build.buildOwnParameters
        consulKvFeature.forEach { feature: ConsulKvFeatureSettings ->
        LOG.info("feature"+feature.namespace);
            
        }
        ConsulKvReferencesUtil.collect(parameters, exposed, consulKvFeature.map { feature -> feature.namespace })
        LOG.info("exposed"+exposed);
        
        return exposed
    }
}

