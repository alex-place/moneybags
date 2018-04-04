package com.aeplace.moneybag;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.aeplace.moneybag.value.EncounterValue;

@Controller
public class MainController {

	@GetMapping("/encounterbuilder")
    public String greetingForm(Model model) {
        model.addAttribute("encounterValue", new EncounterValue());
        return "encounterbuilder";
    }

    @PostMapping("/encounterbuilder")
    public String greetingSubmit(@ModelAttribute EncounterValue encounterValue) {
        return "encounterbuilder_output";
    }

}
