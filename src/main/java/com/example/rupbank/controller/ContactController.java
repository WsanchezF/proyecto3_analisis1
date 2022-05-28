package com.example.rupbank.controller;

import com.example.rupbank.model.Contact;
import com.example.rupbank.repository.ContactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.Instant;
import javax.validation.Valid;

@Controller
public class ContactController implements WebMvcConfigurer {
    Logger logger = LoggerFactory.getLogger(HomepageController.class);

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private JavaMailSender emailSender;

    @GetMapping("/contact-us")
    public String main(Contact contact) {
        return "contact/index"; //view
    }

    @PostMapping("/contact-us")
    public String checkContactForm(@Valid Contact contact, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "contact/index";
        }

        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            String ip = socket.getLocalAddress().getHostAddress();

            contact.setCreatedFromIp(ip);
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }

        contact.setCreatedAt(Instant.now());
        contactRepository.save(contact);

        sendThankYouEmail(contact);

        return "redirect:/contact-us";
    }

    private void sendThankYouEmail(Contact contact) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("rupbankumg@gmail.com");
        message.setTo(contact.getEmail());
        message.setSubject("Gracias por su mensaje.");
        message.setText(String.format("Hola %s, estamos agradecidos con su mensaje de contacto, Gracias.", contact.getFullName()));

        emailSender.send(message);
    }
}
