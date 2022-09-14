package ru.babin.babindisk.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.babin.babindisk.exception.BadRequestException;
import ru.babin.babindisk.exception.NotFoundException;
import ru.babin.babindisk.model.DiskItem;
import ru.babin.babindisk.model.DiskItemHistory;
import ru.babin.babindisk.model.DiskItemType;
import ru.babin.babindisk.model.dto.DiskHistoryResponse;
import ru.babin.babindisk.model.dto.DiskResponseStatus;
import ru.babin.babindisk.model.dto.DiskImportRequest;
import ru.babin.babindisk.model.dto.DiskItemDto;
import ru.babin.babindisk.repository.DiskItemHistoryRepository;
import ru.babin.babindisk.repository.DiskItemRepository;
import ru.babin.babindisk.service.DiskService;
import ru.babin.babindisk.service.impl.DiskServiceImpl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiskServiceTest {

    private DiskService diskService;

    @Mock
    private DiskItemRepository diskItemRepositoryStub;
    @Mock
    private DiskItemHistoryRepository diskItemHistoryRepositoryStub;
    @Mock
    private DiskItem itemFileStub;
    @Mock
    private DiskItem itemFileStub2;
    @Mock
    private DiskItem itemFolderStub;
    @Mock
    private DiskItem itemFolderStub2;
    @Mock
    private DiskItemDto itemFolderDtoStub;
    @Mock
    private DiskItemDto itemFileDtoStub;

    @BeforeEach
    void setUp() {
        diskService = new DiskServiceImpl(diskItemRepositoryStub, diskItemHistoryRepositoryStub);
    }

    @Test
    void whenChangeItemTypeThenThrowBadRequestException() {
        DiskImportRequest importRequest = new DiskImportRequest();
        List<DiskItem> items = new ArrayList<>();

        when(itemFileStub.getType()).thenReturn(DiskItemType.FILE);
        when(itemFileStub.getSize()).thenReturn(1L);
        when(itemFolderStub.getType()).thenReturn(DiskItemType.FOLDER);

        itemFolderStub.setId(itemFileStub.getId());

        List<DiskItem> stubList = new ArrayList<>();
        stubList.add(itemFolderStub);

        when(diskItemRepositoryStub.findAllById(anyCollection())).thenReturn(stubList);

        items.add(itemFileStub);

        importRequest.setItems(items);

        BadRequestException e = assertThrows(BadRequestException.class, () -> diskService.importDiskItems(importRequest));
        assertEquals(String.format("Item with id %s cannot change type", itemFileStub.getId()), e.getMessage());
    }

    @Test
    void whenParentIsFileThenThrowBadRequestException() {

        String itemFolderStubId = "UUID.randomUUID()";
        List<DiskItem> stubList = new ArrayList<>();
        stubList.add(itemFolderStub);
        Set<DiskItem> wrongParents = new HashSet<>();
        wrongParents.add(itemFolderStub);

        when(itemFolderStub.getSize()).thenReturn(null);

        when(itemFolderStub.getId()).thenReturn(itemFolderStubId);
        when(itemFolderStub.getType()).thenReturn(DiskItemType.FOLDER);

        when(diskItemRepositoryStub.findAllById(anyCollection())).thenReturn(stubList);
        when(diskItemRepositoryStub.findWrongParents()).thenReturn(wrongParents);

        DiskImportRequest importRequest = new DiskImportRequest();
        List<DiskItem> items = new ArrayList<>();
        items.add(itemFolderStub);
        importRequest.setItems(items);

        BadRequestException e = assertThrows(BadRequestException.class, () -> diskService.importDiskItems(importRequest));
        assertEquals(String.format("Items with id %s have wrong parents", itemFolderStub.getId()), e.getMessage());
    }

    @Test
    void whenUrlFolderIsNotNullThenThrowBadRequestException() {

        itemFolderStub.setUrl("/new/url");
        when(itemFolderStub.getType()).thenReturn(DiskItemType.FOLDER);

        List<DiskItem> stubList = new ArrayList<>();
        stubList.add(itemFolderStub);

        DiskImportRequest importRequest = new DiskImportRequest();
        List<DiskItem> items = new ArrayList<>();
        items.add(itemFolderStub);
        importRequest.setItems(items);

        BadRequestException e = assertThrows(BadRequestException.class,
                () -> diskService.importDiskItems(importRequest));

        assertEquals(String.format("Size and url for folder %s must be null", itemFolderStub.getId()),
                e.getMessage());
    }

    @Test
    void whenSizeFolderIsNotNullThenThrowBadRequestException() {

        itemFolderStub.setUrl(null);
        when(itemFolderStub.getSize()).thenReturn(1L);
        when(itemFolderStub.getType()).thenReturn(DiskItemType.FOLDER);

        DiskImportRequest importRequest = new DiskImportRequest();
        List<DiskItem> items = new ArrayList<>();
        items.add(itemFolderStub);
        importRequest.setItems(items);

        BadRequestException e = assertThrows(BadRequestException.class,
                () -> diskService.importDiskItems(importRequest));

        assertEquals(String.format("Size and url for folder %s must be null", itemFolderStub.getId()),
                e.getMessage());
    }

    @Test
    void whenSizeFileIsEqualsZeroThenThrowBadRequestException() {

        itemFileStub.setSize(0L);
        when(itemFileStub.getType()).thenReturn(DiskItemType.FILE);

        DiskImportRequest importRequest = new DiskImportRequest();
        List<DiskItem> items = new ArrayList<>();
        items.add(itemFileStub);
        importRequest.setItems(items);

        BadRequestException e = assertThrows(BadRequestException.class,
                () -> diskService.importDiskItems(importRequest));

        assertEquals(String.format("Size file %s required", itemFileStub.getId()),
                e.getMessage());
    }

    @Test
    void whenItemTypeIsUnknownThenThrowBadRequestException() {

        itemFileStub.setType(null);

        DiskImportRequest importRequest = new DiskImportRequest();
        List<DiskItem> items = new ArrayList<>();
        items.add(itemFileStub);
        importRequest.setItems(items);

        BadRequestException e = assertThrows(BadRequestException.class,
                () -> diskService.importDiskItems(importRequest));

        assertEquals(String.format("Unknown item disk type %s", itemFileStub.getType()),
                e.getMessage());
    }

    @Test
    void whenRequestCorrectThenGetResponseOk() throws NotFoundException, BadRequestException {
        when(itemFileStub.getType()).thenReturn(DiskItemType.FILE);
        when(itemFileStub.getSize()).thenReturn(1L);

        DiskImportRequest importRequest = new DiskImportRequest();
        List<DiskItem> items = new ArrayList<>();
        items.add(itemFileStub);
        importRequest.setItems(items);
        importRequest.setUpdateDate(LocalDateTime.now());

        ResponseEntity<DiskResponseStatus> response = diskService.importDiskItems(importRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void whenDeleteByNonExistentIdThenThrowNotFoundException() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> diskService.deleteByIdWithChild(anyString(), LocalDateTime.now()));
        assertEquals("Item not found", e.getMessage());
    }

    @Test
    void whenDeleteWithChildThenGetResponseOk() throws NotFoundException {
        itemFileStub.setParentId(itemFolderStub.getParentId());
        List<DiskItem> listStubs = List.of(itemFileStub, itemFolderStub);
        when(diskItemRepositoryStub.findByIdWithChild(itemFolderStub.getId()))
                .thenReturn(listStubs);
        ResponseEntity<DiskResponseStatus> response = diskService.deleteByIdWithChild(itemFolderStub.getId(), LocalDateTime.now());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(diskItemRepositoryStub, times(1)).deleteAll(listStubs);
    }

    @Test
    void whenDeleteByCorrectIdThenGetResponseOk() throws NotFoundException {
        when(diskItemRepositoryStub.findByIdWithChild(itemFileStub.getId())).thenReturn(List.of(itemFileStub));
        ResponseEntity<DiskResponseStatus> response = diskService.deleteByIdWithChild(itemFileStub.getId(), LocalDateTime.now());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void whenGetByNonExistentIdThenThrowNotFoundException() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> diskService.findByIdWithChild(anyString()));
        assertEquals("Item not found", e.getMessage());
    }

    @Test
    void whenGetWithChildThenGetDiskItemDto() throws NotFoundException {
        when(itemFileStub.getType()).thenReturn(DiskItemType.FILE);
        when(itemFolderStub.getType()).thenReturn(DiskItemType.FOLDER);
        LocalDateTime now = LocalDateTime.now();

        when(itemFolderStub.getDate()).thenReturn(now);
        when(itemFileStub.getDate()).thenReturn(now);

        itemFileStub.setParentId(itemFolderStub.getId());
        itemFileStub.setDate(LocalDateTime.now());
        itemFolderStub.setDate(LocalDateTime.now());
        List<DiskItem> listStubs = List.of(itemFolderStub, itemFileStub);
        when(diskItemRepositoryStub.findByIdWithChild(itemFolderStub.getId()))
                .thenReturn(listStubs);

        List<DiskItemDto> children = new ArrayList<>();
        itemFolderDtoStub.setId(itemFolderStub.getId());
        itemFileDtoStub.setId(itemFileStub.getId());
        children.add(itemFileDtoStub);
        itemFolderDtoStub.setChildren(children);

        DiskItemDto head = diskService.findByIdWithChild(itemFolderStub.getId());
        assertEquals(1, head.getChildren().size());
        assertEquals(head.getChildren().get(0).getId(), itemFileStub.getId());

        verify(diskItemRepositoryStub, times(1)).findByIdWithChild(itemFolderStub.getId());
    }

    @Test
    void whenFindChildThenCalculateTreeFolders() throws NotFoundException {

        when(itemFileStub.getType()).thenReturn(DiskItemType.FILE);
        when(itemFileStub2.getType()).thenReturn(DiskItemType.FILE);
        when(itemFolderStub.getType()).thenReturn(DiskItemType.FOLDER);
        when(itemFolderStub2.getType()).thenReturn(DiskItemType.FOLDER);

        when(itemFileStub.getId()).thenReturn("key1");
        when(itemFileStub2.getId()).thenReturn("key2");
        when(itemFolderStub.getId()).thenReturn("key3");
        when(itemFolderStub2.getId()).thenReturn("key4");

        when(itemFileStub.getSize()).thenReturn(10L);
        when(itemFileStub2.getSize()).thenReturn(20L);

        String id = itemFolderStub.getId();
        when(itemFileStub.getParentId()).thenReturn(id);

        String id1 = itemFolderStub2.getId();
        when(itemFileStub2.getParentId()).thenReturn(id1);

        String id2 = itemFolderStub.getId();
        when(itemFolderStub2.getParentId()).thenReturn(id2);

        List<DiskItem> stubsList = new LinkedList<>();
        stubsList.add(itemFolderStub);
        stubsList.add(itemFileStub);
        stubsList.add(itemFolderStub2);
        stubsList.add(itemFileStub2);

        when(diskItemRepositoryStub.findByIdWithChild(itemFolderStub.getId())).thenReturn(stubsList);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nowPlus = now.plus(1, ChronoUnit.DAYS);

        when(itemFolderStub.getDate()).thenReturn(now);
        when(itemFolderStub2.getDate()).thenReturn(now);
        when(itemFileStub.getDate()).thenReturn(now);
        when(itemFileStub2.getDate()).thenReturn(nowPlus);

        DiskItemDto head = diskService.findByIdWithChild(itemFolderStub.getId());

        assertEquals(30L, head.getSize());
        assertEquals(nowPlus, head.getDate());
    }

    @Test
    void whenGetUpdatesByNonExistentDateThenReturnEmptyList() throws NotFoundException {
        LocalDateTime future = LocalDateTime.now().plus(2000, ChronoUnit.YEARS);
        List<DiskItem> emptyList = Collections.emptyList();
        when(diskItemRepositoryStub.findAllChangedFilesBetween(future.minusDays(1), future))
                .thenReturn(emptyList);

        assertEquals(emptyList, diskService.getUpdatesForLastDay(future));
    }

    @Test
    void whenGetHistoryWithNonExistingIdThenThrowNotFoundException() {
        LocalDateTime now = LocalDateTime.now();
        when(diskItemRepositoryStub.existsById("1")).thenReturn(false);
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> diskService.getHistoryByDiskItemId("1", now, now));
        assertEquals("Item not found", e.getMessage());
    }

    @Test
    void whenGetHistoryBrokenDatesThenThrowBadRequestException() {
        LocalDateTime dateStart = LocalDateTime.now();
        LocalDateTime dateEnd = LocalDateTime.now().minusDays(1);

        when(diskItemRepositoryStub.existsById("1")).thenReturn(true);
        BadRequestException e = assertThrows(BadRequestException.class,
                () -> diskService.getHistoryByDiskItemId("1", dateStart, dateEnd));
        assertEquals("Date end must be after date start", e.getMessage());
    }

    @Test
    void whenGetHistoryWithExistsIdThenReturnOkResponse() throws NotFoundException, BadRequestException {
        LocalDateTime now = LocalDateTime.now();
        List<DiskItemHistory> emptyList = Collections.emptyList();
        when(diskItemRepositoryStub.existsById("1")).thenReturn(true);
        when(diskItemHistoryRepositoryStub.getHistoryByDiskItemId("1", now, now.plusDays(1)))
                .thenReturn(emptyList);
        DiskHistoryResponse history = diskService.getHistoryByDiskItemId("1", now, now.plusDays(1));
        assertEquals(DiskHistoryResponse.class, history.getClass());
    }

}