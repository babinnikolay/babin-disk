package ru.babin.babindisk.model.mapper;

import ru.babin.babindisk.model.DiskItem;
import ru.babin.babindisk.model.DiskItemHistory;

public class DiskItemHistoryMapper {
    private DiskItemHistoryMapper() {

    }

    public static DiskItemHistory toDiskItemHistory(DiskItem item) {
        DiskItemHistory itemHistory = new DiskItemHistory();
        itemHistory.setId(item.getId());
        itemHistory.setDate(item.getDate());
        itemHistory.setParentId(item.getParentId());
        itemHistory.setSize(item.getSize());
        itemHistory.setType(item.getType());
        itemHistory.setUrl(item.getUrl());
        return itemHistory;
    }
}
