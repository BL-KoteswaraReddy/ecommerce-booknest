//package com.ecommerce.book_service.mapper.impl;
//
////import com.ecommerce.book_service.dto.ResponseDto;
//import com.ecommerce.book_service.entity.Book;
//import com.ecommerce.book_service.mapper.Mapper;
//import lombok.RequiredArgsConstructor;
//import org.modelmapper.ModelMapper;
//import org.springframework.boot.Banner;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class UserResponseMapper implements Mapper<Book, ResponseDto> {
//
//    private final ModelMapper modelMapper;
//
//    @Override
//    public ResponseDto mapTo(Book book) {
//        return modelMapper.map(book, ResponseDto.class);
//    }
//
//    @Override
//    public Book mapFrom(ResponseDto responseDto) {
//        return modelMapper.map(responseDto, Book.class);
//    }
//}
