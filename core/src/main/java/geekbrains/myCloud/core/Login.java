package geekbrains.myCloud.core;

public class Login extends AuthorizedCloudMessage {
    private final String login;
    private final String password;

    public Login(String login, String password) {
        this.token = getToken();
        this.login = login;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getLogin() {
        return login;
    }

    @Override
    public CommandType getType() {
        return CommandType.LOGIN;
    }
}
