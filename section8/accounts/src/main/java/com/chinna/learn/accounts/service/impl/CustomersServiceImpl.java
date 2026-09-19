package com.chinna.learn.accounts.service.impl;


import com.chinna.learn.accounts.dto.AccountsDto;
import com.chinna.learn.accounts.dto.CardsDto;
import com.chinna.learn.accounts.dto.CustomerDetailsDto;
import com.chinna.learn.accounts.dto.LoansDto;
import com.chinna.learn.accounts.entity.Accounts;
import com.chinna.learn.accounts.entity.Customer;
import com.chinna.learn.accounts.exception.ResourceNotFoundException;
import com.chinna.learn.accounts.mapper.AccountsMapper;
import com.chinna.learn.accounts.mapper.CustomerMapper;
import com.chinna.learn.accounts.repository.AccountsRepository;
import com.chinna.learn.accounts.repository.CustomerRepository;
import com.chinna.learn.accounts.service.ICustomersService;
import com.chinna.learn.accounts.service.client.CardsFeignClient;
import com.chinna.learn.accounts.service.client.LoansFeignClient;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomersServiceImpl implements ICustomersService {

    private AccountsRepository accountsRepository;
    private CustomerRepository customerRepository;
    private CardsFeignClient cardsFeignClient;
    private LoansFeignClient loansFeignClient;

    /**
     * @param mobileNumber - Input Mobile Number
     * @return Customer Details based on a given mobileNumber
     */
    @Override
    public CustomerDetailsDto fetchCustomerDetails(String mobileNumber) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );
        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                () -> new ResourceNotFoundException("Account", "customerId", customer.getCustomerId().toString())
        );

        CustomerDetailsDto customerDetailsDto = CustomerMapper.mapToCustomerDetailsDto(customer, new CustomerDetailsDto());
        customerDetailsDto.setAccountsDto(AccountsMapper.mapToAccountsDto(accounts, new AccountsDto()));

        ResponseEntity<LoansDto> loansDtoResponseEntity = loansFeignClient.fetchLoanDetails(mobileNumber);
        customerDetailsDto.setLoansDto(loansDtoResponseEntity.getBody());

        ResponseEntity<CardsDto> cardsDtoResponseEntity = cardsFeignClient.fetchCardDetails(mobileNumber);
        customerDetailsDto.setCardsDto(cardsDtoResponseEntity.getBody());

        return customerDetailsDto;

    }
}
