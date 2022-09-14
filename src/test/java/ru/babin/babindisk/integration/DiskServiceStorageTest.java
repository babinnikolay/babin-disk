package ru.babin.babindisk.integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.babin.babindisk.exception.BadRequestException;
import ru.babin.babindisk.exception.NotFoundException;
import ru.babin.babindisk.model.DiskItem;
import ru.babin.babindisk.model.DiskItemType;
import ru.babin.babindisk.model.dto.DiskHistoryResponse;
import ru.babin.babindisk.model.dto.DiskImportRequest;
import ru.babin.babindisk.model.dto.DiskItemDto;
import ru.babin.babindisk.service.DiskService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(properties = {"spring.datasource.url" +
        "=jdbc:postgresql://localhost:5432/test?useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTCs",
        "spring.datasource.username=diskadmin",
        "spring.datasource.password=s4sf1yzGvf59nmVYGgNM",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.hibernate.show-sql=true",
        "spring.sql.init.mode=always"},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DiskServiceStorageTest {

    private final EntityManager em;
    private final DiskService service;


    @AfterEach
    public void cleanTable() {
        em.createQuery("delete from DiskItem ").executeUpdate();
        em.createQuery("delete from DiskItemHistory ").executeUpdate();
    }

    @Test
    void whenImportDiskItemsOneFileThenSaveItToDb() throws NotFoundException, BadRequestException {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        DiskItem item = new DiskItem();
        item.setId("newId");
        item.setSize(1L);
        item.setType(DiskItemType.FILE);
        item.setUrl("/url/1");
        item.setParentId(null);

        List<DiskItem> items = new ArrayList<>();
        items.add(item);
        DiskImportRequest request = new DiskImportRequest();
        request.setUpdateDate(now);
        request.setItems(items);

        service.importDiskItems(request);

        TypedQuery<DiskItem> query = em.createQuery("SELECT di FROM DiskItem di WHERE di.id = :id",
                DiskItem.class);
        DiskItem diskItem = query.setParameter("id", "newId").getSingleResult();

        assertEquals( "newId", diskItem.getId());
        assertEquals(now, diskItem.getDate());
        assertEquals(DiskItemType.FILE, diskItem.getType());
        assertEquals(1L, diskItem.getSize());
        assertEquals(null, diskItem.getParentId());
        assertEquals("/url/1", diskItem.getUrl());
    }

    @Test
    void whenImportDiskItemsFolderThenSaveItToDb() throws NotFoundException, BadRequestException {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        DiskItem folder = new DiskItem();
        folder.setId("folderId");
        folder.setType(DiskItemType.FOLDER);

        DiskItem file = new DiskItem();
        file.setId("newId");
        file.setSize(1L);
        file.setType(DiskItemType.FILE);
        file.setUrl("/url/1");
        file.setParentId("folderId");

        List<DiskItem> items = new ArrayList<>();
        items.add(folder);
        items.add(file);
        DiskImportRequest request = new DiskImportRequest();
        request.setUpdateDate(now);
        request.setItems(items);

        service.importDiskItems(request);

        TypedQuery<DiskItem> query = em.createQuery("SELECT di FROM DiskItem di WHERE di.id = :id",
                DiskItem.class);
        DiskItem folderItem = query.setParameter("id", "folderId").getSingleResult();

        assertEquals("folderId", folderItem.getId());
        assertEquals(DiskItemType.FOLDER, folderItem.getType());
        assertEquals(now, folderItem.getDate());
        assertEquals(null, folderItem.getParentId());
        assertEquals(null, folderItem.getUrl());
    }

    @Test
    void whenImportDiskFilesThenGetFolderSize() throws NotFoundException, BadRequestException {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        DiskItem folder = new DiskItem();
        folder.setId("folderId");
        folder.setType(DiskItemType.FOLDER);

        DiskItem file = new DiskItem();
        file.setId("newId");
        file.setSize(1L);
        file.setType(DiskItemType.FILE);
        file.setUrl("/url/1");
        file.setParentId("folderId");

        DiskItem file2 = new DiskItem();
        file2.setId("newId2");
        file2.setSize(11L);
        file2.setType(DiskItemType.FILE);
        file2.setUrl("/url/1");
        file2.setParentId("folderId");

        List<DiskItem> items = new ArrayList<>();
        items.add(folder);
        items.add(file);
        items.add(file2);
        DiskImportRequest request = new DiskImportRequest();
        request.setUpdateDate(now);
        request.setItems(items);

        service.importDiskItems(request);

        DiskItem folderItem = service.findByIdWithChild("folderId");

        assertEquals("folderId", folderItem.getId());
        assertEquals(DiskItemType.FOLDER, folderItem.getType());
        assertEquals(12L, folderItem.getSize());
    }

    @Test
    void whenGetNonExistsFolderThenNotFoundException() throws NotFoundException, BadRequestException {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        DiskItem folder = new DiskItem();
        folder.setId("folderId");
        folder.setType(DiskItemType.FOLDER);

        DiskItem file = new DiskItem();
        file.setId("newId");
        file.setSize(1L);
        file.setType(DiskItemType.FILE);
        file.setUrl("/url/1");
        file.setParentId("folderId");

        DiskItem file2 = new DiskItem();
        file2.setId("newId2");
        file2.setSize(11L);
        file2.setType(DiskItemType.FILE);
        file2.setUrl("/url/1");
        file2.setParentId("folderId");

        List<DiskItem> items = new ArrayList<>();
        items.add(folder);
        items.add(file);
        items.add(file2);
        DiskImportRequest request = new DiskImportRequest();
        request.setUpdateDate(now);
        request.setItems(items);

        service.importDiskItems(request);
        service.deleteByIdWithChild(folder.getId(), now.plus(1, ChronoUnit.SECONDS));

        assertThrows(NotFoundException.class, () -> service.findByIdWithChild("folderId"));
    }

    @Test
    void whenGetFolderThenReturnFoldersWithChild() throws NotFoundException, BadRequestException {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        DiskItem folder = new DiskItem();
        folder.setId("folderId");
        folder.setType(DiskItemType.FOLDER);

        DiskItem file = new DiskItem();
        file.setId("newId");
        file.setSize(1L);
        file.setType(DiskItemType.FILE);
        file.setUrl("/url/1");
        file.setParentId("folderId");

        DiskItem file2 = new DiskItem();
        file2.setId("newId2");
        file2.setSize(11L);
        file2.setType(DiskItemType.FILE);
        file2.setUrl("/url/1");
        file2.setParentId("folderId");

        List<DiskItem> items = new ArrayList<>();
        items.add(folder);
        items.add(file);
        items.add(file2);
        DiskImportRequest request = new DiskImportRequest();
        request.setUpdateDate(now);
        request.setItems(items);

        service.importDiskItems(request);

        DiskItemDto headDto = service.findByIdWithChild(folder.getId());
        assertEquals(2, headDto.getChildren().size());
        assertEquals(DiskItemType.FILE, headDto.getChildren().get(0).getType());
        assertEquals(DiskItemType.FILE, headDto.getChildren().get(1).getType());
    }

    @Test
    void whenGetHistoryByIdThenReturnHistory() throws NotFoundException, BadRequestException {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).minusSeconds(10);

        DiskItem folder = new DiskItem();
        folder.setId("folderId");
        folder.setType(DiskItemType.FOLDER);

        List<DiskItem> items = new ArrayList<>();
        items.add(folder);
        DiskImportRequest request = new DiskImportRequest();
        request.setUpdateDate(now);
        request.setItems(items);

        service.importDiskItems(request);

        request.setUpdateDate(now.plusSeconds(1));
        service.importDiskItems(request);

        DiskHistoryResponse history = service.getHistoryByDiskItemId(folder.getId(), null, null);

        assertEquals(2, history.getItems().size());
        assertEquals(now, history.getItems().get(0).getDate());
        assertEquals(now.plusSeconds(1), history.getItems().get(1).getDate());
    }
}