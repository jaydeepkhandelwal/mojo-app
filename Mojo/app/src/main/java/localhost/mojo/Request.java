package localhost.mojo;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jaydeep on 31/05/15.
 */
public class Request {
    private String url;
    private List<NameValuePair> requestParams;

    Request(){
        requestParams =  new ArrayList<NameValuePair>();
    }
    void addParams(String key, String value){
        NameValuePair nameValuePair = new BasicNameValuePair(key, value);
        requestParams.add(nameValuePair);

    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<NameValuePair> getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(List<NameValuePair> requestParams) {
        this.requestParams = requestParams;
    }
}
