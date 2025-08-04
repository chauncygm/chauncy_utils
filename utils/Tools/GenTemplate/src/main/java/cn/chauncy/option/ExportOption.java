package cn.chauncy.option;

import cn.chauncy.validator.ExcelValidator;
import lombok.Data;

import java.nio.file.Path;
import java.util.List;

@Data
public class ExportOption {

    /** excel目录路径 */
    private Path excelPath;
    /** 需要到处的表格id列表，空表示全部 */
    private List<Integer> exportIds;
    /** 导出模式 */
    private Mode mode;
    /** 表格校验器 */
    private List<ExcelValidator> validators;

    private Path classOutputPath;
    private Path jsonOutputPath;
    private String classOutPackage;

    private ExportOption(Builder builder) {
        this.excelPath = builder.excelPath;
        this.exportIds = builder.exportIds;
        this.mode = builder.mode;
        this.validators = builder.validators;
        this.classOutputPath = builder.classOutputPath;
        this.jsonOutputPath = builder.jsonOutputPath;
        this.classOutPackage = builder.classOutPackage;
    }

    public static class Builder {

        private Path excelPath;
        private Mode mode;
        private List<Integer> exportIds = List.of();
        private List<ExcelValidator> validators = List.of();
        private Path classOutputPath;
        private Path jsonOutputPath;
        private String classOutPackage;

        public Builder excelPath(Path excelPath) {
            this.excelPath = excelPath;
            return this;
        }

        public Builder exportIds(List<Integer> exportIds) {
            this.exportIds = exportIds;
            return this;
        }

        public Builder mode(Mode mode) {
            this.mode = mode;
            return this;
        }

        public Builder validators(List<ExcelValidator> validators) {
            this.validators = validators;
            return this;
        }

        public Builder classOutputPath(Path classOutputPath) {
            this.classOutputPath = classOutputPath;
            return this;
        }

        public Builder jsonOutputPath(Path jsonOutputPath) {
            this.jsonOutputPath = jsonOutputPath;
            return this;
        }

        public Builder classOutPackage(String classOutPackage) {
            this.classOutPackage = classOutPackage;
            return this;
        }

        public ExportOption build() {
            return new ExportOption(this);
        }
    }

}
