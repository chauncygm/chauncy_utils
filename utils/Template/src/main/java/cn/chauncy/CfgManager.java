package cn.chauncy;

import cn.chauncy.template.CfgDefine;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CfgManager extends CfgDefine {

    private static final Logger logger = LoggerFactory.getLogger(CfgManager.class);

    private static final String tablePath = "./res/config/";

    public static CfgManager INSTANCE = new CfgManager();

    @Override
    protected void initLoad(String tableName) throws IOException {
        String jsonFile = tablePath + tableName + ".json";
        String jsonContent = FileUtils.readFileToString(FileUtils.getFile(jsonFile), "utf-8");
        int loadSize = reloadCfg(tableName, jsonContent);
        logger.info("load table {}, size: {}", tableName, loadSize);
        if (loadSize <= 0) {
            throw new IOException("load table " + tableName + " size error");
        }
    }
}
