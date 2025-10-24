package com.example.bankcards.util.mapper;

public interface DtoMapper<T, S> {
    T map(S dto);
}
