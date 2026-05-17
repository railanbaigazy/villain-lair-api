package kz.railan.villain_lair_api.lair.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateLairRequest(
        @NotBlank @Size(max = 100) String name
) {
}
