package ${package.Entity};

<#list table.importPackages as pkg>
import ${pkg};
</#list>
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import java.io.Serial;
import lombok.ToString;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


/**
 * <p>
 * ${table.comment!}
 * </p>
 *
 * @author ${author}
 * @since ${date}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@TableName("${table.name?replace('_default$', '', 'r')}")
@Schema(name = "${entity}", description = "${table.comment!}")
@JsonPropertyOrder({
<#list table.fields as field>
    "${field.propertyName}"<#if field_has_next>, </#if>
</#list>
})
public class ${entity} implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

<#-- ----------  BEGIN 字段循环遍历  ---------->
<#list table.fields as field>

    <#-- 字段注释 -->
    /**
     * ${field.comment!}
     */
    @Schema(description = "${field.comment!}")
    <#-- 只有 id 字段使用 @TableId 注解 -->
    <#if field.keyFlag && field.propertyName == "id">
           <#assign keyPropertyName="${field.propertyName}"/>
           <#if field.idType??>
    @TableId(value = "${field.annotationColumnName}", type = IdType.${field.idType})
            <#else>
    @TableId(value = "${field.annotationColumnName}")
           </#if>
    <#else>
    @TableField("${field.annotationColumnName}")
    </#if>
    <#if field.propertyType == 'LocalDateTime' || field.propertyType == 'Date'>
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    </#if>
    private ${field.propertyType} ${field.propertyName};

</#list>
<#-- ----------  END 字段循环遍历  ---------->


}