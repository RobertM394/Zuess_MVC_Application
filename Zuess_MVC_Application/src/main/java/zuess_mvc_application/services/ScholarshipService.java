package zuess_mvc_application.services;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import zuess_mvc_application.domain.OtterCoin;
import zuess_mvc_application.domain.Scholarship;
import zuess_mvc_application.repository.ScholarshipRepository;

@Service
public class ScholarshipService {

	@Autowired
	ScholarshipRepository scholarshipRepository;
	
	@Autowired 
	BlockchainService blockchainService;
	
	//grantNewScholarship() Creates a new scholarship in the database and calls approveAllowance() to approve an allowance on the blockchain
	public Scholarship grantNewScholarship(OtterCoin otterCoin, int recipient_id, String recipient_eth_id, int amount, java.sql.Date date_expires) throws Exception {
		
		Date date_granted = new Date(System.currentTimeMillis());
		Scholarship scholarship = new Scholarship(recipient_id, recipient_eth_id, amount, date_granted, date_expires);
		
		//Persist scholarship object in database and approve scholarship allowance on Ethereum blockchain 
		scholarshipRepository.save(scholarship);
		final TransactionReceipt receipt = blockchainService.approveAllowance(otterCoin, recipient_eth_id, amount);
		
		System.out.print("Approve Allowance transaction receipt: " + receipt);
		return scholarship;
	}
	
	public boolean syncEthereumAndDatabaseAllowances() {
		return true;
	}
	
	public List<Scholarship> getActiveScholarships() {
		List<Scholarship> scholarshipsList = new ArrayList<>();
		scholarshipsList = scholarshipRepository.getActiveScholarships();
		return scholarshipsList;
	}
	
	public List<Scholarship> getScholarshipsByUserId(int userId){
		List<Scholarship> scholarshipsList = new ArrayList<>();
		scholarshipsList = scholarshipRepository.getActiveScholarshipsByUserId(userId);
		return scholarshipsList;
	}
	
}
