package cn.tj.food.test;

import cn.tj.food.common.DataUtil;
import cn.tj.food.onion.Knife;
import cn.tj.food.sauce.Papers;
import cn.tj.food.sauce.Path;
import cn.tj.food.sauce.ProtocolLoader;
import cn.tj.food.sauce.decoder.ProtocolDecoder;
import cn.tj.food.sauce.decoder.model.Info;
import com.alibaba.fastjson2.JSON;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class ProtocolDecodeTest {
    private static final Logger logger = LoggerFactory.getLogger(ProtocolDecodeTest.class);
    private static final int MAX_LENGTH = 10;

    private static final String PROTOCOL_ID = "protocol.id";
    private static final String PROTOCOL_PATH = "protocol.path";
    private static final String DATA_PATH = "data.path";
    private static final String OUTPUT_PATH = "output.path";

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(Option.builder().longOpt(PROTOCOL_ID).required(true).hasArg(true).build());
        options.addOption(Option.builder().longOpt(PROTOCOL_PATH).required(true).hasArg(true).build());
        options.addOption(Option.builder().longOpt(DATA_PATH).required(true).hasArg(true).build());
        options.addOption(Option.builder().longOpt(OUTPUT_PATH).required(true).hasArg(true).build());
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        String protocolId = cmd.getOptionValue(PROTOCOL_ID);
        String protocolPath = cmd.getOptionValue(PROTOCOL_PATH);
        String dataPath = cmd.getOptionValue(DATA_PATH);
        String outputPath = cmd.getOptionValue(OUTPUT_PATH);
        logger.info("loading protocol \"{}\"...... [{}]", protocolId, protocolPath);
        Path path = new Path(protocolPath, protocolId);
        Papers papers = ProtocolLoader.build()
                .setFormat(ProtocolLoader.DEFAULT_FORMAT)
                .load(path);
        papers.setMaxLength(MAX_LENGTH);
        cn.tj.food.common.FileReader fileReader = new cn.tj.food.common.FileReader();
        String hex = fileReader.read(dataPath);
        hex = hex.replaceAll(" ", "");
        logger.info("decoding data...... [{}]", DATA_PATH);
        ProtocolDecoder decoder = new ProtocolDecoder(papers);
        Info info = decoder.decode(DataUtil.hexToBytes(hex));
        Knife knife = Knife.build();
        Object result = knife.peel(info);
        logger.info("result: {}", JSON.toJSONString(result));
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(outputPath+File.separator+"out.txt"),
                            StandardCharsets.UTF_8
                    )
            );
            writer.write(JSON.toJSONString(result));
            writer.flush();
        } finally {
            if(writer != null) {
                writer.close();
            }
        }
    }
}