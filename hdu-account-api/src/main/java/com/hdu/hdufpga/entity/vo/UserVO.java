package com.hdu.hdufpga.entity.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hdu.hdufpga.cvt.Duration2LongConverter;
import com.hdu.hdufpga.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import javax.persistence.Convert;
import java.time.Duration;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserVO extends BaseEntity {
    @ExcelProperty("学号")
    private String username;
    private String password;
    @ExcelProperty("姓名")
    private String realName;
    private Long activeTime; // todo: undocumented. ???
    private Integer userDepartmentId;
    private String userDepartmentName;
    private Integer userRoleId;
    private String userRoleName;
    private Integer privilegeLevel;
    @Convert(converter = Duration2LongConverter.class)
    /// total active time of the user. Guaranteed not null.
    private Duration totActiveTime;
    ///  total experiment count of the user. Guaranteed not null.
    private Integer totExpCnt;
}
