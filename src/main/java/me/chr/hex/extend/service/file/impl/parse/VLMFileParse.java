package me.chr.hex.extend.service.file.impl.parse;


/**
 * @Author: CHR
 * @Date: create in 2026/2/10
 **/

import lombok.extern.slf4j.Slf4j;
import me.chr.hex.extend.BO.VLLMParse;
import me.chr.hex.extend.service.file.FileParse;
import me.chr.hex.extend.service.model.ParseModel;
import me.chr.hex.general.entity.TFile;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * 高配置：视觉语言模型（VLLM）解析实现
 * 注：实际需集成VLLM SDK（如通义千问VLM/LLaVA）
 */
@Slf4j
@Service("fileParse") // 统一Bean名称：fileParse
@ConditionalOnProperty(
        prefix = "server.file.parse",
        name = "type",
        havingValue = "vlm",
        matchIfMissing = false // 配置缺失时不注入
)
public class VLMFileParse implements FileParse {

    @Autowired
    private ParseModel parseModel;

    @Override
    public List<String> parse(byte[] fileBytes, TFile fileEntity) {
        log.info("【{}解析器】开始解析文件,fileId: {},fileType:{}",this.getType(), fileEntity.getId(),fileEntity.getFileType());

//        if (fileEntity.getFileType().equals())
        //每页图像集合
        List<String> base64List = new ArrayList<>();
        //all of chunk
        List<String> chunk=new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(fileBytes)){
            int pageCount = document.getNumberOfPages();
            PDFRenderer renderer = new PDFRenderer(document);
            for (int i = 0; i < pageCount; i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, 300);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "PNG", baos);
                String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
                base64List.add("data:image/png;base64," + base64);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String frontContext="";
        for (String pageBase64:base64List){
            VLLMParse vllmParse=parseModel.parse(pageBase64,frontContext,fileEntity);
            frontContext=vllmParse.getUnprocessed();
            chunk.addAll(vllmParse.getChunks());
        }

        log.info("【VLLM解析】完成，fileId: {}，分片数: {}", fileEntity.getId(), chunk.size());
        return chunk;
    }

    @Override
    public String getType() {
        return "vllm";
    }

    //    private List<String> parsePDF(byte[] fileBytes){
//        List<String> chunkList = new ArrayList<>();
//        try (InputStream inputStream = new ByteArrayInputStream(fileBytes);
//             PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) { // 👈 关键修改在这里！
//
//            if (document.isEncrypted()) {
//                logger.warn("【解析PDF】该PDF文件已加密，跳过解析");
//                return chunkList;
//            }
//
//            PDFTextStripper stripper = new PDFTextStripper();
//            String text = stripper.getText(document);
//
//            if (text == null || text.trim().isEmpty()) {
//                logger.warn("【解析PDF】提取的文本为空");
//                return chunkList;
//            }
//
//            // 按段落分块（两个及以上换行符分隔）
//            String[] paragraphs = text.split("\\n\\s*\\n");
//            for (String paragraph : paragraphs) {
//                String cleanParagraph = paragraph.replaceAll("\\s+", " ").trim();
//                if (!cleanParagraph.isEmpty()) {
//                    chunkList.add(cleanParagraph);
//                }
//            }
//
//            logger.info("【解析PDF】成功提取 {} 段文本", chunkList.size());
//
//        } catch (IOException e) {
//            logger.error("【解析PDF】PDF解析失败", e);
//            throw new RuntimeException("PDF解析失败", e);
//        }
//
//        return chunkList;
//    }
//
//    private List<String> parseTXT(byte[] fileBytes){
//        List<String> chunkList = new ArrayList<>();
//        try {
//            // 字节转字符串（UTF-8编码）
//            String content = new String(fileBytes, StandardCharsets.UTF_8);
//            // 按换行符拆分，过滤空行
//            String[] lines = content.split("\\n");
//            for (String line : lines) {
//                String trimLine = line.trim();
//                if (!trimLine.isEmpty()) {
//                    chunkList.add(trimLine);
//                }
//            }
//        } catch (Exception e) {
//            logger.error("【解析TXT】文本提取失败", e);
//            throw new RuntimeException("TXT解析失败", e);
//        }
//        return chunkList;
//    }
}
