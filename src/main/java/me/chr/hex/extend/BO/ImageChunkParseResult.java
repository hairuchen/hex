package me.chr.hex.extend.BO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: CHR
 * @Date: create in 2026/2/12
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageChunkParseResult {

    private List<String> chunks;

    private String unprocessed;
}
