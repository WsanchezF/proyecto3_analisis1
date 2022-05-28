package com.example.rupbank.controller;

import com.example.rupbank.model.*;
import com.example.rupbank.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Controller
public class ServiceController {
    Logger logger = LoggerFactory.getLogger(HomepageController.class);

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @GetMapping("/service-module")
    public String main(Model model, HttpServletRequest request) throws Exception {
        logger.warn("Services home.");

        return "service/index"; //view
    }

    @GetMapping("/service-module/telephone")
    public String telephone(Model model, HttpServletRequest request) throws Exception {
        logger.warn("Pay telephone.");

        Bank bank = getBankFromSession(request);
        Customer customer = getCustomerFromSession(request);
        List<Account> accounts = accountRepository.findByCustomerAndBank(customer, bank);

        model.addAttribute("bank", bank);
        model.addAttribute("customer", customer);
        model.addAttribute("accounts", accounts);

        return "service/pay_telephone"; //view
    }

    @GetMapping("/service-module/telephone/pay")
    public String payTelephone(@RequestParam(name = "tel") String tel, @RequestParam(name = "amount") String amount, @RequestParam(name = "account_id") String accountId) throws Exception {
        logger.warn("Pay telephone.");

        Account account = accountRepository.findById(Long.valueOf(accountId)).get();

        if (Long.parseLong(amount) <= 0) {
            throw new Exception("Monto invalido");
        }

        if (account.getBalance().compareTo(BigDecimal.valueOf(Long.parseLong(amount))) < 1) {
            throw new Exception("El monto de pago sobrepasa el balance de la cuenta.");
        }

        // create negative transaction on account
        Transaction transaction = new Transaction();
        transaction.setDescription("Pago de teléfono: " + tel);
        transaction.setAmount(BigDecimal.valueOf(Long.parseLong(amount) * -1));
        transaction.setType("DEBIT");
        transaction.setAccount(account);
        transaction.setCreatedAt(Instant.now());
        transactionRepository.save(transaction);

        account.setBalance(account.getBalance().subtract(BigDecimal.valueOf(Long.parseLong(amount))));
        accountRepository.save(account);

        return "redirect:/service-module/telephone";
    }

    @GetMapping("/service-module/eegsa")
    public String eegsa(Model model, HttpServletRequest request) throws Exception {
        logger.warn("Pay EEGSA.");

        Bank bank = getBankFromSession(request);
        Customer customer = getCustomerFromSession(request);
        List<Account> accounts = accountRepository.findByCustomerAndBank(customer, bank);

        model.addAttribute("bank", bank);
        model.addAttribute("customer", customer);
        model.addAttribute("accounts", accounts);

        return "service/pay_eegsa"; //view
    }

    @GetMapping("/service-module/eegsa/pay")
    public String payEegsa(@RequestParam(name = "counter") String counter, @RequestParam(name = "amount") String amount, @RequestParam(name = "account_id") String accountId) throws Exception {
        logger.warn("Pay EEGSA.");

        Account account = accountRepository.findById(Long.valueOf(accountId)).get();

        if (Long.parseLong(amount) <= 0) {
            throw new Exception("Monto invalido");
        }

        if (account.getBalance().compareTo(BigDecimal.valueOf(Long.parseLong(amount))) < 1) {
            throw new Exception("El monto de pago sobrepasa el balance de la cuenta.");
        }

        // create negative transaction on account
        Transaction transaction = new Transaction();
        transaction.setDescription("Pago de EEGSA, contador: " + counter);
        transaction.setAmount(BigDecimal.valueOf(Long.parseLong(amount) * -1));
        transaction.setType("DEBIT");
        transaction.setAccount(account);
        transaction.setCreatedAt(Instant.now());
        transactionRepository.save(transaction);

        account.setBalance(account.getBalance().subtract(BigDecimal.valueOf(Long.parseLong(amount))));
        accountRepository.save(account);

        return "redirect:/service-module/eegsa";
    }

    @GetMapping("/service-module/empagua")
    public String empagua(Model model, HttpServletRequest request) throws Exception {
        logger.warn("Pay Empagua.");

        Bank bank = getBankFromSession(request);
        Customer customer = getCustomerFromSession(request);
        List<Account> accounts = accountRepository.findByCustomerAndBank(customer, bank);

        model.addAttribute("bank", bank);
        model.addAttribute("customer", customer);
        model.addAttribute("accounts", accounts);

        return "service/pay_empagua"; //view
    }

    @GetMapping("/service-module/empagua/pay")
    public String payEmpagua(@RequestParam(name = "counter") String counter, @RequestParam(name = "amount") String amount, @RequestParam(name = "account_id") String accountId) throws Exception {
        logger.warn("Pay telephone.");

        Account account = accountRepository.findById(Long.valueOf(accountId)).get();

        if (Long.parseLong(amount) <= 0) {
            throw new Exception("Monto invalido");
        }

        if (account.getBalance().compareTo(BigDecimal.valueOf(Long.parseLong(amount))) < 1) {
            throw new Exception("El monto de pago sobrepasa el balance de la cuenta.");
        }

        // create negative transaction on account
        Transaction transaction = new Transaction();
        transaction.setDescription("Pago de EMPAGUA, contador: " + counter);
        transaction.setAmount(BigDecimal.valueOf(Long.parseLong(amount) * -1));
        transaction.setType("DEBIT");
        transaction.setAccount(account);
        transaction.setCreatedAt(Instant.now());
        transactionRepository.save(transaction);

        account.setBalance(account.getBalance().subtract(BigDecimal.valueOf(Long.parseLong(amount))));
        accountRepository.save(account);

        return "redirect:/service-module/empagua";
    }

    private Bank getBankFromSession(HttpServletRequest request) throws Exception {
        Long bankId = Long.parseLong(request.getSession().getAttribute("bank_id").toString());
        logger.warn(String.format("Bank from session: %s", bankId));

        return bankRepository.findById(bankId).orElseThrow(() -> new Exception("Bank not found on :: " + bankId));
    }

    private Customer getCustomerFromSession(HttpServletRequest request) {
        String customerEmail = (String) request.getSession().getAttribute("customer_email");
        logger.warn(String.format("Customer from session: %s", customerEmail));

        return customerRepository.getUserByUsername(customerEmail);
    }
}
