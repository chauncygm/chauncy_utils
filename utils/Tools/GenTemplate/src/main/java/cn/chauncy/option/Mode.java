package cn.chauncy.option;

public enum Mode {

    /** 导出客户端 */
    CLIENT("c"),

    /** 导出服务器 */
    SERVER("s");

    public final String value;

    Mode(String value) {
        this.value = value;
    }
}
