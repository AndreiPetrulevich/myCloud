package geekbrains.myCloud.core;

public abstract class AuthorizedCloudMessage implements CloudMessage {
    protected String token;

    public String getToken() {
        return token;
    }
}
