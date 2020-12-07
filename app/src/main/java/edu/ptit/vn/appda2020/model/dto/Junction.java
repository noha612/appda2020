package edu.ptit.vn.appda2020.model.dto;

import java.io.Serializable;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
@EqualsAndHashCode(callSuper=true)
public class Junction extends GeoPoint implements Serializable {
    private String id;

    public Junction(String id, Double lat, Double lng) {
        super(lat, lng);
        this.id = id;
    }
}
