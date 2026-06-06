package com.qiujie.unit;

import com.qiujie.entity.ValidateCode;
import com.qiujie.util.ValidateCodeUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidateCodeUtil unit tests")
class ValidateCodeUtilTest {

    @Test
    @DisplayName("generate — returns image and 4-digit code")
    void shouldGenerateValidateCode() {
        ValidateCode vc = ValidateCodeUtil.generateValidateCode();

        assertNotNull(vc);
        assertNotNull(vc.getCode());
        assertEquals(4, vc.getCode().length());
        for (char c : vc.getCode().toCharArray()) {
            assertTrue(Character.isDigit(c));
        }

        BufferedImage image = vc.getImage();
        assertNotNull(image);
        assertEquals(60, image.getWidth());
        assertEquals(20, image.getHeight());

        assertNotNull(vc.getExpireTime());
    }

    @Test
    @DisplayName("generate — produces different codes on subsequent calls")
    void shouldGenerateDifferentCodes() {
        ValidateCode vc1 = ValidateCodeUtil.generateValidateCode();
        ValidateCode vc2 = ValidateCodeUtil.generateValidateCode();
        assertNotNull(vc1.getCode());
        assertNotNull(vc2.getCode());
    }

    @Test
    @DisplayName("isExpire — returns false for future time")
    void shouldNotBeExpired() {
        LocalDateTime future = LocalDateTime.now().plusMinutes(5);
        assertFalse(ValidateCodeUtil.isExpire(future));
    }

    @Test
    @DisplayName("isExpire — returns true for past time")
    void shouldBeExpired() {
        LocalDateTime past = LocalDateTime.now().minusSeconds(1);
        assertTrue(ValidateCodeUtil.isExpire(past));
    }

    @Test
    @DisplayName("expireIn — constant is 60 seconds")
    void shouldHaveCorrectExpireIn() {
        assertEquals(60, ValidateCodeUtil.expireIn);
    }
}
