package cn.chauncy.behavior_tree;

public enum Status {

    /** 新创建状态 */
    NEW(0),
    /** 运行状态 */
    RUNNING(1),
    /** 执行成功 */
    SUCCESS(2),
    /** 执行失败 */
    FAILURE(3);

    final int value;

    Status(int value) {
        this.value = value;
    }

    public boolean isComplete() {
        return value == SUCCESS.value || value == FAILURE.value;
    }

    public boolean isRunning() {
        return this == RUNNING;
    }

    public Status invert() {
        if (!isComplete()) {
            throw new IllegalStateException("Not complete status");
        }
        return value == SUCCESS.value ? FAILURE : SUCCESS;
    }

}
