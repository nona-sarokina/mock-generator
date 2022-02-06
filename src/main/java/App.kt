import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.javalin.Javalin
import org.eclipse.jetty.http.HttpMethod
import java.io.File
import kotlin.streams.toList

fun main() {
    val configPathPropName = "endpoint.config.path"
    val portPropName = "port"
    val configPath = System.getProperty(configPathPropName) ?: throw java.lang.Exception("specify $configPathPropName")
    val portProp = System.getProperty(portPropName)

    val app = Javalin.create().apply {
        exception(Exception::class.java) { e, _ -> e.printStackTrace() }
        error(404) { ctx -> ctx.json("not found") }
    }.start(portProp?.toInt() ?: 7000)

    val mapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule())
    val endpointData: List<EndpointData> = mapper.readValue(File(configPath))
    endpointData.forEach {
        when (it.method) {
            HttpMethod.GET -> {
                app.get(it.urlPath) { ctx -> ctx.json(getResponseBody(it.pathToFile)).status(it.status) }
            }
            HttpMethod.POST -> app.post(it.urlPath) { ctx -> ctx.json(getResponseBody(it.pathToFile)).status(it.status) }
            else -> println("not supported")
        }
    }
    val toList = endpointData.stream().map { "${it.method} ${it.urlPath}" }.toList()
    app.get("/") { ctx -> ctx.result("endpoints:\n${toList.joinToString(separator = "\n")}") }
}

fun getResponseBody(filePath: String): String = File(filePath).readText(Charsets.UTF_8)
