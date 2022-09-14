package ru.babin.babindisk.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import ru.babin.babindisk.serialize.LocalDateTimeToZonedSerializer;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class AbstractDiskItem {
    @Column(name = "disk_item_type")
    @Enumerated(EnumType.STRING)
    @NotNull
    private DiskItemType type;
    @Column(name = "disk_item_parent_id")
    private String parentId;
    @Column(name = "disk_item_url")
    @Size(max = 255)
    private String url;
    @Column(name = "disk_item_size")
    private Long size;
    @Column(name = "disk_item_date", columnDefinition = "TIMESTAMP WITHOUT TIME ZONE")
    @NotNull
    @PastOrPresent
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonSerialize(using = LocalDateTimeToZonedSerializer.class, as = LocalDateTime.class)
    private LocalDateTime date;
}
