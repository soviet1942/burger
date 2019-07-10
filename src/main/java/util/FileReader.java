package util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/10 11:04
 * @Description:
 */
public class FileReader {

    private static final Logger logger = LoggerFactory.getLogger(FileReader.class);

    public static void main(String[] args) {
        JSONObject jsonObject = transYaml2Json("E:\\motor\\motorbang\\src\\main\\resources\\config\\burger.yaml");
        System.out.println(jsonObject.getJSONObject("download").getString("maxRetry"));
    }

    public static JSONObject transYaml2Json(String filePath) {
        Yaml yaml = new Yaml();
        Map<String, Object> yamlObject = null;
        try {
            yamlObject = (Map<String, Object>) yaml.load(new FileInputStream(new File(filePath)));
        } catch (FileNotFoundException e) {
            logger.error("yaml文件读取错误, cause: {}", ExceptionUtils.getMessage(e.getCause()));
        }
        JSONObject jsonObject = new JSONObject(yamlObject);
        return jsonObject;
    }
}
