package com.hdu.hdufpga.entity.ro;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
public class VerificationCodeRO {
    /**
     * 宽度
     */
    @Builder.Default
    Integer width = 200;
    /**
     * 长度
     */
    @Builder.Default
    Integer height = 50;

    /**
     * 干扰线长度
     */
    @Builder.Default
    Integer thickness = 4;

    /**
     * 验证码长度
     */
    @Builder.Default
    Integer numberLength = 1;
}
