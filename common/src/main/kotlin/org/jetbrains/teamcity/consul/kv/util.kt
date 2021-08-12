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

import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.util.StringUtil
import jetbrains.buildServer.util.VersionComparatorUtil
import jetbrains.buildServer.util.ssl.SSLTrustStoreProvider
import org.jetbrains.teamcity.consul.kv.support.ClientHttpRequestFactoryFactory
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.converter.ByteArrayHttpMessageConverter
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.util.Assert
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import java.net.URI
import com.intellij.openapi.diagnostic.Logger
import org.springframework.web.util.DefaultUriTemplateHandler


fun isDefault(namespace: String): Boolean {
    return namespace == ConsulKvConstants.FeatureSettings.DEFAULT_PARAMETER_NAMESPACE
}

fun getEnvPrefix(namespace: String): String {
    return if (isDefault(namespace)) ""
    else namespace.replace("[^a-zA-Z0-9_]".toRegex(), "_").toUpperCase() + "_"
}

fun getConsulKvParameterName(namespace: String, suffix: String): String {
    if (isDefault(namespace)) return ConsulKvConstants.PARAMETER_PREFIX + suffix
    return ConsulKvConstants.PARAMETER_PREFIX + ".$namespace" + suffix
}

fun isUrlParameter(value: String) =
        value.startsWith(ConsulKvConstants.PARAMETER_PREFIX) && value.endsWith(ConsulKvConstants.URL_PROPERTY_SUFFIX)

fun isJava8OrNewer(): Boolean {
    return VersionComparatorUtil.compare(System.getProperty("java.specification.version"), "1.8") >= 0
}

fun createClientHttpRequestFactory(trustStoreProvider: SSLTrustStoreProvider): ClientHttpRequestFactory {
    return ClientHttpRequestFactoryFactory.create(trustStoreProvider)
}

fun createRestTemplate(settings: ConsulKvFeatureSettings, trustStoreProvider: SSLTrustStoreProvider): RestTemplate {
    val endpoint =settings.url
    val factory = createClientHttpRequestFactory(trustStoreProvider)
    // HttpComponents.usingHttpComponents(options, sslConfiguration)

    return createRestTemplate(endpoint, factory)    
}

fun createRestTemplate(endpoint: String, factory: ClientHttpRequestFactory): RestTemplate {
    val template = createRestTemplate()

    template.requestFactory = factory
    //DefaultUriTemplateHandler defaultUriTemplateHandler =  DefaultUriTemplateHandler();
//defaultUriTemplateHandler.setBaseUrl(url);
    template.uriTemplateHandler = DefaultUriTemplateHandler()


    return template
}





private fun createRestTemplate(): RestTemplate {
    // However custom Jackson2 converter is used
    val converters = listOf<HttpMessageConverter<*>>(
            ByteArrayHttpMessageConverter(),
            StringHttpMessageConverter()
    )
    return RestTemplate(converters)
}

fun isShouldSetEnvParameters(parameters: MutableMap<String, String>, namespace: String): Boolean {
    return parameters[getConsulKvParameterName(namespace, ConsulKvConstants.BehaviourParameters.ExposeEnvSuffix)]
            ?.toBoolean() ?: false
}



fun getBaseUrl():String?{
return "null"
}
fun String?.nullIfEmpty(): String? {
    return StringUtil.nullIfEmpty(this)
}

fun String.ensureHasPrefix(prefix: String): String {
    return if (!this.startsWith(prefix)) "$prefix$this" else this
}

fun String.pluralize(size: Int) = StringUtil.pluralize(this, size)
fun String.pluralize(collection: Collection<Any>) = this.pluralize(collection.size)
fun String.sizeAndPluralize(collection: Collection<Any>) = "${collection.size} " + this.pluralize(collection)

fun <T> BuildProgressLogger.activity(activityName: String, activityType: String, body: () -> T): T {
    this.activityStarted(activityName, activityType)
    try {
        return body()
    } catch (t: Throwable) {
        this.internalError(activityType, "Exception occured: ${t.message}", t)
        throw t
    } finally {
        this.activityFinished(activityName, activityType)
    }
}