package com.example.rupbank.controller;

import com.example.rupbank.model.Account;
import com.example.rupbank.model.Bank;
import com.example.rupbank.model.Customer;
import com.example.rupbank.model.Transaction;
import com.example.rupbank.repository.AccountRepository;
import com.example.rupbank.repository.BankRepository;
import com.example.rupbank.repository.CustomerRepository;
import com.example.rupbank.repository.TransactionRepository;
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
public class TransferController {
    Logger logger = LoggerFactory.getLogger(HomepageController.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @GetMapping("/transfer-module")
    public String main(Model model, HttpServletRequest request) throws Exception {
        logger.warn("Transfer home.");

        return "transfer/index"; //view
    }

    @GetMapping("/transfer-module/my-accounts")
    public String own(Model model, HttpServletRequest request) throws Exception {
        logger.warn("Transfer my accounts.");

        Bank bank = getBankFromSession(request);
        Customer customer = getCustomerFromSession(request);
        List<Account> accounts = accountRepository.findByCustomerAndBank(customer, bank);

        model.addAttribute("bank", bank);
        model.addAttribute("customer", customer);
        model.addAttribute("accounts", accounts);

        return "transfer/own"; //view
    }

    @GetMapping("/transfer-module/my-accounts/transfer")
    public String transferOwn(@RequestParam(name = "amount") String amount, @RequestParam(name = "from_id") String fromId, @RequestParam(name = "to_id") String toId) throws Exception {
        logger.warn("Transfer own accounts.");

        Account fromAccount = accountRepository.findById(Long.valueOf(fromId)).get();
        Account toAccount = accountRepository.findById(Long.valueOf(toId)).get();

        if (Long.parseLong(amount) <= 0) {
            throw new Exception("Monto invalido");
        }

        if (fromAccount.getBalance().compareTo(BigDecimal.valueOf(Long.parseLong(amount))) < 1) {
            throw new Exception("El monto de pago sobrepasa el balance de la cuenta.");
        }

        // create negative transaction on account
        Transaction negativeTransaction = new Transaction();
        negativeTransaction.setDescription("Transferencia entre cuentas, a: " + toAccount.getIdentifier());
        negativeTransaction.setAmount(BigDecimal.valueOf(Long.parseLong(amount) * -1));
        negativeTransaction.setType("DEBIT");
        negativeTransaction.setAccount(fromAccount);
        negativeTransaction.setCreatedAt(Instant.now());
        transactionRepository.save(negativeTransaction);

        fromAccount.setBalance(fromAccount.getBalance().subtract(BigDecimal.valueOf(Long.parseLong(amount))));
        accountRepository.save(fromAccount);

        // create positive transaction on account
        Transaction positiveTransaction = new Transaction();
        positiveTransaction.setDescription("Transferencia entre cuentas, desde: " + fromAccount.getIdentifier());
        positiveTransaction.setAmount(BigDecimal.valueOf(Long.parseLong(amount)));
        positiveTransaction.setType("CREDIT");
        positiveTransaction.setAccount(toAccount);
        positiveTransaction.setCreatedAt(Instant.now());
        transactionRepository.save(positiveTransaction);

        toAccount.setBalance(toAccount.getBalance().add(BigDecimal.valueOf(Long.parseLong(amount))));
        accountRepository.save(toAccount);

        return "redirect:/transfer-module/my-accounts";
    }

    @GetMapping("/transfer-module/third-party")
    public String third(Model model, HttpServletRequest request) throws Exception {
        logger.warn("Transfer third party.");

        Bank bank = getBankFromSession(request);
        Customer customer = getCustomerFromSession(request);
        List<Account> myAccounts = accountRepository.findByCustomerAndBank(customer, bank);
        List<Account> bankAccounts = accountRepository.findByBank(bank);

        model.addAttribute("bank", bank);
        model.addAttribute("customer", customer);
        model.addAttribute("myAccounts", myAccounts);
        model.addAttribute("bankAccounts", bankAccounts);

        return "transfer/third"; //view
    }

    @GetMapping("/transfer-module/third-party/transfer")
    public String transferThirdParty(@RequestParam(name = "amount") String amount, @RequestParam(name = "from_id") String fromId, @RequestParam(name = "to_id") String toId) throws Exception {
        logger.warn("Transfer third party.");

        Account fromAccount = accountRepository.findById(Long.valueOf(fromId)).get();
        Account toAccount = accountRepository.findById(Long.valueOf(toId)).get();

        if (Long.parseLong(amount) <= 0) {
            throw new Exception("Monto invalido");
        }

        if (fromAccount.getBalance().compareTo(BigDecimal.valueOf(Long.parseLong(amount))) < 1) {
            throw new Exception("El monto de pago sobrepasa el balance de la cuenta.");
        }

        // create negative transaction on account
        Transaction negativeTransaction = new Transaction();
        negativeTransaction.setDescription("Transferencia a terceros, a: " + toAccount.getIdentifier());
        negativeTransaction.setAmount(BigDecimal.valueOf(Long.parseLong(amount) * -1));
        negativeTransaction.setType("DEBIT");
        negativeTransaction.setAccount(fromAccount);
        negativeTransaction.setCreatedAt(Instant.now());
        transactionRepository.save(negativeTransaction);

        fromAccount.setBalance(fromAccount.getBalance().subtract(BigDecimal.valueOf(Long.parseLong(amount))));
        accountRepository.save(fromAccount);

        // create positive transaction on account
        Transaction positiveTransaction = new Transaction();
        positiveTransaction.setDescription("Transferencia a terceros, desde: " + fromAccount.getIdentifier());
        positiveTransaction.setAmount(BigDecimal.valueOf(Long.parseLong(amount)));
        positiveTransaction.setType("CREDIT");
        positiveTransaction.setAccount(toAccount);
        positiveTransaction.setCreatedAt(Instant.now());
        transactionRepository.save(positiveTransaction);

        toAccount.setBalance(toAccount.getBalance().add(BigDecimal.valueOf(Long.parseLong(amount))));
        accountRepository.save(toAccount);

        return "redirect:/transfer-module/third-party";
    }

    @GetMapping("/transfer-module/ach")
    public String ach(Model model, HttpServletRequest request) throws Exception {
        logger.warn("Transfer ACH.");

        Bank bank = getBankFromSession(request);
        Customer customer = getCustomerFromSession(request);
        List<Account> myAccounts = accountRepository.findByCustomerAndBank(customer, bank);
        List<Account> allAccounts = accountRepository.findAll();

        model.addAttribute("bank", bank);
        model.addAttribute("customer", customer);
        model.addAttribute("myAccounts", myAccounts);
        model.addAttribute("allAccounts", allAccounts);

        return "transfer/ach"; //view
    }

    @GetMapping("/transfer-module/ach/transfer")
    public String transferAch(@RequestParam(name = "amount") String amount, @RequestParam(name = "from_id") String fromId, @RequestParam(name = "to_id") String toId) throws Exception {
        logger.warn("Transfer ACH.");

        Account fromAccount = accountRepository.findById(Long.valueOf(fromId)).get();
        Account toAccount = accountRepository.findById(Long.valueOf(toId)).get();

        if (Long.parseLong(amount) <= 0) {
            throw new Exception("Monto invalido");
        }

        if (fromAccount.getBalance().compareTo(BigDecimal.valueOf(Long.parseLong(amount))) < 1) {
            throw new Exception("El monto de pago sobrepasa el balance de la cuenta.");
        }

        // create negative transaction on account
        Transaction negativeTransaction = new Transaction();
        negativeTransaction.setDescription("Transferencia ACH, a: " + toAccount.getFullIdentifier());
        negativeTransaction.setAmount(BigDecimal.valueOf(Long.parseLong(amount) * -1));
        negativeTransaction.setType("DEBIT");
        negativeTransaction.setAccount(fromAccount);
        negativeTransaction.setCreatedAt(Instant.now());
        transactionRepository.save(negativeTransaction);

        fromAccount.setBalance(fromAccount.getBalance().subtract(BigDecimal.valueOf(Long.parseLong(amount))));
        accountRepository.save(fromAccount);

        // create positive transaction on account
        Transaction positiveTransaction = new Transaction();
        positiveTransaction.setDescription("Transferencia ACH, desde: " + fromAccount.getFullIdentifier());
        positiveTransaction.setAmount(BigDecimal.valueOf(Long.parseLong(amount)));
        positiveTransaction.setType("CREDIT");
        positiveTransaction.setAccount(toAccount);
        positiveTransaction.setCreatedAt(Instant.now());
        transactionRepository.save(positiveTransaction);

        toAccount.setBalance(toAccount.getBalance().add(BigDecimal.valueOf(Long.parseLong(amount))));
        accountRepository.save(toAccount);

        return "redirect:/transfer-module/ach";
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
