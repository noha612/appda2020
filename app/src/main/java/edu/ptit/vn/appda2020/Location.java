package edu.ptit.vn.appda2020;

public class Location {
    private String name;
    private Intersection intersection;

    public Location() {
    }

    public Location(String name, Intersection intersection) {
        this.name = name;
        this.intersection = intersection;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Intersection getIntersection() {
        return intersection;
    }

    public void setIntersection(Intersection intersection) {
        this.intersection = intersection;
    }

    @Override
    public String toString() {
        return "Location{" +
                "name='" + name + '\'' +
                ", intersection=" + intersection +
                '}';
    }
}
