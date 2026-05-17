package kz.railan.villain_lair_api.lair.dto;

public record LairDefenseUnitResponse(
        Long id,
        String name,
        Integer power,
        Integer durability,
        Boolean active
) {
}
