package ru.babin.babindisk.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.babin.babindisk.exception.BadRequestException;
import ru.babin.babindisk.exception.NotFoundException;
import ru.babin.babindisk.model.dto.DiskHistoryResponse;
import ru.babin.babindisk.model.dto.DiskResponseStatus;
import ru.babin.babindisk.model.dto.DiskImportRequest;
import ru.babin.babindisk.model.dto.DiskItemDto;

import java.time.LocalDateTime;
import java.util.List;

@Service
public interface DiskService {
    ResponseEntity<DiskResponseStatus> importDiskItems(DiskImportRequest request) throws NotFoundException, BadRequestException;

    ResponseEntity<DiskResponseStatus> deleteByIdWithChild(String id, LocalDateTime newDate) throws NotFoundException;

    DiskItemDto findByIdWithChild(String id) throws NotFoundException;

    List<DiskItemDto> getUpdatesForLastDay(LocalDateTime date) throws NotFoundException;

    DiskHistoryResponse getHistoryByDiskItemId(String id, LocalDateTime dateStart, LocalDateTime dateEnd) throws NotFoundException, BadRequestException;
}
