package com.example.VaultPay.service.user;


import com.example.VaultPay.dao.UserRepo;
import com.example.VaultPay.model.user.User;
import com.example.VaultPay.model.user.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserService implements UserDetailsService {

    @Autowired
    private UserRepo repo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = repo.findByUsername(username);

        if(user == null){
            System.out.println("USER NOT FOUND!!");
            throw  new UsernameNotFoundException("USER NOT FOUND");
        }

        return new UserPrincipal(user);
    }
}
