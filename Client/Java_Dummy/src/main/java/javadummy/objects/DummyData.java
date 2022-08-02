package javadummy.objects;


public class DummyData {
    static int count = 0;

    private final String macAddress;
    private String token = "";


    public DummyData() {
        this.macAddress = "1a:2b:3d:4e:5f:g" + count;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    
}
