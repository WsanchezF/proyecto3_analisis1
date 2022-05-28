package com.example.rupbank.controller;

import com.example.rupbank.model.Bank;
import com.example.rupbank.repository.BankRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import javax.servlet.http.HttpServletRequest;

@Controller
public class HomepageController {
    Logger logger = LoggerFactory.getLogger(HomepageController.class);

    @Autowired
    private BankRepository bankRepository;

    @GetMapping("/")
    public String main(Model model, HttpServletRequest request) throws Exception {
        Long bankId = Long.parseLong(request.getSession().getAttribute("bank_id").toString());
        logger.warn(String.format("Bank from session: %s", bankId));

        Bank bank = bankRepository.findById(bankId).orElseThrow(() -> new Exception("Bank not found on :: " + bankId));
        model.addAttribute("bank", bank);

        return "homepage/index"; //view
    }
}
