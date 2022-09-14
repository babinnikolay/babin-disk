package ru.babin.babindisk.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.babin.babindisk.model.DiskItemHistory;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class DiskHistoryResponse {
    @Getter
    @Setter
    private List<DiskItemHistory> items;
}
