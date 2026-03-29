package cn.tj.food.framework;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ConfReader {
    private final List<Config> configList = new ArrayList<>();

    public List<Config> getConfigList() {
        return this.configList;
    }

    public static class Config {
        private final String key;
        private final String value;

        public Config(
                String key,
                String value
        ) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }
        public String getValue() {
            return value;
        }
    }

    public void read(String path) throws Exception {
        this.read(new FileInputStream(path));
    }

    public void readFromRoot(String confName) throws Exception {
        InputStream in = ConfReader.class.getResourceAsStream(String.format("/%s", confName));
        try {
            if(in == null) {
                throw new Exception(String.format("config file not found [%s]", confName));
            }
            this.read(in);
        } finally {
            if(in != null) {
                in.close();
            }
        }
    }

    public void read(InputStream in) throws Exception {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(
                            in,
                            StandardCharsets.UTF_8
                    )
            );
            String line = null;
            while(true) {
                line = reader.readLine();
                if(line == null) {
                    break;
                }
                String[] parts = line.split("=");
                this.configList.add(new Config(parts[0], parts[1]));
            }
        } finally {
            if(reader != null) {
                reader.close();
            }
        }
    }
}