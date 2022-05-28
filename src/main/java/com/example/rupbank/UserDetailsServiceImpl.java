package com.example.rupbank;

import com.example.rupbank.model.Customer;
import org.springframework.util.StringUtils;
import com.example.rupbank.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String[] usernameAndBank = StringUtils.split(username, String.valueOf(Character.LINE_SEPARATOR));

        if (usernameAndBank == null || usernameAndBank.length != 2) {
            throw new UsernameNotFoundException("Username and bank must be provided");
        }

        Customer customer = customerRepository.getUserByUsername(usernameAndBank[0]);

        if (customer == null) {
            throw new UsernameNotFoundException("Could not find customer");
        }

        return new MyUserDetails(customer);
    }

}