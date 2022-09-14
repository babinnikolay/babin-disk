package ru.babin.babindisk.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.babin.babindisk.controller.DiskController;
import ru.babin.babindisk.model.DiskItem;
import ru.babin.babindisk.model.DiskItemType;
import ru.babin.babindisk.model.dto.DiskImportRequest;
import ru.babin.babindisk.model.dto.DiskItemDto;
import ru.babin.babindisk.service.DiskService;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = DiskController.class)
class DiskWebMvcTest {

    @MockBean
    private DiskService diskService;

    @Autowired
    ObjectMapper mapper;

    private MockMvc mvc;

    private DateTimeFormatter formatter;

    private DiskImportRequest diskImportRequest;

    private DiskItemDto diskItemDto;

    @BeforeEach
    void setUp(WebApplicationContext wac) {

        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .build();

        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");

        DiskItem folder = new DiskItem();
        folder.setId("folderId");
        folder.setType(DiskItemType.FOLDER);
        List<DiskItem> items = new ArrayList<>();
        items.add(folder);

        diskImportRequest = new DiskImportRequest();
        diskImportRequest.setUpdateDate(LocalDateTime.now());
        diskImportRequest.setItems(items);

        diskItemDto = new DiskItemDto();
    }

    @Test
    void whenImportOneFolderThenGetOk() throws Exception {
        mvc.perform(post("/imports")
                .content(mapper.writeValueAsString(diskImportRequest))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
    }

    @Test
    void whenImportOneFolderFromFutureThenGetBadRequest() throws Exception {
        diskImportRequest.setUpdateDate(LocalDateTime.now().plusDays(1));
        mvc.perform(post("/imports")
                        .content(mapper.writeValueAsString(diskImportRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenDeleteWithWrongFormatDateThenBadRequest() throws Exception {
        mvc.perform(delete("/delete/folderId")
                        .flashAttr("date", "2022-07-10")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenDeleteOneFolderThenGetOk() throws Exception {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(now, ZoneOffset.UTC);
        now.atOffset(ZoneOffset.UTC);
        when(diskService.deleteByIdWithChild("folderId", localDateTime))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(null));

        mvc.perform(delete("/delete/folderId")
                        .param("date", now.toString())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void whenGetOneFolderThenGetOk() throws Exception {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(now, ZoneOffset.UTC);
        now.atOffset(ZoneOffset.UTC);
        when(diskService.findByIdWithChild("folderId"))
                .thenReturn(diskItemDto);

        diskItemDto.setDate(LocalDateTime.now().minusDays(1));
        diskItemDto.setId("folderId");
        diskItemDto.setType(DiskItemType.FOLDER);

        mvc.perform(get("/nodes/folderId")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(diskItemDto.getId()), String.class))
                .andExpect(jsonPath("$.type", is(diskItemDto.getType().toString())));
    }

}
