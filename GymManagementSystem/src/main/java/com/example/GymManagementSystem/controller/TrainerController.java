package com.example.GymManagementSystem.controller;

import com.example.GymManagementSystem.entity.Member;
import com.example.GymManagementSystem.entity.Trainer;
import com.example.GymManagementSystem.service.MemberService;
import com.example.GymManagementSystem.service.TrainerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@RestController
@RequestMapping("/trainers")
public class TrainerController {

    @Autowired
    private TrainerService trainerService;
    @Autowired
    private MemberService memberService;


    @PostMapping
    public Trainer addTrainer(
            @RequestBody Trainer trainer) {

        return trainerService.saveTrainer(trainer);
    }

    @GetMapping
    public List<Trainer> getAllTrainers() {
        return trainerService.getAllTrainers();
    }

    @GetMapping("/{id}")
    public Trainer getTrainerById(
            @PathVariable Long id) {

        return trainerService.getTrainerById(id);
    }


    @PutMapping("/{id}")
    public Trainer updateTrainer(
            @PathVariable Long id,
            @RequestBody Trainer trainer) {

        return trainerService.updateTrainer(id, trainer);
    }


    @DeleteMapping("/{id}")
    public String deleteTrainer(
            @PathVariable Long id) {

        return trainerService.deleteTrainer(id);
    }
    @PostMapping("/{id}")
    public Member updateMember(@PathVariable Integer id , @RequestBody Member member){
        return memberService.updateMember(id ,member);
    }
}