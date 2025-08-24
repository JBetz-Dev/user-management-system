public class HttpResponse extends HttpMessage{
    private String version;
    private int statusCode;
    private String reasonPhrase;

    public String getVersion() {
        return version != null ? version : "";
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase != null ? reasonPhrase : "";
    }

    public String getStartLine() {
        return version + " " +  statusCode + " " + reasonPhrase;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }
}
