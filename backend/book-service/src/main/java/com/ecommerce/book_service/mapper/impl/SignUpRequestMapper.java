//package com.ecommerce.book_service.mapper.impl;
//
////import com.ecommerce.book_service.dto.RequestDto;
//import com.ecommerce.book_service.entity.Book;
//import com.ecommerce.book_service.mapper.Mapper;
//import lombok.RequiredArgsConstructor;
//import org.modelmapper.ModelMapper;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//public class SignUpRequestMapper implements Mapper<RequestDto, Book> {
//
//    private final ModelMapper modelMapper;
//
//    @Override
//    public Book mapTo(RequestDto requestDto) {
//        return modelMapper.map(requestDto, Book.class);
//    }
//
//    @Override
//    public RequestDto mapFrom(Book book) {
//        return modelMapper.map(book, RequestDto.class);
//    }
//
//
//}
