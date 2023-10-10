package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	private UserRepository userRepository;

	@RequestMapping("/")
	public String home(Model m) {
		m.addAttribute("title","Home - Smart contact Manager");
		return "home";
	}
	@RequestMapping("/signup")
	public String signup(Model m) {
		m.addAttribute("title","Register - Smart contact Manager");
		m.addAttribute("user",new User());
		return "signup";
	}
	//handler for registering user
	@PostMapping(value="/do_register")
	public String registerUser(@Valid @ModelAttribute ("user")User user,BindingResult result,@RequestParam(value = "agreement",defaultValue = "false")boolean agreement ,Model model,HttpSession session) {
		try{if(!agreement) {
			System.out.println("you have not agreed to terms and conditions");
			throw new Exception("you have not agreed to terms and conditions");
		}
		if(result.hasErrors()) {
			model.addAttribute("user", user);
			
			return "signup";
		}
		user.setRole("ROLE_USER");
		user.setEnabled(true);
		user.setImageUrl("default.png");
		user.setPassword(passwordEncoder.encode(user.getPassword()));
	
		System.out.println("Agreement:"+agreement);
		System.out.println(user);
		User user2=this.userRepository.save(user);
		
		model.addAttribute("user",new User());
		session.setAttribute("message",new Message("Successfully Registered!!","alert-success"));
		return "signup";

		}catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message", new Message("Something Went Wrong!!"+e.getMessage(),"alert-danger"));
			return "signup";
		}
	
	}
	
	//handler for custom login page
	@GetMapping("/ulogin")
	public String customLogin(Model model) {
		model.addAttribute("title","Login page");
		return "login";
	}
}
