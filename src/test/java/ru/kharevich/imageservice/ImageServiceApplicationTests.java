package ru.kharevich.imageservice;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Disabled for CI - requires database configuration")
class ImageServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
