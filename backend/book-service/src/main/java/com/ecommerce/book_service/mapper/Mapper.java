package com.ecommerce.book_service.mapper;

import java.util.List;

public interface Mapper<A, B> {

    B mapTo(A a);
    A mapFrom(B b);

    default List<B> mapToList(List<A> list) {
        return list.stream().map(this::mapTo).toList();
    }
}
