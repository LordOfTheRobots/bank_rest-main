package com.example.bankcards.controller.frontend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    @GetMapping({"/", "/index"})
    public String index() { return "index"; }

    @GetMapping("/profile")
    public String profile() { return "profile"; }

    @GetMapping("/cards")
    public String cards() {
        return "cards"; }
    
}
