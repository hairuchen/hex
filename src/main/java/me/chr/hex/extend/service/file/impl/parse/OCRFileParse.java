package me.chr.hex.extend.service.file.impl.parse;


import lombok.extern.slf4j.Slf4j;
import me.chr.hex.extend.service.file.FileParse;
import me.chr.hex.general.entity.TFile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: CHR
 * @Date: create in 2026/2/10
 **/
/**
 * 低配置：本地OCR解析实现（如Tesseract）
 * 注：实际需集成OCR引擎
 */
@Slf4j
@Service("fileParse") // 统一Bean名称：fileParse
@ConditionalOnProperty(
        prefix = "server.file.parse",
        name = "type",
        havingValue = "ocr",
        matchIfMissing = true // 配置缺失时默认注入（兜底）
)
public class OCRFileParse implements FileParse {

    @Override
    public List<String> parse(byte[] fileBytes, TFile fileEntity) {
        // 1. 实际逻辑：调用本地OCR引擎（如Tesseract）解析文件
        log.info("【OCR解析】开始解析文件，fileId: {}", fileEntity.getId());

        // 2. 示例：模拟解析结果（实际需替换为OCR SDK调用）
//        List<String> chunks = LocalOcrClient.extractTextFromFile(fileBytes, fileEntity.getFileName());
        List<String> chunks=new ArrayList<>();
        log.info("【OCR解析】完成，fileId: {}，分片数: {}", fileEntity.getId(), chunks.size());
        return chunks;
    }

    @Override
    public String getType() {
        return "ocr";
    }

}
