package ru.babin.babindisk.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DiskResponseStatus {
    private int code;
    private String message;
}
