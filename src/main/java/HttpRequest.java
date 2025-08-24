public class HttpRequest extends HttpMessage {
    private String method;
    private String path;
    private String version;

    public String getMethod() {
        return method != null ? method : "";
    }

    public String getPath() {
        return path != null ? path : "";
    }

    public String getVersion() {
        return version != null ? version : "";
    }

    public String getStartLine() {
        return method + " " +  path + " " + version;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}