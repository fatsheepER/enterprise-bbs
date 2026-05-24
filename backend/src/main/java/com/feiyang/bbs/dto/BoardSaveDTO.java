package com.feiyang.bbs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BoardSaveDTO {
    @NotBlank(message = "版块名称不能为空")
    @Size(max = 50, message = "版块名称长度不能超过 50")
    private String name;

    private String description;
    private String colorHex;
    private Integer sortOrder;
    private Integer status;
}
