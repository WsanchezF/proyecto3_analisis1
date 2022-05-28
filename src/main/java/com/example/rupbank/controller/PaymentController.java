package com.example.rupbank.controller;

import com.example.rupbank.generator.CreditCardNumberGenerator;
import com.example.rupbank.model.*;
import com.example.rupbank.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Controller
public class PaymentController {
    Logger logger = LoggerFactory.getLogger(HomepageController.class);

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CreditCardTransactionRepository creditCardTransactionRepository;

    @Autowired
    private LoanRepository loanRepository;

    @GetMapping("/payment-module")
    public String main(Model model, HttpServletRequest request) throws Exception {
        logger.warn("Payment home.");

        return "payment/index"; //view
    }

    @GetMapping("/payment-module/loan-payment")
    public String loanPayment(Model model, HttpServletRequest request) throws Exception {
        logger.warn("Pago de prestamo.");

        Bank bank = getBankFromSession(request);
        Customer customer = getCustomerFromSession(request);
        List<Loan> loans = loanRepository.findByCustomerAndBank(customer, bank);
        List<Account> accounts = accountRepository.findByCustomerAndBank(customer, bank);

        model.addAttribute("bank", bank);
        model.addAttribute("customer", customer);
        model.addAttribute("loans", loans);
        model.addAttribute("accounts", accounts);

        return "payment/loan_payment"; //view
    }

    @GetMapping("/payment-module/loan-payment/new")
    public String newLoanPayment(Model model, HttpServletRequest request) throws Exception {
        logger.warn("Pago de prestamo.");

        Bank bank = getBankFromSession(request);
        Customer customer = getCustomerFromSession(request);

        CreditCardNumberGenerator creditCardNumberGenerator = new CreditCardNumberGenerator();
        String number = creditCardNumberGenerator.generate("001", 8);

        Loan loan = new Loan();
        loan.setAmount(BigDecimal.valueOf(10000));
        loan.setBalance(BigDecimal.valueOf(10000));
        loan.setBank(bank);
        loan.setCreatedAt(Instant.now());
        loan.setCustomer(customer);
        loan.setNumber("P-"+ number);
        loanRepository.save(loan);

        return "redirect:/payment-module/loan-payment";
    }

    @GetMapping("/payment-module/loan-payment/pay/{loanId}")
    public String payLoanPayment(Model model, HttpServletRequest request, @PathVariable String loanId, @RequestParam(name = "amount") String amount, @RequestParam(name = "account_id") String accountId) throws Exception {
        logger.warn("Pago de prestamo.");

        Loan loan = loanRepository.findById(Long.valueOf(loanId)).get();
        Account account = accountRepository.findById(Long.valueOf(accountId)).get();

        logger.warn(String.format("Tarjeta: %s", loan.getNumber()));
        logger.warn(String.format("Cuenta: %s", account.getName()));
        logger.warn(String.format("Monto: %s", amount));

        if (Long.parseLong(amount) <= 0) {
            throw new Exception("Monto invalido");
        }

        if (account.getBalance().compareTo(BigDecimal.valueOf(Long.parseLong(amount))) < 1) {
            throw new Exception("El monto de pago sobrepasa el balance de la cuenta.");
        }

        // create negative transaction on account
        Transaction transaction = new Transaction();
        transaction.setDescription("Pago de prestamo: " + loan.getNumber());
        transaction.setAmount(BigDecimal.valueOf(Long.parseLong(amount) * -1));
        transaction.setType("DEBIT");
        transaction.setAccount(account);
        transaction.setCreatedAt(Instant.now());
        transactionRepository.save(transaction);

        // update balance on loan!
        loan.setBalance(loan.getBalance().subtract(BigDecimal.valueOf(Long.parseLong(amount))));
        loanRepository.save(loan);

        account.setBalance(account.getBalance().subtract(BigDecimal.valueOf(Long.parseLong(amount))));
        accountRepository.save(account);

        return "redirect:/payment-module/credit-card-payment";
    }

    @GetMapping("/payment-module/credit-card-payment")
    public String creditCardPayment(Model model, HttpServletRequest request) throws Exception {
        logger.warn("Pago de tarjeta de crédito.");

        Bank bank = getBankFromSession(request);
        Customer customer = getCustomerFromSession(request);
        List<CreditCard> creditCards = creditCardRepository.findByCustomerAndBank(customer, bank);
        List<Account> accounts = accountRepository.findByCustomerAndBank(customer, bank);

        model.addAttribute("bank", bank);
        model.addAttribute("customer", customer);
        model.addAttribute("cards", creditCards);
        model.addAttribute("accounts", accounts);

        return "payment/credit_card_payment"; //view
    }

    @GetMapping("/payment-module/credit-card-payment/pay/{creditCardId}")
    public String payCreditCard(Model model, HttpServletRequest request, @PathVariable String creditCardId, @RequestParam(name = "amount") String amount, @RequestParam(name = "account_id") String accountId) throws Exception {
        logger.warn("Pago de tarjeta de crédito.");

        CreditCard creditCard = creditCardRepository.findById(Long.valueOf(creditCardId)).get();
        Account account = accountRepository.findById(Long.valueOf(accountId)).get();

        logger.warn(String.format("Tarjeta: %s", creditCard.getNumber()));
        logger.warn(String.format("Cuenta: %s", account.getName()));
        logger.warn(String.format("Monto: %s", amount));

        if (Long.parseLong(amount) <= 0) {
            throw new Exception("Monto invalido");
        }

        if (account.getBalance().compareTo(BigDecimal.valueOf(Long.parseLong(amount))) < 1) {
            throw new Exception("El monto de pago sobrepasa el balance de la cuenta.");
        }

        // create negative transaction on account
        Transaction transaction = new Transaction();
        transaction.setDescription("Pago de tarjeta de cŕedito: " + creditCard.getNumber());
        transaction.setAmount(BigDecimal.valueOf(Long.parseLong(amount) * -1));
        transaction.setType("DEBIT");
        transaction.setAccount(account);
        transaction.setCreatedAt(Instant.now());
        transactionRepository.save(transaction);

        // create positive transaction on card
        CreditCardTransaction creditCardTransaction = new CreditCardTransaction();
        creditCardTransaction.setCreditCard(creditCard);
        creditCardTransaction.setType("CREDIT");
        creditCardTransaction.setDescription("Pago de tarjeta de cŕedito: " + creditCard.getNumber() + " con cuenta: " + account.getNumber());
        creditCardTransaction.setCreatedAt(Instant.now());
        creditCardTransaction.setAmount(BigDecimal.valueOf(Long.parseLong(amount)));
        creditCardTransactionRepository.save(creditCardTransaction);

        // more balance now on card!
        creditCard.setBalance(creditCard.getBalance().add(BigDecimal.valueOf(Long.parseLong(amount))));
        creditCardRepository.save(creditCard);

        account.setBalance(account.getBalance().subtract(BigDecimal.valueOf(Long.parseLong(amount))));
        accountRepository.save(account);

        return "redirect:/payment-module/credit-card-payment";
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
