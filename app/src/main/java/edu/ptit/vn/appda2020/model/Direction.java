package edu.ptit.vn.appda2020.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Direction {
    private GeoPoint from;
    private GeoPoint to;
    private List<GeoPoint> route;
}
