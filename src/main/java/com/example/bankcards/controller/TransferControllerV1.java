package com.example.bankcards.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/{userId}")
public class TransferControllerV1 {
    @PostMapping
    @RequestMapping("/{cardId}/{transfer}")
    public void transferMoneyToAnotherCard(){

    }
}
