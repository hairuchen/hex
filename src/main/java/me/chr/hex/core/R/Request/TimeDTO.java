package me.chr.hex.core.R.Request;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.chr.hex.core.log.Loggable;


import java.time.LocalDateTime;

/**
 * @Author: CHR
 * @Date: create in 2026/3/4
 **/
@Data
@ToString
@Schema(name = "TimeDTO", description = "时间查询对象 DTO")
@NoArgsConstructor
public class TimeDTO implements Loggable {

    @Schema(description = "开始时间", example = "2026-01-01 00:00:00")
    @PastOrPresent(message = "开始时间不能是未来时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2026-03-04 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    @AssertTrue(message = "开始时间不能晚于结束时间")
    private boolean isStartTimeValid() {
        if (startTime != null && endTime != null) {
            // 如果 开始时间 > 结束时间，则返回 false (校验失败)
            return !startTime.isAfter(endTime);
        }
        // 如果有一个为空，就不做跨字段校验 (单独的非空校验如果需要可以另加)
        return true;
    }
}
