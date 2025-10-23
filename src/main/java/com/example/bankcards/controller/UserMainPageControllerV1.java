package com.example.bankcards.controller;

import com.example.bankcards.entity.Card;
import com.example.bankcards.service.Service1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserMainPageControllerV1 {

    @Autowired
    private Service1 service;

    @GetMapping
    @RequestMapping("/{userId}")
    protected ResponseEntity<List<Card>> showCards(@PathVariable UUID userId){
        List<Card> list = new ArrayList<>();
        return ResponseEntity.ok(list);
    }

    @PutMapping
    @RequestMapping("/{userId}")
    protected void addCard(@PathVariable UUID userId, @RequestBody Card card){

    }
}
