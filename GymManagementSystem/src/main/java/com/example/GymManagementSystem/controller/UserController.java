package com.example.GymManagementSystem.controller;

import com.example.GymManagementSystem.entity.User;
import com.example.GymManagementSystem.repository.UserRepository;
import com.example.GymManagementSystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepo;


    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userService.register(user);
    }


    @PostMapping("/login")
    public Object login(@RequestBody User user) {
        return userService.login(user);
    }


    @GetMapping("/all/{userId}")
    public Object getAllUsers(@PathVariable int userId){

        User u = userRepo.findById(userId).get();

        if (u.getRole().equals("OWNER")) {
            return userRepo.findAll();
        }

        return "Access Denied!";
    }


    @GetMapping("/details/{id}/{userId}")
    public Object getUserById(@PathVariable int id,
                              @PathVariable int userId) {

        User u = userRepo.findById(userId).get();

        if (u.getRole().equals("OWNER")) {
            return userRepo.findById(id).get();
        }

        return "Access Denied!";
    }

    @DeleteMapping("/{id}/{userId}")
    public String deleteUser(@PathVariable int id,
                             @PathVariable int userId) {

        User u = userRepo.findById(userId).get();

        if (u.getRole().equals("OWNER")) {

            userRepo.deleteById(id);
            return "User Deleted Successfully";
        }

        return "Access Denied!";
    }
}