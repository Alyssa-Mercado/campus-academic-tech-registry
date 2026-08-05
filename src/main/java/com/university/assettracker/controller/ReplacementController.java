package com.university.assettracker.controller;

import com.university.assettracker.service.ReplacementService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/recommendations")
public class ReplacementController {

    private final ReplacementService replacementService;

    public ReplacementController(ReplacementService replacementService) {
        this.replacementService = replacementService;
    }

    @GetMapping
    public String list(Model model) {
        var recommendations = replacementService.getRecommendations();
        model.addAttribute("recommendations", recommendations);
        model.addAttribute("totalCount", recommendations.size());
        model.addAttribute("pageTitle", "Replacement Recommendations");
        return "recommendations/list";
    }
}
