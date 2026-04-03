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

    private final String[] packages;
    private DriverInitializer driverInitializer;

    public Initializer(String[] packages) {
        this.packages = packages;
    }

    public void start() throws Exception {
        this.scan();
    }

    private void scan() throws Exception {
        this.driverInitializer = new DriverInitializer(this.packages);
        this.driverInitializer.load();
        ConfReader confReader = new ConfReader();
        confReader.readFromRoot("jbgz.conf");
        List<ConfReader.Config> configList = confReader.getConfigList();
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
                    e.printStackTrace();
                    continue;
                }
                jbgzField.setAccessible(true);
                jbgzField.set(null, session);
            }
        }
    }
}