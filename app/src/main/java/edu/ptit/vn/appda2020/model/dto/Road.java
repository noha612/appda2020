package edu.ptit.vn.appda2020.model.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Road implements Serializable {
    String id;
    GeoPoint gp1;
    GeoPoint gp2;
}
