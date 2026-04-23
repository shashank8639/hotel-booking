package com.hotelbooking.util;

import com.hotelbooking.exception.InvalidRoomSortException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

/**
 * Caps page size and whitelists sort properties for Room list/search APIs.
 */
public final class RoomPageables {

    public static final int MAX_PAGE_SIZE = 50;

    public static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "roomNumber",
            "roomType",
            "floorNumber",
            "capacity",
            "pricePerNight",
            "discountedPrice",
            "status",
            "createdAt",
            "updatedAt"
    );

    private RoomPageables() {
    }

    public static Pageable constrain(Pageable pageable) {
        int size = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);

        Sort.Order firstInvalid = null;
        Sort.Order firstValid = null;
        for (Sort.Order order : pageable.getSort()) {
            if (!ALLOWED_SORT_FIELDS.contains(order.getProperty())) {
                if (firstInvalid == null) {
                    firstInvalid = order;
                }
                continue;
            }
            if (firstValid == null) {
                firstValid = order;
            }
        }

        if (firstInvalid != null && firstValid == null) {
            throw new InvalidRoomSortException(
                    "Unsupported sort field: " + firstInvalid.getProperty()
                            + ". Allowed: " + ALLOWED_SORT_FIELDS
            );
        }

        Sort sort = Sort.unsorted();
        for (Sort.Order order : pageable.getSort()) {
            if (ALLOWED_SORT_FIELDS.contains(order.getProperty())) {
                sort = sort.and(Sort.by(order));
            }
        }
        if (sort.isUnsorted()) {
            sort = Sort.by(Sort.Order.asc("roomNumber"));
        }

        return PageRequest.of(pageable.getPageNumber(), size, sort);
    }
}
