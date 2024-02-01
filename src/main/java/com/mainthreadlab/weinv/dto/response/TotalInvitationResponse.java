package com.mainthreadlab.weinv.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TotalInvitationResponse {

    private Integer totalGuestsAttending;
    private Integer totalGuestsNotAttending;
    private Integer totalGuestsMaybe;
    private Integer totalGuestsNotReplied;

}
