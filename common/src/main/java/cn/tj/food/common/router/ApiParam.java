package cn.tj.food.common.router;

import java.util.HashMap;
import java.util.Map;

public class ApiParam {
    private final Map<String, Object> values = new HashMap<>();
    private final Map<Class<?>, ConvertFunction<Object, ?>> handlers = new HashMap<>();

    @FunctionalInterface
    private interface ConvertFunction<T, V> {
        V apply(T target) throws Exception;
    }

    public ApiParam() {
        this.registerHandlers();
    }

    public void set(String name, Object value) {
        this.values.put(name, value);
    }

    public Object get(String name) {
        return this.values.get(name);
    }

    public String getString(String name, String defaultValue) throws Exception {
        String value = this.get(name, String.class);
        if(value == null) {
            return defaultValue;
        }
        return value;
    }

    public Integer getInteger(String name, Integer defaultValue) throws Exception {
        Integer value = this.get(name, Integer.class);
        if(value == null) {
            return defaultValue;
        }
        return value;
    }

    public byte[] getBytes(String name, byte[] defaultValue) throws Exception {
        byte[] value = this.get(name, byte[].class);
        if(value == null) {
            return defaultValue;
        }
        return value;
    }

    private <T> T get(String name, Class<T> targetType) throws Exception {
        ConvertFunction<Object, ?> handler = this.handlers.get(targetType);
        if(handler == null) {
            throw new Exception(String.format("unsupported type: %s", targetType.getName()));
        }
        return (T) handler.apply(this.get(name));
    }

    private void registerHandlers() {
        this.register(String.class, this::handleString);
        this.register(Integer.class, this::handleInteger);
        this.register(byte[].class, this::handleBytes);
    }

    private <T> void register(Class<T> targetType, ConvertFunction<Object, T> handler) {
        this.handlers.put(
                targetType,
                target -> {
                    return handler.apply(target);
                }
        );
    }

    private String handleString(Object target) {
        if(target == null) {
            return null;
        }
        return String.valueOf(target);
    }

    private Integer handleInteger(Object target) {
        if(target == null) {
            return null;
        }
        return Integer.valueOf(target.toString());
    }

    private byte[] handleBytes(Object target) {
        if(target == null) {
            return null;
        }
        return (byte[]) target;
    }
}