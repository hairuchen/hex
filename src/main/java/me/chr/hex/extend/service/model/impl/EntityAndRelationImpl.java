package me.chr.hex.extend.service.model.impl;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.chr.hex.extend.BO.AbstractEntityNode;
import me.chr.hex.extend.BO.AbstractRelationEdge;
import me.chr.hex.extend.service.model.ENode;
import me.chr.hex.extend.service.model.REdge;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Author: CHR
 * @Date: create in 2026/2/6
 **/
@Service
public class EntityAndRelationImpl implements ENode, REdge {

    private final ObjectMapper objectMapper=new ObjectMapper();

    private List<AbstractEntityNode> abstractEntityNodeList;
    private List<AbstractRelationEdge> abstractRelationEdgeList;

    @Override
    public void setENode(Map<String, Object> map) {
        // 解析Map为V1实体列表，并缓存到成员变量
        if (map != null) {
            this.abstractEntityNodeList = objectMapper.convertValue(
                    map.get("entities"),
                    new TypeReference<List<AbstractEntityNode>>() {}
            );
        }else{
            this.abstractEntityNodeList =List.of();
        }
    }

    @Override
    public List<AbstractEntityNode> getENode() {
        return this.abstractEntityNodeList;
    }

    @Override
    public void setRx(Map<String, Object> map) {
        // 解析Map为V1关系列表，并缓存到成员变量
        if (map != null) {
            this.abstractRelationEdgeList = objectMapper.convertValue(
                    map.get("relations"),
                    new TypeReference<List<AbstractRelationEdge>>() {}
            );
        }else{
            this.abstractRelationEdgeList = List.of();
        }
    }

    @Override
    public List<AbstractRelationEdge> getRx() {
        return this.abstractRelationEdgeList;
    }
}
