package com.mainthreadlab.weinv.dto.response;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class ResponsePage<T> extends PageImpl<T> {

    private Integer totalGuestsAttending;
    private Integer totalGuestsNotAttending;
    private Integer totalGuestsMaybe;
    private Integer totalGuestsNotReplied;

    public ResponsePage(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public ResponsePage(List<T> content) {
        super(content);
    }

    public ResponsePage(Page<T> page) {
        super(page.getContent(), page.getPageable(), page.getContent().size());
    }


}
