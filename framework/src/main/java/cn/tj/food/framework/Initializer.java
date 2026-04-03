package cn.tj.food.framework;

import cn.tj.food.common.tcp.ClientSession;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Initializer {
    private static final Logger logger = LoggerFactory.getLogger(Initializer.class);
    private static final String CONF_FILE_NAME = "jbgz.conf";

    private final String[] packages;

    public Initializer(String[] packages) {
        this.packages = packages;
    }

    public void start() throws Exception {
        this.loadConfigs();
        this.loadDrivers();
        this.scan();
    }

    private ConfReader confReader;

    private void loadConfigs() throws Exception {
        this.confReader = new ConfReader();
        this.confReader.readFromRoot(CONF_FILE_NAME);
    }

    private DriverInitializer driverInitializer;

    private void loadDrivers() throws Exception {
        this.driverInitializer = new DriverInitializer(this.packages);
        this.driverInitializer.load();
    }

    private void scan() throws Exception {
        List<ConfReader.Config> configList = this.confReader.getConfigList();
        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .forPackages(this.packages)
                        .setScanners(Scanners.TypesAnnotated)
        );
        Set<Class<?>> jbgzClassSet = reflections.getTypesAnnotatedWith(Jbgz.class);
        if(jbgzClassSet==null || jbgzClassSet.size()==0) {
            return;
        }
        for(Class<?> clazz : jbgzClassSet) {
            Jbgz jbgzAnnotation = clazz.getAnnotation(Jbgz.class);
            Field[] jbgzFields = clazz.getDeclaredFields();
            if(jbgzFields==null || jbgzFields.length==0) {
                continue;
            }
            for(Field jbgzField : jbgzFields) {
                if(!jbgzField.isAnnotationPresent(TcpClient.class)) {
                    continue;
                }
                TcpClient tcpClientAnnotation = jbgzField.getAnnotation(TcpClient.class);
                String protocol = tcpClientAnnotation.protocol();
                if(StringUtils.isBlank(protocol)) {
                    continue;
                }
                if(!Modifier.isStatic(jbgzField.getModifiers())) {
                    continue;
                }
                TcpDriver tcpDriver = this.driverInitializer.getInstance(protocol, TcpDriver.class);
                if(tcpDriver == null) {
                    logger.warn("driver not found [name:{}]", protocol);
                }
                Map<String, String> configMap = ConfReader.queryConfig(configList, jbgzAnnotation.configPrefix(), tcpClientAnnotation.name());
                ClientSession<?> session;
                try {
                    session = tcpDriver.connect(configMap);
                } catch(Exception e) {
                    logger.warn("connect failed", e);
                    continue;
                }
                jbgzField.setAccessible(true);
                jbgzField.set(null, session);
            }
        }
    }
}