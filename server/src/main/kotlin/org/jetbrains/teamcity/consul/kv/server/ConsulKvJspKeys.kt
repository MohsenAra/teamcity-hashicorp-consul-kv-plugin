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

import org.jetbrains.teamcity.consul.kv.ConsulKvConstants

class ConsulKvJspKeys {
    val NAMESPACE = ConsulKvConstants.FeatureSettings.NAMESPACE
    val URL = ConsulKvConstants.FeatureSettings.URL
    val CONSUL_KV_NAMESPACE = ConsulKvConstants.FeatureSettings.CONSUL_KV_NAMESPACE

    val FAIL_ON_ERROR = ConsulKvConstants.FeatureSettings.FAIL_ON_ERROR

    val AGENT_REQUIREMENT = ConsulKvConstants.FeatureSettings.AGENT_SUPPORT_REQUIREMENT
}
