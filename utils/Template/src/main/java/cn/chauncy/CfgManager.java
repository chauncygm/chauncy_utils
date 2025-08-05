package cn.chauncy;

import cn.chauncy.template.CfgDefine;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.bson.RawBsonDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class CfgManager extends CfgDefine {

    private static final Logger logger = LoggerFactory.getLogger(CfgManager.class);

    private static final String tablePath = "./res/config/bson/";

    public static CfgManager INSTANCE = new CfgManager();

    @Getter
    private String version;

    public void init() throws IOException {
        super.init();
        File versionFile = FileUtils.getFile(tablePath + "/version.txt");
        this.version = FileUtils.readFileToString(versionFile, "utf-8");
        logger.info("config version: {}", version);
    }

    @Override
    protected void initLoad(String tableName) throws IOException {
        String jsonFile = tablePath + tableName + ".bson";
        byte[] bytes = FileUtils.readFileToByteArray(FileUtils.getFile(jsonFile));
        RawBsonDocument bson = new RawBsonDocument(bytes);
        int loadSize = reloadCfg(tableName, bson.toJson());
        logger.info("load table {}, size: {}", tableName, loadSize);
        if (loadSize <= 0) {
            throw new IOException("load table " + tableName + " size error");
        }
    }
}
