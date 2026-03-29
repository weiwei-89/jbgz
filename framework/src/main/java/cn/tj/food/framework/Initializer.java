package cn.tj.food.framework;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Initializer {
    private final String path;
    private final Map<Class<?>, Object> instanceCache = new HashMap<>();

    public Initializer(String path) {
        this.path = path;
    }

    public void start() throws Exception {
        this.scan();
    }

    private void scan() throws Exception {
        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .forPackages(this.path)
                        .setScanners(Scanners.TypesAnnotated)
        );
        Set<Class<?>> jbgzClassSet = reflections.getTypesAnnotatedWith(Jbgz.class);
        if(jbgzClassSet==null || jbgzClassSet.size()==0) {
            return;
        }
        for(Class<?> clazz : jbgzClassSet) {
//            Jbgz jbgzAnnotation = clazz.getAnnotation(Jbgz.class);
            Object jbgzInstance = clazz.getDeclaredConstructor().newInstance();
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
                if(protocol.equals("mqtt")) {
                    jbgzField.setAccessible(true);
                    Class<?> mqttConnectorClass = Class.forName("cn.tj.food.netty_ext.handler.mqtt.MqttConnector");
                    Object mqttConnectorInstance = mqttConnectorClass.getDeclaredConstructor().newInstance();
                    jbgzField.set(jbgzInstance, mqttConnectorInstance);
                }
            }
            this.instanceCache.put(clazz, jbgzInstance);
        }
    }

    public <T> T getInstance(Class<T> clazz) {
        return clazz.cast(this.instanceCache.get(clazz));
    }
}