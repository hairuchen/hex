package me.chr.hex.extend.service.model;


import me.chr.hex.extend.BO.AbstractRelationEdge;

import java.util.List;
import java.util.Map;

/**
 * @Author: CHR
 * @Date: create in 2026/2/6
 **/
public interface REdge {
    void setRx(Map<String, Object> map);
    List<AbstractRelationEdge> getRx();
}
