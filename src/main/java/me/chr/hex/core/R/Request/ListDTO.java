package me.chr.hex.core.R.Request;


import com.baomidou.mybatisplus.core.metadata.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.chr.hex.core.log.Loggable;

import java.util.List;

/**
 * @Author: CHR
 * @Date: create in 2026/3/4
 **/
@Data
@ToString
@Schema(name = "ListDTO", description = "列表查询对象")
@NoArgsConstructor
public class ListDTO implements Loggable {
    @Schema(description = "当前页")
    private Integer current=1;
    @Schema(description = "每页大小")
    private Integer size=10;

    @Schema(description = "排序对象列表")
    private List<OrderItem> orders;
}
