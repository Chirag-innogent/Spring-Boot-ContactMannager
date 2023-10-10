package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepositary;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepositary contactRepositary;
	
	//method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model, Principal  principal) {
		String userName=principal.getName();
		System.out.println(userName);
		User user = userRepository.findByEmail(userName);
		model.addAttribute("user",user); 
		
	}
	
	@RequestMapping("/index")
	public String index(Model  model,Principal principal) {
		return "normal/user_dashboard";
	}
	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title","Add contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	//adding contact to database
	@PostMapping("/process-contact")
	public String addContact(@ModelAttribute Contact contact,@RequestParam("profileImage")MultipartFile   file,Principal principal,HttpSession session) {
		try {
			User  user=this.userRepository.findByEmail(principal.getName());
			//processing and uploading file
			
			if(file.isEmpty()) {
				//if file is empty then try our message
				contact.setImage("contactdefault.png");
			}else {
				//upload the file to folder and update the name to contact
				contact.setImage(file.getOriginalFilename());
				File savefile = new ClassPathResource("static/images").getFile();
				
				Path path = Paths.get(savefile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is uploaded");
			}
			
			user.getContacts().add(contact);
			contact.setUser(user);
			userRepository.save(user);
			System.out.println("Data "+contact);
			System.out.println("add to data");
			//success message 
			session.setAttribute("message",new Message("Your contact is added","success"));
		} catch (Exception e) {
			System.out.println("ERROR:"+e.getMessage());
			e.printStackTrace();
			//error message
			session.setAttribute("message",new Message("Something went wrong","danger"));
		}
				return "normal/add_contact_form";
	}
	//show all contacts
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page")int page, Model m,Principal principle) {
		m.addAttribute("title","show user contacts");
		
		User user = userRepository.findByEmail(principle.getName());
		Pageable pageable = PageRequest.of(page, 3);
		Page<Contact> contacts = contactRepositary.findByUser(user.getId(),pageable);
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentPage",page);
		m.addAttribute("totalPages",contacts.getTotalPages());
		return "normal/show_contacts";
	}
	
	//showing particulars of a contact
	@GetMapping("/{cId}/contact")
	public String showContactsDetail(@PathVariable ("cId")Integer cId,Model m,Principal principal)
	{
		System.out.println("CID"+cId);
		Optional<Contact> contactOptional = contactRepositary.findById(cId);
		if(contactOptional.isEmpty()) {

			return "normal/contact_detail";
		}
		Contact contact = contactOptional.get();
		

		//validating the contact for each user
		String name = principal.getName();
		User user = userRepository.findByEmail(name);
		if((user.getId())==(contact.getUser().getId())) {
			m.addAttribute("contact",contact);
			m.addAttribute("title",contact.getName());
		}
		return "normal/contact_detail";
	}
	
	//deleting contact handler
	@GetMapping("/delete/{cId}")
	public String deleteHandler(@PathVariable ("cId")Integer cId,Principal principal,HttpSession session ) {
		Optional<Contact> contactOptional = contactRepositary.findById(cId);
		if(contactOptional.isEmpty()) {

			return "redirect:/user/show_contacts/0";
		}
		Contact contact = contactOptional.get();
		//validating the contact for each user
		String name = principal.getName();
		User user = userRepository.findByEmail(name);
		if((user.getId())==(contact.getUser().getId())) {
			contactRepositary.delete(contact);
			session.setAttribute("message",new Message("Contact Deleted Succesfully...","success"));
		}
		return "redirect:/user/show-contacts/0";

	}
	
	//update form handler
	@PostMapping("/open-contact/{cid}")
	public String updateForm(@PathVariable("cid")Integer cid,Model m) {
		m.addAttribute("title","Update Contact");
		Optional<Contact> contactOptional = contactRepositary.findById(cid);
		Contact contact = contactOptional.get();
		System.out.println(contact);
		m.addAttribute("contact",contact);
		return "normal/update-form";
	}

	//method update handler
	@PostMapping("/process-update")
	public String updateContact(@ModelAttribute Contact contact,@RequestParam("profileImage")MultipartFile   file,Principal principal,HttpSession session) {
		try {
			User  user=this.userRepository.findByEmail(principal.getName());
			//processing and uploading file
			
			if(file.isEmpty()) {
				//if file is empty then try our message
			}else {
				//upload the file to folder and update the name to contact
				contact.setImage(file.getOriginalFilename());
				File savefile = new ClassPathResource("static/images").getFile();
				
				Path path = Paths.get(savefile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is uploaded");
			}
			
			user.getContacts().add(contact);
			contact.setUser(user);
			System.out.println("Data "+contact.getCid());
			System.out.println("add to data");
			//success message 
			session.setAttribute("message",new Message("Your contact is added","success"));
		} catch (Exception e) {
			System.out.println("ERROR:"+e.getMessage());
			e.printStackTrace();
			//error message
			session.setAttribute("message",new Message("Something went wrong","danger"));
		}

		return "redirect:/user/show-contacts/0";
	}

}

