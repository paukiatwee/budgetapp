package io.budgetapp.mocks;

import io.budgetapp.crypto.PasswordEncoder;
import io.budgetapp.dao.*;
import io.budgetapp.model.form.SignUpForm;
import io.budgetapp.service.FinanceService;
import org.junit.Test;
import static org.mockito.Mockito.mock;

public class FinanceServiceTest {

    @Test
    public void addUserTest(){

        UserDAO userDAOMock = mock(UserDAO.class);
        BudgetDAO budgetDAOMock = mock(BudgetDAO.class);
        BudgetTypeDAO budgetTypeDAOMock = mock(BudgetTypeDAO.class);
        CategoryDAO categoryDAOMock = mock(CategoryDAO.class);
        TransactionDAO transactionDAOMock = mock(TransactionDAO.class);
        RecurringDAO recurringDAOMock = mock(RecurringDAO.class);
        AuthTokenDAO authTokenDAOMock = mock(AuthTokenDAO.class);
        PasswordEncoder passwordEncoderMock = mock(PasswordEncoder.class);

        SignUpForm signUpForm = mock(SignUpForm.class);

        FinanceService financeService = new FinanceService(userDAOMock, budgetDAOMock, budgetTypeDAOMock, categoryDAOMock, transactionDAOMock, recurringDAOMock, authTokenDAOMock, passwordEncoderMock);

        financeService.addUser(signUpForm);

        //add verification

        
    }

}
