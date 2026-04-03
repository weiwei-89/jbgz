package cn.tj.food.netty_ext.handler.tcp;

public class LoginInfo {
    private final String clientId;
    private final String userName;
    private final String password;

    public LoginInfo(
            String clientId,
            String userName,
            String password
    ) {
        this.clientId = clientId;
        this.userName = userName;
        this.password = password;
    }

    public String getClientId() {
        return clientId;
    }
    public String getUserName() {
        return userName;
    }
    public String getPassword() {
        return password;
    }
}