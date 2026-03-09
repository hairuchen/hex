package me.chr.hex.extend.Enum;


import lombok.Getter;

/**
 * @Author: CHR
 * @Date: create in 2026/2/13
 **/
@Getter
public enum VLMFileType {
//    /**
//     * 可分页类型：具有页面概念，可逐页转图像供 VLM 理解
//     */
//    PAGINATED("可分页"),
//    /**
//     * 不可分页类型：无页面结构（如纯文本），不适用于 VLM 视觉解析
//     */
//    NON_PAGINATED("不可分页"),
//    /**
//     * 其他类型：未知格式、不支持格式、或特殊二进制文件
//     * 如 EXE、ZIP、ENCRYPTED PDF、DWG 等 → 应跳过或报错
//     */
//    OTHER("其他/不支持")
//    ;
//
//    private final String type;
//
//    VLMFileType(String type) {
//        this.type=type;
//    }
//
//    /**
//     * 根据文件名判断 VLM 文件类型
//     */
//    public static VLMFileType fromFileType(String filetype) {
//        if (filename == null || filename.isEmpty()) {
//            return OTHER;
//        }
//
//        int lastDot = filename.lastIndexOf('.');
//        if (lastDot <= 0 || lastDot == filename.length() - 1) {
//            return OTHER; // 无扩展名 → 视为未知
//        }
//
//        String ext = filename.substring(lastDot + 1).toLowerCase();
//
//        if (PAGINATED_EXTENSIONS.contains(ext)) {
//            return PAGINATED;
//        } else if (NON_PAGINATED_EXTENSIONS.contains(ext)) {
//            return NON_PAGINATED;
//        } else {
//            return OTHER; // 未知扩展名
//        }
//    }

}
