package ru.babin.babindisk.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "Id")
@Getter
@Setter
public class DiskItem extends AbstractDiskItem{
    @Id
    @Column(name = "disk_item_id")
    @NotNull
    @NotEmpty
    private String id;
}
