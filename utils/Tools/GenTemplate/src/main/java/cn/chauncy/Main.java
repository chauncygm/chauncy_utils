package cn.chauncy;

import cn.chauncy.struct.TableFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        if (args.length < 2) {
            logger.error("参数错误: 配置目录，生成目标目录");
            return;
        }

        String cfgPath = args[0];
        String targetPath = args[1];

        List<Integer> exportIds = getInput();

        long t1 = System.currentTimeMillis();
        List<TableFile> list = getFiles(cfgPath, exportIds);
        if (list.isEmpty()) {
            logger.error("没有找到需要导出的文件！");
            return;
        }

        ReadExcel.INSTANCE.read(list);
        long t2 = System.currentTimeMillis();
        logger.info("=== 读取配置成功, 数量: {} 总耗时: {}ms！ ===", list.size(), (t2 - t1));



        logger.info("正在准备写入数据...");
        GenerateFile.INSTANCE.write(targetPath, list, exportIds.isEmpty());
        t2 = System.currentTimeMillis();
        logger.info("=== 结束！数量: {}, 总耗时: {}毫秒！ ===", list.size(), (t2 - t1));
    }

    private static void removeSkipTable(List<TableFile> list) {
        list.removeIf(TableFile::isNotSrv);
    }

    private static List<Integer> getInput() {
        logger.info("输入要导出的表ID(导全表直接回车，多个表以空格分隔): ");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            List<Integer> list = new ArrayList<>();
            String str = br.readLine().trim();
            if (str.isEmpty() || str.equals("0"))
                return list;

            String[] ids = str.split(" ");
            for (String tmp : ids) {
                list.add(Integer.parseInt(tmp));
            }
            return list;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<TableFile> getFiles(String cfgPath, List<Integer> ids) {
        File dir = new File(cfgPath);
        if (!dir.exists() || !dir.isDirectory()) {
            logger.error("配置目录不存在：{}", cfgPath);
            System.exit(-1);
        }

        List<TableFile> list = new ArrayList<>();
        File[] files = dir.listFiles((file) -> {
            if (!file.isFile() || !file.canRead()) {
                return false;
            }
            String filename = file.getName();
            if (filename.startsWith("~")) {
                return false;
            }
            return filename.endsWith(".xlsx") || filename.endsWith(".xlsm");
        });

        for (File f : Objects.requireNonNull(files)) {

            String filename = f.getName();
            int index = filename.indexOf(".");
            String tableName = filename.substring(0, index);

            try {
                TableFile tableFile = new TableFile(f.getAbsolutePath(), tableName);
                if (!ids.isEmpty() && !ids.contains(tableFile.getId()))
                    continue;


                // 检查是否已经包含此表
                for (TableFile t : list) {
                    if (t.getId() == tableFile.getId() || t.getName().equalsIgnoreCase(tableFile.getName())) {
                        logger.error("发现重复的配置表,id : {}, 表名: {}", tableFile.getId(), tableFile.getName());
                        System.exit(-1);
                    }
                }

                // 添加
                list.add(tableFile);
            } catch (Exception e) {
                if (ids.isEmpty()) {
                    logger.error("配置目录中发现错误的文件：{}", filename);
                    System.exit(-1);
                }
            }
        }
        return list;
    }
}
