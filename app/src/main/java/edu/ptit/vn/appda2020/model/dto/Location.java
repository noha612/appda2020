package edu.ptit.vn.appda2020.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    GeoPoint marker;
    GeoPoint h;
    Place place;
}
