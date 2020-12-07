package edu.ptit.vn.appda2020.model.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class GeoPoint{
    private Double lat;
    private Double lng;
}
