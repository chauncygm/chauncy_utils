package cn.chauncy.base;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@type")
public abstract class BaseBean {
}
