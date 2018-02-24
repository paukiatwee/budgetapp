package io.budgetapp.mocks;

import io.budgetapp.application.DataConstraintException;
import io.budgetapp.crypto.PasswordEncoder;
import io.budgetapp.dao.*;
import io.budgetapp.model.User;
import io.budgetapp.model.form.SignUpForm;
import io.budgetapp.service.FinanceService;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FinanceServiceTest {

    private UserDAO userDAOMock;
    private BudgetDAO budgetDAOMock;
    private BudgetTypeDAO budgetTypeDAOMock;
    private CategoryDAO categoryDAOMock;
    private TransactionDAO transactionDAOMock;
    private RecurringDAO recurringDAOMock;
    private AuthTokenDAO authTokenDAOMock;
    private PasswordEncoder passwordEncoderMock;
    private SignUpForm signUpFormMock;

    @Before
    public void initMocks(){
        userDAOMock = mock(UserDAO.class);
        budgetDAOMock = mock(BudgetDAO.class);
        budgetTypeDAOMock = mock(BudgetTypeDAO.class);
        categoryDAOMock = mock(CategoryDAO.class);
        transactionDAOMock = mock(TransactionDAO.class);
        recurringDAOMock = mock(RecurringDAO.class);
        authTokenDAOMock = mock(AuthTokenDAO.class);
        passwordEncoderMock = mock(PasswordEncoder.class);
        signUpFormMock = mock(SignUpForm.class);
    }


    //verifies that an exception is caught when adding a user with an existing username
    @Test(expected=DataConstraintException.class)
    public void addUserExistingUserNameTest(){
        //given
        FinanceService financeService = new FinanceService(userDAOMock, budgetDAOMock, budgetTypeDAOMock, categoryDAOMock, transactionDAOMock, recurringDAOMock, authTokenDAOMock, passwordEncoderMock);
        User userMock = mock(User.class);
        Optional<User> userOptional = Optional.of(userMock);
        SignUpForm form = new SignUpForm();
        form.setUsername("usernameExists");
        form.setPassword("password");
        when(userDAOMock.findByUsername("usernameExists")).thenReturn(userOptional);

        //when
        financeService.addUser(form);

        //then exception is caught via the @Test annotation
    }

}
