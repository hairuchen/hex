package me.chr.hex.extend.service.model;


import me.chr.hex.extend.BO.AbstractEntityNode;

import java.util.List;
import java.util.Map;

/**
 * @Author: CHR
 * @Date: create in 2026/2/6
 **/
public interface ENode {
    void setENode(Map<String, Object> map);
    List<AbstractEntityNode> getENode();
}
