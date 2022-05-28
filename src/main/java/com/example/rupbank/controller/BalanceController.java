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

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Controller
public class BalanceController {
    Logger logger = LoggerFactory.getLogger(HomepageController.class);

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CheckRepository checkRepository;

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private CreditCardTransactionRepository creditCardTransactionRepository;

    @GetMapping("/balance-module")
    public String main() throws Exception {
        logger.warn("Balance home.");

        return "balance/index"; //view
    }

    @GetMapping("/balance-module/balance-inquiry")
    public String balanceInquiry(Model model, HttpServletRequest request) throws Exception {
        logger.warn("Consulta de saldos.");

        Bank bank = getBankFromSession(request);
        Customer customer = getCustomerFromSession(request);
        List<Account> accounts = accountRepository.findByCustomerAndBank(customer, bank);

        model.addAttribute("bank", bank);
        model.addAttribute("accounts", accounts);

        return "balance/balance_inquiry"; //view
    }

    @GetMapping("/balance-module/balance-inquiry/new")
    public String newBalanceInquiry(HttpServletRequest request) throws Exception {
        logger.warn("Consulta de saldos.");

        Bank bank = getBankFromSession(request);
        Customer customer = getCustomerFromSession(request);
        createBankAccount(customer, bank);

        return "redirect:/balance-module/balance-inquiry";
    }

    @GetMapping("/balance-module/account-statement-inquiry")
    public String statementInquiry(Model model, HttpServletRequest request) throws Exception {
        logger.warn("Consulta estado de cuenta.");

        Bank bank = getBankFromSession(request);
        Customer customer = getCustomerFromSession(request);
        List<Account> accounts = accountRepository.findByCustomerAndBank(customer, bank);

        model.addAttribute("bank", bank);
        model.addAttribute("accounts", accounts);

        return "balance/account_statement_inquiry"; //view
    }

    @GetMapping("/balance-module/account-statement-inquiry/{accountId}")
    public String accountsStatementInquiry(Model model, HttpServletRequest request, @PathVariable String accountId) throws Exception {
        logger.warn("Consulta estado de cuenta.");

        Bank bank = getBankFromSession(request);
        Customer customer = getCustomerFromSession(request);
        Account account = accountRepository.findById(Long.valueOf(accountId)).get();
        List<Transaction> transactions = transactionRepository.findByAccount(account);

        model.addAttribute("bank", bank);
        model.addAttribute("customer", customer);
        model.addAttribute("account", account);
        model.addAttribute("transactions", transactions);

        return "balance/account_statement_inquiry_transactions"; //view
    }

    @GetMapping("/balance-module/check-inquiry")
    public String checkInquiry(Model model, HttpServletRequest request) throws Exception {
        logger.warn("Consulta de cheques.");

        Bank bank = getBankFromSession(request);
        Customer customer = getCustomerFromSession(request);

        model.addAttribute("bank", bank);
        model.addAttribute("customer", customer);


        return "balance/check_inquiry"; //view
    }

    @GetMapping("/balance-module/card-inquiry")
    public String cardInquiry(Model model, HttpServletRequest request) throws Exception {
        logger.warn("Consulta de tarjetas.");

        Bank bank = getBankFromSession(request);
        Customer customer = getCustomerFromSession(request);
        List<CreditCard> creditCards = creditCardRepository.findByCustomerAndBank(customer, bank);

        model.addAttribute("bank", bank);
        model.addAttribute("customer", customer);
        model.addAttribute("cards", creditCards);

        return "balance/card_inquiry"; //view
    }

    @GetMapping("/balance-module/card-inquiry/{cardId}")
    public String tranactionsCardInquiry(Model model, HttpServletRequest request, @PathVariable String cardId) throws Exception {
        logger.warn("Consulta de tarjetas.");

        Bank bank = getBankFromSession(request);
        Customer customer = getCustomerFromSession(request);
        CreditCard creditCard = creditCardRepository.findById(Long.valueOf(cardId)).get();
        List<CreditCardTransaction> transactions = creditCardTransactionRepository.findByCard(creditCard);

        model.addAttribute("bank", bank);
        model.addAttribute("customer", customer);
        model.addAttribute("card", creditCard);
        model.addAttribute("transactions", transactions);

        return "balance/card_inquiry_transactions"; //view
    }

    @GetMapping("/balance-module/card-inquiry/new")
    public String newCardInquiry(Model model, HttpServletRequest request) throws Exception {
        logger.warn("Consulta de tarjetas.");

        Bank bank = getBankFromSession(request);
        Customer customer = getCustomerFromSession(request);

        CreditCardNumberGenerator creditCardNumberGenerator = new CreditCardNumberGenerator();
        String number = creditCardNumberGenerator.generate("4422", 16);
        Instant futureDate = between(Instant.parse("2022-06-01T18:35:24.00Z"), Instant.parse("2025-12-31T18:35:24.00Z"));

        // generate credit card
        CreditCard creditCard = new CreditCard();
        creditCard.setBalance(BigDecimal.valueOf(1000));
        creditCard.setBank(bank);
        creditCard.setCustomer(customer);
        creditCard.setNameOnCard(customer.getFullName());
        creditCard.setLimit(BigDecimal.valueOf(100));
        creditCard.setCreatedAt(Instant.now());
        creditCard.setExpireDate(futureDate);
        creditCard.setNumber(number);

        creditCardRepository.save(creditCard);

        // create transaction
        CreditCardTransaction transaction = new CreditCardTransaction();
        transaction.setAmount(creditCard.getBalance());
        transaction.setCreatedAt(Instant.now());
        transaction.setDescription("Primer saldo patrocinado por el banco.");
        transaction.setType("DEPOSIT");
        transaction.setCreditCard(creditCard);

        creditCardTransactionRepository.save(transaction);

        return "redirect:/balance-module/card-inquiry";
    }

    private static Instant between(Instant startInclusive, Instant endExclusive) {
        long startSeconds = startInclusive.getEpochSecond();
        long endSeconds = endExclusive.getEpochSecond();
        long random = ThreadLocalRandom
                .current()
                .nextLong(startSeconds, endSeconds);

        return Instant.ofEpochSecond(random);
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

    private void createBankAccount(Customer customer, Bank bank) {
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
}
