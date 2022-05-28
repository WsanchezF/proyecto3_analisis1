package com.example.rupbank.controller;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.example.rupbank.form.CustomerForm;
import com.example.rupbank.model.*;
import com.example.rupbank.repository.AccountRepository;
import com.example.rupbank.repository.CustomerRepository;
import com.example.rupbank.repository.TransactionRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import com.example.rupbank.repository.BankRepository;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class SecurityController {
    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @GetMapping("/login")
    public String login(Model model) {
        List<Bank> banks = bankRepository.findAll();
        model.addAttribute("banks", banks);

        return "security/login"; //view
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        return "redirect:/";  //Where you go after logout here.
    }

    @GetMapping("/forgot-password")
    public String forgotPassword(Model model) {
        return "security/forgotPassword"; //view
    }

    @GetMapping("/register")
    public String register(Model model, CustomerForm customerForm) {
        List<Bank> banks = bankRepository.findAll();
        model.addAttribute("banks", banks);

        return "security/register"; //view
    }

    @PostMapping("/register")
    public String postRegister(Model model, @Valid CustomerForm customerForm, BindingResult bindingResult) throws Exception {
        if (bindingResult.hasErrors()) {
            List<Bank> banks = bankRepository.findAll();
            model.addAttribute("banks", banks);

            return "security/register";
        }

        // check if exists...
        Customer exist = customerRepository.getUserByUsername(customerForm.getEmail());

        if (exist != null) {
            List<Bank> banks = bankRepository.findAll();
            model.addAttribute("banks", banks);
            model.addAttribute("message", "Ya existe un usuario con este email.");

            return "security/register";
        }

        Customer customer = new Customer();
        customer.setFullName(customerForm.getFullName());
        customer.setEmail(customerForm.getEmail());
        customer.setPassword(passwordEncoder.encode(customerForm.getPassword()));
        customer.setCreatedAt(Instant.now());
        customer.setRole("ROLE_USER");
        customerRepository.save(customer);

        sendWelcomeEmail(customer);
        createFirstBankAccount(customer, customerForm.getBankId());

        return "redirect:/";
    }

    private void createFirstBankAccount(Customer customer, long bankId) throws Exception {
        Bank bank = bankRepository.findById(bankId).orElseThrow(() -> new Exception("Bank not found."));
        Integer lastAccountNumberIssued = bank.getLastNumberAccountIssued();
        Integer newAccountNumberIssued = lastAccountNumberIssued+1;

        // create first account
        Account account = new Account();
        account.setBank(bank);
        account.setBalance(BigDecimal.valueOf(1000));
        account.setName(customer.getFullName());
        account.setCustomer(customer);
        account.setDailyLimit(BigDecimal.valueOf(100));
        account.setNumber(String.valueOf(newAccountNumberIssued));
        account.setType("M");
        account.setCreatedAt(Instant.now());
        accountRepository.save(account);

        // create transaction
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setCreatedAt(Instant.now());
        transaction.setType("DEPOSIT");
        transaction.setAmount(account.getBalance());
        transaction.setDescription("Apertura de cuenta No. " + account.getNumber());
        transactionRepository.save(transaction);

        bank.setLastNumberAccountIssued(newAccountNumberIssued);
        bankRepository.save(bank);
    }

    private void sendWelcomeEmail(Customer customer) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("rupbankumg@gmail.com");
        message.setTo(customer.getEmail());
        message.setSubject("Bienvenido al mejor banco");
        message.setText(String.format("Hola %s, estamos felices de pdoder darte la bienvenida.", customer.getFullName()));

        emailSender.send(message);
    }
}
