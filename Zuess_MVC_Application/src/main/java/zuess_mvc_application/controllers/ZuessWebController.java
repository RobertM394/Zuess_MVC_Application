package zuess_mvc_application.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigInteger;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpSession;

import zuess_mvc_application.services.*;
import zuess_mvc_application.domain.*;
import zuess_mvc_application.repository.UserRepository;

@Controller
public class ZuessWebController {
	
	@Autowired
	CustomUserDetailsService customUserDetailsService;
	
	@Autowired
	BlockchainService blockchainService;
	
	@Autowired
	HttpSession session;
	
	@Autowired
	UserRepository userRepo;
	
	OtterCoin otterCoin;
	
	/***HTTP Routes
	 * @throws ExecutionException 
	 * @throws InterruptedException ***/
	@GetMapping("/registration")
	public String getUserRegistrationForm(Model model) throws InterruptedException, ExecutionException {
		model.addAttribute("user", new User());
		return "new_user_registration";
	}
	
// TODO: Prevent repeated information signups (ensure email is unique, return error if already present)
	@PostMapping("/submitNewUserRegistration")
	public String persistNewUser(Model model, User user) {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		userRepo.save(user);
		return "acct_create_success";
	}
	 
	@GetMapping("/accountInfo")
	public String getUserAccountDetails(Principal principal) {
		String email = principal.getName();
		System.out.println("\n Email is: " + email + "\n");
		User user = customUserDetailsService.retrieveUserByEmail(email);
		session.setAttribute("user", user);
		return "standard_user_account";
	}
	
	@GetMapping("")
	public String getHomepage() {
		return "index.html";
	}
	
	@GetMapping("/adminPortal")
	public String getAdminPortal(HttpSession session, Model model) throws Exception {
		List<String> ethereumAccountsList = blockchainService.getEthereumUserAccounts();
		session.setAttribute("ethereumAccountsList", ethereumAccountsList);
		return "admin_portal";
	}
	
	@PostMapping("/deploySmartContract")
	public String deploySmartContract(HttpSession session,
			@RequestParam("contractType") String contractType,
			@RequestParam("initialContractFunds") int initialContractFunds,
			@RequestParam("ethPrivateKey") String ethPrivateKey
			) throws Exception {
		
		otterCoin = blockchainService.deploySmartContract(contractType, ethPrivateKey, initialContractFunds);
		session.setAttribute("deployed", true);
		session.setAttribute("contractType", contractType);
		return "admin_portal";
	}
	
	@PostMapping("/transferFunds")
	public String transferFundsToEthAccounts(HttpSession session,
			@RequestParam("accounts") List<String> accounts,
			@RequestParam("transferAmount") int transferAmount
			) throws Exception {
		blockchainService.transferFunds(otterCoin, accounts, transferAmount);
		return "admin_portal";
	}
	
	//If feasible, this should eventually replace all above PostMapping routes by using Optional Request Params
	//Or we could look at using Ajax
	@PostMapping("/adminActions")
	public String administratorActions(HttpSession session,
			@RequestParam("getBalanceOfAddress") Optional<String> balanceAddress
			) throws Exception {
		
		if (balanceAddress != null) {
		BigInteger accountBalance = blockchainService.getBalance(otterCoin, balanceAddress.get());
		session.setAttribute("accountBalance", accountBalance);
		}
		return "admin_portal";
	}
	
}
