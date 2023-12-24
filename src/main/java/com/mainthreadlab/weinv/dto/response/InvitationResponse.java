package com.mainthreadlab.weinv.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
public class InvitationResponse extends UserResponse {

    private boolean confirmed;

    private boolean rejected;

    private String uuidWedding;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InvitationResponse)) return false;
        if (!super.equals(o)) return false;
        InvitationResponse that = (InvitationResponse) o;
        return confirmed == that.confirmed && rejected == that.rejected;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), confirmed, rejected);
    }
}
