package cn.chauncy;

import cn.chauncy.validator.ExcelValidator;
import lombok.Data;

import java.nio.file.Path;
import java.util.ArrayList;
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

    private Path outputPath;
    private Path jsonOutputPath;

    private ExportOption(Builder builder) {
        this.excelPath = builder.excelPath;
        this.exportIds = builder.exportIds;
        this.mode = builder.mode;
        this.validators = builder.validators;
        this.outputPath = builder.outputPath;
        this.jsonOutputPath = builder.jsonOutputPath;
    }

    static class Builder {

        private Path excelPath;
        private Mode mode;
        private List<Integer> exportIds = List.of();
        private List<ExcelValidator> validators = List.of();
        private Path outputPath;
        private Path jsonOutputPath;

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

        public Builder outputPath(Path outputPath) {
            this.outputPath = outputPath;
            return this;
        }

        public Builder jsonOutputPath(Path jsonOutputPath) {
            this.jsonOutputPath = jsonOutputPath;
            return this;
        }

        public ExportOption build() {
            return new ExportOption(this);
        }
    }

}
