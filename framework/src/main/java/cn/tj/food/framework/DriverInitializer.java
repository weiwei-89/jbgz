package cn.tj.food.framework;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DriverInitializer {
    private final String[] packages;
    private final Map<String, TcpDriver> instanceCache = new HashMap<>();

    public DriverInitializer(String[] packages) {
        this.packages = packages;
    }

    public void load() throws Exception {
        this.scan();
        this.init();
    }

    private void scan() throws Exception {
        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .forPackages(this.packages)
                        .setScanners(Scanners.TypesAnnotated)
        );
        Set<Class<?>> driverClassSet = reflections.getTypesAnnotatedWith(Driver.class);
        if(driverClassSet==null || driverClassSet.size()==0) {
            return;
        }
        for(Class<?> driverClass : driverClassSet) {
            if(!TcpDriver.class.isAssignableFrom(driverClass)) {
                continue;
            }
            Driver driverAnnotation = driverClass.getAnnotation(Driver.class);
            Object driverInstance = driverClass.getDeclaredConstructor().newInstance();
            this.instanceCache.putIfAbsent(driverAnnotation.name(), TcpDriver.class.cast(driverInstance));
        }
    }

    private void init() throws Exception {
        for(Map.Entry<String, TcpDriver> entry : this.instanceCache.entrySet()) {
            TcpDriver tcpDriver = this.instanceCache.get(entry.getKey());
            try {
                tcpDriver.init();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public <T> T getInstance(String name, Class<T> clazz) {
        return clazz.cast(this.instanceCache.get(name));
    }
}