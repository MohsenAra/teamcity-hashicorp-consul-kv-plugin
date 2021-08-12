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


@Suppress("MayBeConstant")
object ConsulKvConstants {
    val FEATURE_SUPPORTED_AGENT_PARAMETER = "teamcity.consol.kv.supported"

    val PARAMETER_PREFIX = "teamcity.consul.kv"
    val URL_PROPERTY_SUFFIX = ".url"
    val CONSUL_KV_NAMESPACE_PROPERTY_SUFFIX = ".consul.kv.namespace"
    val FAIL_ON_ERROR_PROPERTY_SUFFIX = ".failOnError"

    @JvmField val CONSUL_KV_PARAMETER_PREFIX = "consul-kv:"


    object FeatureSettings {
        @JvmField val FEATURE_TYPE = "teamcity-consul-kv"

        // Feature settings
        @JvmField val NAMESPACE = "namespace"
        @JvmField val DEFAULT_PARAMETER_NAMESPACE = ""

        @JvmField val CONSUL_KV_NAMESPACE = "consul-kv-namespace"
        @JvmField val DEFAULT_CONSUL_KV_NAMESPACE = ""

        @JvmField val URL = "url"

        @JvmField val ENDPOINT = "endpoint"
        @JvmField val DEFAULT_ENDPOINT_PATH = ""



        @JvmField val FAIL_ON_ERROR = "fail-on-error"

        @JvmField val AGENT_SUPPORT_REQUIREMENT = "teamcity.consul.kv.requirement"
        @JvmField val AGENT_SUPPORT_REQUIREMENT_VALUE = "%$FEATURE_SUPPORTED_AGENT_PARAMETER%"
    }

    object BehaviourParameters {
        val ExposeEnvSuffix = ".set.env"
    }


    // Special values
    val SPECIAL_FAILED_TO_FETCH = "FAILED_TO_FETCH"
    val SPECIAL_VALUES = setOf<String>(SPECIAL_FAILED_TO_FETCH)
}

