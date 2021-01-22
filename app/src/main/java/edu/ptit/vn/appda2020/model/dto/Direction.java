package edu.ptit.vn.appda2020.model.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Direction {
    private GeoPoint from;
    private GeoPoint to;
    private List<Junction> junctions;
    private Map<String, String> traffics;
    private double length; //km
    private double time; //h
}
