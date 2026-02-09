package me.chr.hex.extend.service.model;


import me.chr.hex.extend.BO.VLLMParse;
import me.chr.hex.general.entity.TFile;

import java.util.HashMap;
import java.util.List;

/**
 * @Author: CHR
 * @Date: create in 2026/2/10
 **/
public interface ParseModel {

    VLLMParse parse(String pageBase64, String frontContext, TFile fileEntity);
}
