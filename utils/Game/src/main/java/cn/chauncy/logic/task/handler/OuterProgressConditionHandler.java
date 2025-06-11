package cn.chauncy.logic.task.handler;

import cn.chauncy.logic.player.Player;
import cn.chauncy.template.bean.CfgCondition;

/** 从外部获取进度的条件处理器 */
public abstract class OuterProgressConditionHandler extends ConditionHandler {

    public OuterProgressConditionHandler() {
        super(true);
    }

    /**
     * 获取当前状态的进度
     * 比如：当前等级，当前装备紫色装备数量等从外部玩家数据中获取
     * 或者从玩家累积的任务数据中统计进度，如：历史击杀多少只精英怪
     */
    protected abstract int getCurrentStateProgress(Player player, CfgCondition condition);


}
