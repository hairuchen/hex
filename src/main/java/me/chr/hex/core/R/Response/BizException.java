package me.chr.hex.core.R.Response;

import lombok.Getter;

/**
 * @Author: CHR
 * @Date: create in 2025/11/28
 */
@Getter
public class BizException extends RuntimeException{

    public BizException(String msg) {
        super(msg);
    }

}
