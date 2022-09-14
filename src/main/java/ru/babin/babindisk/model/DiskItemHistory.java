package ru.babin.babindisk.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class DiskItemHistory extends AbstractDiskItem{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "disk_item_history_id")
    @JsonIgnore
    private Long historyId;
    @Column(name = "disk_item_id")
    private String id;
}
