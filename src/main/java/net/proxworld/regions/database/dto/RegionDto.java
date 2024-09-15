package net.proxworld.regions.database.dto;

import java.beans.ConstructorProperties;

public record RegionDto(String name, String type, String owner, String world, int x, int y, int z) {

    @ConstructorProperties({"name", "type", "owner", "world", "x", "y", "z"})
    public RegionDto {
    }

}
