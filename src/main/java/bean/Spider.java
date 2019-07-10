package bean;


/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/10 9:13
 * @Description:
 */
public class Spider {
    String[] startUrls;
    Response response;

    public String[] getStartUrls() {
        return startUrls;
    }

    public void setStartUrls(String[] startUrls) {
        this.startUrls = startUrls;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }
}
