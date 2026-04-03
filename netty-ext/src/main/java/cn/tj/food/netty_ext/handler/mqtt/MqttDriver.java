package cn.tj.food.netty_ext.handler.mqtt;

import cn.tj.food.common.task.SimpleProcessor;
import cn.tj.food.common.task.TaskPool;
import cn.tj.food.common.tcp.Config;
import cn.tj.food.common.tcp.EventListener;
import cn.tj.food.common.tcp.User;
import cn.tj.food.framework.Driver;
import cn.tj.food.framework.TcpDriver;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Driver(protocol="mqtt3.1.1")
public class MqttDriver implements TcpDriver {
    private static final String PARAM_HOST = "host";
    private static final String PARAM_PORT = "port";
    private static final String PARAM_CLIENT_ID = "client-id";
    private static final String PARAM_USER_NAME = "user-name";
    private static final String PARAM_PASSWORD = "password";
    private static final long HEARTBEAT = 10*1000;

    private MqttConnector connector;
    private final TaskPool taskPool = TaskPool.getInstance();

    @Override
    public void init() throws Exception {
        this.connector = new DefaultMqttConnector(HEARTBEAT);
    }

    @Override
    public MqttSession connect(Map<String, String> param) throws Exception {
        String host = param.get(PARAM_HOST);
        if(StringUtils.isBlank(host)) {
            throw new Exception("host is blank");
        }
        String port = param.get(PARAM_PORT);
        if(StringUtils.isBlank(port)) {
            throw new Exception("port is blank");
        }
        String clientId = param.get(PARAM_CLIENT_ID);
        if(StringUtils.isBlank(clientId)) {
            throw new Exception("client_id is blank");
        }
        String userName = param.get(PARAM_USER_NAME);
        if(StringUtils.isBlank(userName)) {
            throw new Exception("user_name is blank");
        }
        String password = param.get(PARAM_PASSWORD);
        if(StringUtils.isBlank(password)) {
            throw new Exception("password is blank");
        }
        Config config = new Config();
        config.setHost(host);
        config.setPort(Integer.parseInt(port));
        User user = new User();
        user.setName(clientId);
        MqttSession session = this.connector.connect(config, user).getSession();
        LoginInfo loginInfo = new LoginInfo(clientId, userName, password);
        session.addReloginListener(new EventListener() {
            @Override
            public void process() throws Exception {
                loginFunction.apply(session, loginInfo);
            }
        });
        this.loginFunction.apply(session, loginInfo);
        return session;
    }

    @FunctionalInterface
    private interface LoginFunction {
        void apply(MqttSession session, LoginInfo loginInfo) throws Exception;
    }

    private final LoginFunction loginFunction = (session, loginInfo) -> {
        this.taskPool.addGeneralTask(
                "login",
                new LoginProcessor(session, loginInfo)
        );
    };

    private static class LoginInfo {
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

    private static class LoginProcessor extends SimpleProcessor {
        private final MqttSession session;
        private final LoginInfo loginInfo;

        public LoginProcessor(
                MqttSession session,
                LoginInfo loginInfo
        ) {
            this.session = session;
            this.loginInfo = loginInfo;
        }

        @Override
        public void process() throws Exception {
            this.session.login(
                    this.loginInfo.getClientId(),
                    this.loginInfo.getUserName(),
                    this.loginInfo.getPassword()
            );
        }
    }
}