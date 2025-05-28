package cn.chauncy.dao.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;

public class BaseEntity {

    @TableField(fill = FieldFill.INSERT)
    private long createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private long updateTime;

    @TableLogic(value = "0", delval = "1")
    private boolean deleted;
}
