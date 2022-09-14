package ru.babin.babindisk.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.babin.babindisk.exception.BadRequestException;
import ru.babin.babindisk.exception.NotFoundException;
import ru.babin.babindisk.model.DiskItem;
import ru.babin.babindisk.model.DiskItemHistory;
import ru.babin.babindisk.model.DiskItemType;
import ru.babin.babindisk.model.dto.DiskHistoryResponse;
import ru.babin.babindisk.model.dto.DiskResponseStatus;
import ru.babin.babindisk.model.dto.DiskImportRequest;
import ru.babin.babindisk.model.dto.DiskItemDto;
import ru.babin.babindisk.model.mapper.DiskItemMapper;
import ru.babin.babindisk.repository.DiskItemHistoryRepository;
import ru.babin.babindisk.repository.DiskItemRepository;
import ru.babin.babindisk.service.DiskService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class DiskServiceImpl implements DiskService {
    private final DiskItemRepository diskItemRepository;
    private final DiskItemHistoryRepository diskItemHistoryRepository;
    @Override
    @Transactional(rollbackFor = {BadRequestException.class})
    public ResponseEntity<DiskResponseStatus> importDiskItems(DiskImportRequest importRequest)
            throws BadRequestException {

        Map<String, DiskItem> itemsRequest = importRequest.getItems()
                .stream()
                .collect(Collectors.toMap(DiskItem::getId, Function.identity()));
        for (Map.Entry<String, DiskItem> entry : itemsRequest.entrySet()) {
            DiskItem item = entry.getValue();
            itemValidate(itemsRequest, item);
            item.setDate(importRequest.getUpdateDate());
        }
        diskItemRepository.updateFoldersDateUp(importRequest.getUpdateDate(), importRequest.getItems().stream()
                .map(DiskItem::getId)
                .collect(Collectors.toList()));

        diskItemRepository.saveAll(itemsRequest.values());
        checkWrongParents();

        diskItemRepository.updateFoldersSizeAndDate(importRequest.getUpdateDate(), Collections.emptyList());
        diskItemHistoryRepository.upsertDiskItemsHistory(importRequest.getUpdateDate());

        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @Override
    @Transactional
    public ResponseEntity<DiskResponseStatus> deleteByIdWithChild(String id, LocalDateTime newDate)
            throws NotFoundException {
        List<DiskItem> items = diskItemRepository.findByIdWithChild(id);
        if (items.isEmpty()) {
            String itemNotFound = "Item not found";
            throw new NotFoundException(itemNotFound);
        }

        diskItemRepository.deleteAll(items);
        diskItemRepository.updateFoldersSizeAndDate(newDate, items.stream()
                .map(item -> {
                    String parentId = item.getParentId();
                    if (parentId == null) {
                        return "";
                    }
                    return parentId;
                })
                .collect(Collectors.toList()));
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @Override
    public DiskItemDto findByIdWithChild(String id) throws NotFoundException {
        List<DiskItem> items = diskItemRepository.findByIdWithChild(id);
        if (items.isEmpty()) {
            throw new NotFoundException("Item not found");
        }
        DiskItemDto headDto = DiskItemMapper.toDiskItemDto(items);
        calculateSumAndDate(headDto);
        return headDto;
    }

    @Override
    public List<DiskItemDto> getUpdatesForLastDay(LocalDateTime date) {
        List<DiskItem> items = diskItemRepository.findAllChangedFilesBetween(date.minusDays(1), date);
        return items.stream().map(DiskItemMapper::toDiskItemDto).collect(Collectors.toList());
    }

    @Override
    public DiskHistoryResponse getHistoryByDiskItemId(String id, LocalDateTime dateStart, LocalDateTime dateEnd)
            throws NotFoundException, BadRequestException {
        if (!diskItemRepository.existsById(id)) {
            throw new NotFoundException("Item not found");
        }
        if (dateStart == null) {
            dateStart = LocalDateTime.of(1900, 01, 01, 01, 01);
        }
        if (dateEnd == null) {
            dateEnd = LocalDateTime.of(3900, 01, 01, 01, 01);
        }
        if (dateEnd.isBefore(dateStart)) {
            throw new BadRequestException("Date end must be after date start");
        }
        List<DiskItemHistory> history = diskItemHistoryRepository.getHistoryByDiskItemId(id, dateStart, dateEnd);
        return new DiskHistoryResponse(history);
    }

    private void checkWrongParents() throws BadRequestException {
        Set<String> wrongParents = diskItemRepository.findWrongParents()
                .stream()
                .map(DiskItem::getId)
                .collect(Collectors.toSet());

        if (!wrongParents.isEmpty()) {
            throw new BadRequestException(String.format("Items with id %s have wrong parents",
                    String.join(",", wrongParents)));
        }
    }

    private void itemValidate(Map<String, DiskItem> itemsRequest, DiskItem item) throws BadRequestException {
        if (item.getType() == DiskItemType.FOLDER) {
            if (item.getSize() != null || item.getUrl() != null) {
                throw new BadRequestException(String.format("Size and url for folder %s must be null", item.getId()));
            }
        } else if (item.getType() == DiskItemType.FILE) {
            if (item.getSize() == null || item.getSize() == 0) {
                throw new BadRequestException(String.format("Size file %s required", item.getId()));
            }
        } else {
            throw new BadRequestException(String.format("Unknown item disk type %s", item.getType()));
        }

        List<DiskItem> itemsDB = diskItemRepository.findAllById(itemsRequest.values()
                .stream()
                .map(DiskItem::getId)
                .collect(Collectors.toSet()));

        for (DiskItem itemDB : itemsDB) {
            if (itemsRequest.containsKey(itemDB.getId())
                    && itemsRequest.get(itemDB.getId()).getType() != itemDB.getType()) {
                throw new BadRequestException(String.format("Item with id %s cannot change type", itemDB.getId()));
            }
        }
    }

    private void calculateSumAndDate(DiskItemDto dto) {

        if (dto.getChildren().stream().anyMatch(item -> item.getType() == DiskItemType.FOLDER)) {
            for (DiskItemDto childDto : dto.getChildren()) {
                if (childDto.getType() == DiskItemType.FOLDER) {
                    calculateSumAndDate(childDto);
                }
            }
            dto.setSize(dto.getChildren().stream().collect(Collectors.summingLong(DiskItem::getSize)));
            LocalDateTime childDto = dto.getChildren().stream().map(DiskItemDto::getDate).max(LocalDateTime::compareTo).orElseThrow();
            if (dto.getDate().isBefore(childDto)) {
                dto.setDate(childDto);
            }
        } else {
            LocalDateTime maxDate = dto.getDate();
            Long sumSize = 0L;
            for (DiskItemDto file : dto.getChildren()) {
                maxDate = maxDate.isBefore(file.getDate()) ? file.getDate() : maxDate;
                sumSize += file.getSize();
            }
            dto.setDate(maxDate);
            dto.setSize(sumSize);
        }
    }
}
