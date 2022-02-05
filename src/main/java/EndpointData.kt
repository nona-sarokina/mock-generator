import org.eclipse.jetty.http.HttpMethod

data class EndpointData(
        var urlPath: String,
        var pathToFile: String,
        var status: Int,
        var method: HttpMethod
)


