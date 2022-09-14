package ru.babin.babindisk;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.babin.babindisk.controller.DiskController;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BabinDiskApplicationTests {

    @Autowired
    private DiskController controller;

    @Test
    void contextLoads() {
        assertThat(controller).isNotNull();
    }

}
