/*

package org.jetbrains.teamcity.consul.kv.server

import com.intellij.openapi.diagnostic.Logger
import com.jayway.jsonpath.JsonPath
import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.log.Loggers
import jetbrains.buildServer.util.ssl.SSLTrustStoreProvider
import org.jetbrains.teamcity.consul.kv.*
import org.jetbrains.teamcity.consul.kv.support.ConsulKvTemplate
import java.net.URI
import java.util.*
import kotlin.collections.HashSet


class ConsulKvParametersResolver(private val trustStoreProvider: SSLTrustStoreProvider) {
    companion object {
        val LOG = Logger.getInstance(Loggers.AGENT_CATEGORY + "." + ConsulKvParametersResolver::class.java.name)!!
    }

    fun resolve(build: AgentRunningBuild, settings: ConsulKvFeatureSettings) {
        LOG.info("ConsulKvParametersResolver");
        val references = getReleatedParameterReferences(build, settings.namespace)
        if (references.isEmpty()) {
            LOG.info("There's nothing to resolve")
            return
        }
        val logger = build.buildLogger
        logger.message("${references.size} Consul ${"reference".pluralize(references)} to resolve: $references")

        val parameters = references.map { ConsulKvParameter.extract(ConsulKvReferencesUtil.getPath(it, settings.namespace)) }

        val replacements = doFetchAndPrepareReplacements(settings,  parameters, logger)

        if (settings.failOnError && replacements.errors.isNotEmpty()) {
            val ns = if (isDefault(settings.namespace)) "" else "('${settings.namespace}' namespace)"
            val message = "${"Error".pluralize(replacements.errors.size)} while fetching data from HashiCorp Consul Kv $ns"
            build.buildLogger.logBuildProblem(BuildProblemData.createBuildProblem("VC_${build.buildTypeId}_${settings.namespace}_A", "ConvsulKvConnection", message))
            build.stopBuild(message)
        }

        replaceParametersReferences(build, replacements.replacements, references, settings.namespace)

        LOG.info("Araz End");

    }


    fun doFetchAndPrepareReplacements(settings: ConsulKvFeatureSettings, parameters: List<ConsulKvParameter>, logger: BuildProgressLogger): ResolvingResult {
        val endpoint = settings.url
        LOG.info("Araz endpoint"+endpoint);
        val factory = createClientHttpRequestFactory(trustStoreProvider)
        val client = ConsulKvTemplate( endpoint,factory)

        return doFetchAndPrepareReplacements(client, parameters, logger)
    }

    fun doFetchAndPrepareReplacements(client: ConsulKvTemplate, parameters: List<ConsulKvParameter>, logger: BuildProgressLogger): ResolvingResult {
        return ConsulKvParametersFetcher(client, logger).doFetchAndPrepareReplacements(parameters)
    }

    data class ResolvingResult(val replacements: Map<String, String>, val errors: Map<String, String>)

    class ConsulKvParametersFetcher(private val client: ConsulKvTemplate,
                                 private val logger: BuildProgressLogger) {
        fun doFetchAndPrepareReplacements(parameters: List<ConsulKvParameter>): ResolvingResult {
            val responses = fetch(client, parameters.mapTo(HashSet()) { it.keyPath })

            return getReplacements(parameters, responses)
        }

        private class ResolvingError(message: String) : Exception(message)

        private fun fetch(client: ConsulKvTemplate, paths: Collection<String>): HashMap<String,String?> {
            val responses = HashMap<String, String?>(paths.size)

            for (path in paths.toSet()) {
                try {
                    val response = client.read( path.removePrefix("/"))
                    responses[path] =response 
                    LOG.info(" fetch data for path '$path' "+response)
                } catch (e: Exception) {
                    logger.warning("Failed to fetch data for path '$path'")
                    LOG.warn("Failed to fetch data for path '$path'", e)
                    responses[path] = null
                }
            }
            return responses
        }

        private fun getReplacements(parameters: List<ConsulKvParameter>, responses: Map<String, String?>): ResolvingResult {
            val replacements = HashMap<String, String>()
            val errors = HashMap<String, String>()

            for (parameter in parameters) {
                try {
                    val response = responses[parameter.keyPath]
                    if (response == null) {
                        logger.warning("Cannot resolve '${parameter.full}': data wasn't received from HashiCorp Consul Kv")
                        LOG.warn("Cannot resolve '${parameter.full}': data wasn't received from HashiCorp Consul Kv")
                        throw ResolvingError("Data wasn't received from HashiCorp Consul Kv")
                    }
                    replacements[parameter.full] = response
                } catch (e: ResolvingError) {
                    errors[parameter.full] = e.message!!
                }
            }
            return ResolvingResult(replacements, errors)
        }

    }


    private fun getReleatedParameterReferences(build: AgentRunningBuild, namespace: String): Collection<String> {
        val references = HashSet<String>()
        ConsulKvReferencesUtil.collect(build.sharedConfigParameters, references, namespace)
        ConsulKvReferencesUtil.collect(build.sharedBuildParameters.allParameters, references, namespace)
        return references.sorted()
    }
    private fun replaceParametersReferences(build: AgentRunningBuild, replacements: Map<String, String>, usages: Collection<String>, namespace: String) {
        // usage may not have leading slash
        for (usage in usages) {
            val replacement = replacements[ConsulKvReferencesUtil.getPath(usage, namespace)]
            if (replacement != null) {
                build.addSharedConfigParameter(usage, replacement)
            }
        }
    }
}

*/