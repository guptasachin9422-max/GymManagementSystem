package com.example.GymManagementSystem.service;

import com.example.GymManagementSystem.entity.User;
import com.example.GymManagementSystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.GymManagementSystem.entity.Member;
import com.example.GymManagementSystem.repository.MemberRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MemberRepository memberRepository;

    public User register(User user) {
        return userRepository.save(user);
    }

    public Object login(User user) {

        User dbUser =
                userRepository.findByUsername(user.getUsername());

        if (dbUser == null) {
            return "User Not Found";
        }

        if (!dbUser.getPassword().equals(user.getPassword())) {
            return "Wrong Password";
        }

        Member member =
                memberRepository.findByUserId(dbUser.getId());

        if (member == null) {
            return "Login Successful. Please create your Member Profile.";
        }

        return member;
    }
}