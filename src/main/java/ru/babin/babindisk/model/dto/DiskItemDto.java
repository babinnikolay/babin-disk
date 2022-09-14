package ru.babin.babindisk.model.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.babin.babindisk.model.DiskItem;

import java.util.List;

@EqualsAndHashCode
public class DiskItemDto extends DiskItem {
    @Getter
    @Setter
    private List<DiskItemDto> children;
}
