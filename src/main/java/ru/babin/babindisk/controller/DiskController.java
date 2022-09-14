package ru.babin.babindisk.controller;

import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.babin.babindisk.exception.BadRequestException;
import ru.babin.babindisk.exception.NotFoundException;
import ru.babin.babindisk.model.dto.DiskHistoryResponse;
import ru.babin.babindisk.model.dto.DiskResponseStatus;
import ru.babin.babindisk.model.dto.DiskImportRequest;
import ru.babin.babindisk.model.dto.DiskItemDto;
import ru.babin.babindisk.service.DiskService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@AllArgsConstructor
public class DiskController {

    private final DiskService diskService;
    @PostMapping("/imports")
    public ResponseEntity<DiskResponseStatus> importDiskItems(@Valid @RequestBody DiskImportRequest request)
            throws NotFoundException, BadRequestException {
        return diskService.importDiskItems(request);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<DiskResponseStatus> deleteByIdWithChild(@PathVariable String id,
                                                                  @RequestParam
                                                                  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssX")
                                                                  LocalDateTime date)
            throws NotFoundException {
        return diskService.deleteByIdWithChild(id, date);
    }

    @GetMapping("/nodes/{id}")
    public DiskItemDto findByIdWithChild(@PathVariable String id) throws NotFoundException {
        return diskService.findByIdWithChild(id);
    }

    @GetMapping("/updates")
    public List<DiskItemDto> getSalesForLastDay(
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssX")
            LocalDateTime date) throws NotFoundException {
        return diskService.getUpdatesForLastDay(date);
    }

    @GetMapping("/node/{id}/history")
    public DiskHistoryResponse getHistoryById(@PathVariable String id,
                                              @RequestParam(required = false)
                                              @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssX") LocalDateTime dateStart,
                                              @RequestParam(required = false)
                                              @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssX") LocalDateTime dateEnd)
            throws NotFoundException, BadRequestException {
        return diskService.getHistoryByDiskItemId(id, dateStart, dateEnd);
    }
}

