package me.chr.hex.extend.BO;


import lombok.Data;

import java.util.List;

/**
 * @Author: CHR
 * @Date: create in 2026/2/12
 **/
@Data
public class VLLMParse {

    private List<String> chunks;

    private String unprocessed;
}
