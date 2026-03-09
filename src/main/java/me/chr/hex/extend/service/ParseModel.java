package me.chr.hex.extend.service;


import me.chr.hex.extend.BO.EntityNode;
import me.chr.hex.extend.BO.ImageChunkParseResult;

import java.util.List;

/**
 * @Author: CHR
 * @Date: create in 2026/2/10
 **/
public interface ParseModel {

    ImageChunkParseResult base64ToChunks(String pageBase64, String frontContext);

    List<String> chunkToEntity(String chunk);
}
