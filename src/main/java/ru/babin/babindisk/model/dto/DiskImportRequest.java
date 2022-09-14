package ru.babin.babindisk.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.babin.babindisk.model.DiskItem;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DiskImportRequest {
    @NotNull
    @NotEmpty
    private List<DiskItem> items;
    @NotNull
    @PastOrPresent
    private LocalDateTime updateDate;
}

