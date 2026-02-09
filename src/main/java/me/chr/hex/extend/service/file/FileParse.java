package me.chr.hex.extend.service.file;


import me.chr.hex.general.entity.TFile;

import java.util.List;

/**
 * @Author: CHR
 * @Date: create in 2026/2/10
 **/
public interface FileParse {

    /**
     * 解析文件并返回文本分片
     * @param fileBytes 文件字节流
     * @param fileEntity 文件元数据
     * @return 文本分片列表
     */
    List<String> parse(byte[] fileBytes, TFile fileEntity);

    /**
     * 获取当前解析器的类型（用于配置区分）
     * @return 解析器类型（如"vllm"/"ocr"）
     */
    String getType();
}
