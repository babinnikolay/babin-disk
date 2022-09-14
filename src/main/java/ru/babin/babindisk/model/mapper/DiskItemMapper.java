package ru.babin.babindisk.model.mapper;

import ru.babin.babindisk.model.DiskItem;
import ru.babin.babindisk.model.DiskItemType;
import ru.babin.babindisk.model.dto.DiskItemDto;

import java.util.*;

public class DiskItemMapper {

    private DiskItemMapper() {

    }

    public static DiskItemDto toDiskItemDto(List<DiskItem> items) {
        DiskItemDto head = null;
        Map<String, DiskItemDto> folders = new HashMap<>();
        for (DiskItem item : items) {
            DiskItemDto dto = toDiskItemDto(item);
            if (dto.getType() == DiskItemType.FOLDER) {
                folders.put(dto.getId(), dto);
            }
            if (head == null) {
                head = dto;
                continue;
            }
            if (folders.containsKey(dto.getParentId())) {
                DiskItemDto folder = folders.get(dto.getParentId());
                folder.getChildren().add(dto);
            }
        }
        return head;
    }

    public static DiskItemDto toDiskItemDto(DiskItem item) {
        DiskItemDto dto = new DiskItemDto();
        dto.setId(item.getId());
        dto.setUrl(item.getUrl());
        dto.setSize(item.getSize());
        dto.setType(item.getType());
        dto.setParentId(item.getParentId());
        dto.setDate(item.getDate());
        if (item.getType() == DiskItemType.FOLDER) {
            dto.setChildren(new ArrayList<>());
        }
        return dto;
    }
}
