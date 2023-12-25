package com.mainthreadlab.weinv.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

public class PaginationUtils {

    private PaginationUtils() {
    }

    public static Pageable toPageable(int offset, int limit, String sortingKeys) {

        if (StringUtils.isBlank(sortingKeys)) {
            return PageRequest.of(offset, limit, Sort.unsorted());
        } else {
            List<Sort.Order> orders = Arrays.stream(sortingKeys.split(",")).map(x -> {
                String[] split = x.split(":");
                return "ASC".equalsIgnoreCase(split[1]) ? Sort.Order.by(split[0]).with(ASC) : Sort.Order.by(split[0]).with(DESC);
            }).collect(Collectors.toList());

            return PageRequest.of(offset, limit, Sort.by(orders));
        }
    }

}
