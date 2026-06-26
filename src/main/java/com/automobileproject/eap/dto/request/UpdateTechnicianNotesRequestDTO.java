package com.automobileproject.eap.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpdateTechnicianNotesRequestDTO {

    @NotBlank(message = "Technician notes must not be blank")
    private String technicianNotes;
}
