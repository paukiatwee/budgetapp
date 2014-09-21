package io.budgetapp.service;

import io.budgetapp.application.DataConstraintException;
import io.budgetapp.crypto.PasswordEncoder;
import io.budgetapp.dao.AuthTokenDAO;
import io.budgetapp.dao.CategoryDAO;
import io.budgetapp.dao.BudgetDAO;
import io.budgetapp.dao.BudgetTypeDAO;
import io.budgetapp.dao.RecurringDAO;
import io.budgetapp.dao.TransactionDAO;
import io.budgetapp.dao.UserDAO;
import io.budgetapp.model.AccountSummary;
import io.budgetapp.model.AuthToken;
import io.budgetapp.model.Budget;
import io.budgetapp.model.BudgetType;
import io.budgetapp.model.Category;
import io.budgetapp.model.CategoryType;
import io.budgetapp.model.Group;
import io.budgetapp.model.Point;
import io.budgetapp.model.PointType;
import io.budgetapp.model.Recurring;
import io.budgetapp.model.Transaction;
import io.budgetapp.model.UsageSummary;
import io.budgetapp.model.User;
import io.budgetapp.model.form.LoginForm;
import io.budgetapp.model.form.SignUpForm;
import io.budgetapp.model.form.TransactionForm;
import io.budgetapp.model.form.budget.AddBudgetForm;
import io.budgetapp.model.form.budget.UpdateBudgetForm;
import io.budgetapp.model.form.recurring.AddRecurringForm;
import io.budgetapp.model.form.report.SearchFilter;
import io.budgetapp.model.form.user.Profile;
import io.budgetapp.util.Util;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 *
 */
public class FinanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FinanceService.class);
    private static final DateTimeFormatter SUMMARY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM");

    private final SessionFactory sessionFactory;
    private final UserDAO userDAO;
    private final BudgetDAO budgetDAO;
    private final BudgetTypeDAO budgetTypeDAO;
    private final CategoryDAO categoryDAO;
    private final TransactionDAO transactionDAO;
    private final RecurringDAO recurringDAO;
    private final AuthTokenDAO authTokenDAO;

    private final PasswordEncoder passwordEncoder;

    public FinanceService(SessionFactory sessionFactory, UserDAO userDAO, BudgetDAO budgetDAO, BudgetTypeDAO budgetTypeDAO, CategoryDAO categoryDAO, TransactionDAO transactionDAO, RecurringDAO recurringDAO, AuthTokenDAO authTokenDAO, PasswordEncoder passwordEncoder) {
        this.sessionFactory = sessionFactory;
        this.userDAO = userDAO;
        this.budgetDAO = budgetDAO;
        this.budgetTypeDAO = budgetTypeDAO;
        this.categoryDAO = categoryDAO;
        this.transactionDAO = transactionDAO;
        this.recurringDAO = recurringDAO;
        this.authTokenDAO = authTokenDAO;

        this.passwordEncoder = passwordEncoder;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }


    //==================================================================
    // USER
    //==================================================================
    public User addUser(SignUpForm signUp) {
        Optional<User> optional = userDAO.findByUsername(signUp.getUsername());
        if(optional.isPresent()) {
            throw new DataConstraintException("username", "Username already taken.");
        }
        signUp.setPassword(passwordEncoder.encode(signUp.getPassword()));
        return userDAO.add(signUp);
    }

    public User update(User user, Profile profile) {
        user.setName(profile.getName());
        userDAO.update(user);
        return user;
    }

    public Optional<User> findUserByToken(String token) {

        Optional<AuthToken> authToken = authTokenDAO.find(token);

        if(authToken.isPresent()) {
            return Optional.of(authToken.get().getUser());
        } else {
            return Optional.empty();
        }
    }

    public Optional<User> login(LoginForm login) {
        Optional<User> optionalUser = userDAO.findByUsername(login.getUsername());
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            if(passwordEncoder.matches(login.getPassword(), user.getPassword())) {
                List<AuthToken> tokens = authTokenDAO.findByUser(user);
                if(tokens.isEmpty()) {
                    AuthToken token = authTokenDAO.add(optionalUser.get());
                    optionalUser.get().setToken(token.getToken());
                    return optionalUser;
                } else {
                    optionalUser.get().setToken(tokens.get(0).getToken());
                    return optionalUser;
                }
            }
        }

        return Optional.empty();
    }
    //==================================================================
    // END USER
    //==================================================================


    public AccountSummary findAccountSummaryByUser(User user, Integer month, Integer year) {
        if(month == null || year == null) {
            LocalDate now = LocalDate.now();
            month = now.getMonthValue();
            year = now.getYear();
        }
        LOGGER.debug("Find account summary {} {}-{}", user, month, year);
        AccountSummary accountSummary = new AccountSummary();
        List<Budget> budgets = budgetDAO.findBudgets(user, month, year);

        // no budgets, first time access
        if(budgets.isEmpty()) {
            LOGGER.debug("First time access budgets {} {}-{}", user, month, year);
            Collection<Category> categories = categoryDAO.findCategories(user);
            // no categories, first time access
            if(categories.isEmpty()) {
                LOGGER.debug("Create default categories and budgets {} {}-{}", user, month, year);
                generateDefaultCategoriesAndBudgets(user, month, year);
            } else {
                LOGGER.debug("Copy budgets {} {}-{}", user, month, year);
                generateBudgets(user, month, year);
            }
            budgets = budgetDAO.findBudgets(user, month, year);
        }
        Map<Category, List<Budget>> grouped = budgets
                .stream()
                .collect(Collectors.groupingBy(Budget::getCategory));

        for(Map.Entry<Category, List<Budget>> entry: grouped.entrySet()) {
            Category category = entry.getKey();
            double budget = entry.getValue().stream().mapToDouble(Budget::getBudget).sum();
            double spent = entry.getValue().stream().mapToDouble(Budget::getSpent).sum();
            Group group = new Group(category.getId(), category.getName());
            group.setType(category.getType());
            group.setBudget(budget);
            group.setSpent(spent);
            group.setBudgets(entry.getValue());
            accountSummary.getGroups().add(group);
        }

        Collections.sort(accountSummary.getGroups(), (o1, o2) -> o1.getId().compareTo(o2.getId()));
        return accountSummary;
    }

    public UsageSummary findUsageSummaryByUser(User user, Integer month, Integer year) {

        if(month == null || year == null) {
            LocalDate now = LocalDate.now();
            month = now.getMonthValue();
            year = now.getYear();
        }

        List<Budget> budgets = budgetDAO.findBudgets(user, month, year);
        double budget =
                budgets
                        .stream()
                        .filter(p -> p.getCategory().getType() == CategoryType.EXPENSE)
                        .mapToDouble(Budget::getBudget)
                        .sum();

        double spent =
                budgets
                        .stream()
                        .filter(p -> p.getCategory().getType() == CategoryType.EXPENSE)
                        .mapToDouble(Budget::getSpent)
                        .sum();
        return new UsageSummary(budget, spent);
    }

    private void generateDefaultCategoriesAndBudgets(User user, int month, int year) {
        Collection<Category> categories = categoryDAO.addDefaultCategories(user);
        Map<String, List<Budget>> defaultBudgets = budgetDAO.findDefaultBudgets();
        Date period = Util.yearMonthDate(month, year);
        for(Category category: categories) {
            List<Budget> budgets = defaultBudgets.get(category.getName());
            if(budgets != null) {
                for(Budget budget : budgets) {
                    BudgetType budgetType = budgetTypeDAO.addBudgetType();
                    Budget newBudget = new Budget();
                    newBudget.setName(budget.getName());
                    newBudget.setPeriod(period);
                    newBudget.setCategory(category);
                    newBudget.setBudgetType(budgetType);
                    budgetDAO.addBudget(user, newBudget);
                }
            }
        }
    }

    //==================================================================
    // BUDGET
    //==================================================================

    public Budget addBudget(User user, AddBudgetForm budgetForm) {
        BudgetType budgetType = budgetTypeDAO.addBudgetType();
        Budget budget = new Budget(budgetForm);
        budget.setBudgetType(budgetType);
        return budgetDAO.addBudget(user, budget);
    }

    public Budget updateBudget(User user, UpdateBudgetForm budgetForm) {
        Budget budget = budgetDAO.findById(user, budgetForm.getId());
        budget.setName(budgetForm.getName());
        budget.setBudget(budgetForm.getBudget());
        budgetDAO.update(budget);
        return budget;
    }

    public void deleteBudget(User user, long budgetId) {
        Budget budget = budgetDAO.findById(user, budgetId);
        budgetDAO.delete(budget);
    }

    public List<Budget> findBudgetsByUser(User user) {
        return budgetDAO.findBudgets(user);
    }

    public Budget findBudgetById(User user, long budgetId) {
        return budgetDAO.findById(user, budgetId);
    }

    public List<Budget> findBudgetsByCategory(User user, long categoryId) {
        return budgetDAO.findByUserAndCategory(user, categoryId);
    }

    public List<String> findBudgetSuggestions(User user, String q) {
        return budgetDAO.findSuggestions(user, q);
    }

    private void generateBudgets(User user, int month, int year) {
        List<Budget> originalBudgets = budgetDAO.findBudgets(user);
        Date period = Util.yearMonthDate(month, year);
        for(Budget budget : originalBudgets) {
            Budget newBudget = new Budget();
            newBudget.setName(budget.getName());
            newBudget.setBudget(budget.getBudget());
            newBudget.setPeriod(period);
            newBudget.setCategory(budget.getCategory());
            newBudget.setBudgetType(budget.getBudgetType());
            budgetDAO.addBudget(user, newBudget);
        }
    }

    //==================================================================
    // END BUDGET
    //==================================================================


    //==================================================================
    // RECURRING
    //==================================================================

    public Recurring addRecurring(User user, AddRecurringForm recurringForm) {
        Budget budget = findBudgetById(user, recurringForm.getBudgetId());

        Recurring recurring = new Recurring();
        recurring.setAmount(recurringForm.getAmount());
        recurring.setRecurringType(recurringForm.getRecurringType());
        recurring.setBudgetType(budget.getBudgetType());
        return recurringDAO.addRecurring(recurring);
    }

    public List<Recurring> findRecurrings(User user) {
        List<Recurring> results = recurringDAO.findRecurrings(user);
        // TODO: fix N + 1 query but now still OK
        for (Recurring recurring : results) {
            populateRecurring(recurring);
        }
        LOGGER.debug("Found recurrings {}", results);
        return results;
    }

    public void updateRecurrings() {
        LOGGER.debug("Begin update recurrings...");
        List<Recurring> recurrings = recurringDAO.findActiveRecurrings();
        LOGGER.debug("Found {} recurring(s) item to update", recurrings.size());
        for (Recurring recurring : recurrings) {

            // budget
            Budget budget = budgetDAO.findByBudgetType(recurring.getBudgetType().getId());
            budget.setSpent(budget.getSpent() + recurring.getAmount());
            budgetDAO.update(budget);
            // end budget

            // recurring
            recurring.setLastRunAt(new Date());
            recurringDAO.update(recurring);
            // end recurring

            // transaction
            Transaction transaction = new Transaction();
            transaction.setName(budget.getName());
            transaction.setAmount(recurring.getAmount());
            transaction.setRecurring(recurring);
            transaction.setRemark(recurring.getRecurringTypeDisplay() + " recurring for " + budget.getName());
            transaction.setAuto(true);
            transaction.setBudget(budget);
            transaction.setTransactionOn(new Date());
            transactionDAO.addTransaction(transaction);
            // end transaction

        }
        LOGGER.debug("Finish update recurrings...");
    }

    private void populateRecurring(Recurring recurring) {
        Budget budget = budgetDAO.findByBudgetType(recurring.getBudgetType().getId());
        recurring.setName(budget.getName());
    }

    public void deleteRecurring(User user, long recurringId) {
        Recurring recurring = recurringDAO.find(user, recurringId);
        recurringDAO.delete(recurring);
    }
    //==================================================================
    // END RECURRING
    //==================================================================


    //==================================================================
    // TRANSACTION
    //==================================================================
    public Transaction addTransaction(User user, TransactionForm transactionForm) {

        Budget budget = budgetDAO.findById(user, transactionForm.getBudget().getId());

        // validation
        if(Boolean.TRUE.equals(transactionForm.getRecurring()) && transactionForm.getRecurringType() == null) {
            throw new DataConstraintException("recurringType", "Recurring Type is required");
        }

        Date transactionOn = transactionForm.getTransactionOn();
        if(!Util.inMonth(transactionOn, budget.getPeriod())) {
            throw new DataConstraintException("transactionOn", "Transaction Date must within " + Util.toFriendlyMonthDisplay(budget.getPeriod()) + " " + (budget.getPeriod().getYear() + 1900));
        }
        // end validation


        budget.setSpent(budget.getSpent() + transactionForm.getAmount());
        budgetDAO.update(budget);

        if(Boolean.TRUE.equals(transactionForm.getRecurring())) {
            LOGGER.debug("Add recurring {} by {}", transactionForm, user);
            Recurring recurring = new Recurring();
            recurring.setAmount(transactionForm.getAmount());
            recurring.setRecurringType(transactionForm.getRecurringType());
            recurring.setBudgetType(budget.getBudgetType());
            recurring.setLastRunAt(new Date());
            recurringDAO.addRecurring(recurring);
        }

        Transaction transaction = new Transaction();
        transaction.setName(budget.getName());
        transaction.setAmount(transactionForm.getAmount());
        transaction.setRemark(transactionForm.getRemark());
        transaction.setAuto(Boolean.TRUE.equals(transactionForm.getRecurring()));
        transaction.setTransactionOn(transactionForm.getTransactionOn());
        transaction.setBudget(transactionForm.getBudget());
        return transactionDAO.addTransaction(transaction);
    }

    public List<Transaction> findRecentTransactions(User user, Integer limit) {
        if(limit == null) {
            limit = 20;
        }
        return transactionDAO.find(user, limit);
    }

    public Transaction findTransactionById(long transactionId) {
        return transactionDAO.findById(transactionId);
    }

    public List<Transaction> findTransactions(User user) {
        return transactionDAO.find(user, 100);
    }

    public List<Transaction> findTransactionsByRecurring(User user, long recurringId) {
        return transactionDAO.findByRecurring(user, recurringId);
    }

    public List<Transaction> findTransactions(User user, SearchFilter filter) {
        LOGGER.debug("Search transactions with {}", filter);
        return transactionDAO.findTransactions(user, filter);
    }

    public List<Transaction> findTransactionsByBudget(User user, long budgetId) {
        return transactionDAO.findByBudget(user, budgetId);
    }

    public List<Point> findTransactionUsage(User user) {
        List<Point> points = new ArrayList<>();

        LocalDate lastWeek = LocalDate.now().minusWeeks(1);
        Instant instantEnd = lastWeek.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        Date start = Date.from(instantEnd);

        LocalDate now = LocalDate.now();
        Instant instantStart = now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        Date end = Date.from(instantStart);

        List<Transaction> transactions = transactionDAO.findByRange(user, start, end);

        Map<Date, List<Transaction>> groups = transactions
                .stream()
                .collect(Collectors.groupingBy(Transaction::getTransactionOn, TreeMap::new, Collectors.toList()));

        for (int i = 0; i < 8; i++) {
            LocalDate day = LocalDate.now().minusDays(i);
            Instant instantDay = day.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
            Date dayDate = Date.from(instantDay);
            groups.putIfAbsent(dayDate, Collections.emptyList());
        }

        for (Map.Entry<Date, List<Transaction>> entry : groups.entrySet()) {
            double total = entry.getValue()
                    .stream()
                    .mapToDouble(Transaction::getAmount)
                    .sum();

            LocalDate res = Util.toLocalDate(entry.getKey());
            Point point = new Point(SUMMARY_DATE_FORMATTER.format(res), entry.getKey().getTime(), total, PointType.TRANSACTIONS);
            points.add(point);
        }
        return points;
    }
    //==================================================================
    // END TRANSACTION
    //==================================================================


    //==================================================================
    // CATEGORY
    //==================================================================

    public List<Category> findCategories(User user) {
        return categoryDAO.findCategories(user);
    }

    public Category addCategory(User user, Category category) {
        return categoryDAO.addCategory(user, category);
    }

    public Category findCategoryById(long categoryId) {
        return categoryDAO.findById(categoryId);
    }

    public List<Point> findUsageByCategory(User user) {
        List<Point> points = new ArrayList<>();
        LocalDate now = LocalDate.now();
        List<Budget> budgets = budgetDAO.findBudgets(user, now.getMonthValue(), now.getYear());
        Map<Category, List<Budget>> groups = budgets
                .stream()
                .collect(Collectors.groupingBy(Budget::getCategory));
        for (Map.Entry<Category, List<Budget>> entry : groups.entrySet()) {
            double total = entry.getValue()
                    .stream()
                    .mapToDouble(Budget::getSpent)
                    .sum();
            Point point = new Point(entry.getKey().getName(), entry.getKey().getId(), total, PointType.CATEGORY);
            points.add(point);
        }

        Collections.sort(points, (p1, p2) -> Double.compare(p2.getValue(), p1.getValue()));
        return points;
    }

    public List<Point> findMonthlyTransactionUsage(User user) {
        List<Point> points = new ArrayList<>();
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusMonths(6);
        List<Budget> budgets = budgetDAO.findByRange(user, start.getMonthValue(), start.getYear(), end.getMonthValue(), end.getYear());

        // group by period
        Map<Date, List<Budget>> groups = budgets
                .stream()
                .collect(Collectors.groupingBy(Budget::getPeriod, TreeMap::new, Collectors.toList()));

        LocalDate now = LocalDate.now();
        // populate empty months, if any
        for (int i = 0; i < 6; i++) {
            LocalDate day = LocalDate.of(now.getYear(), now.getMonthValue() - i, 1);
            groups.putIfAbsent(Util.toDate(day), Collections.emptyList());
        }

        // generate points
        for (Map.Entry<Date, List<Budget>> entry : groups.entrySet()) {
            // budget
            double budget = entry.getValue()
                    .stream()
                    .mapToDouble(Budget::getBudget)
                    .sum();

            // spending
            double spending = entry.getValue()
                    .stream()
                    .filter(p -> p.getSpent() > 0)
                    .mapToDouble(Budget::getSpent)
                    .sum();

            // refund
            double refund = entry.getValue()
                    .stream()
                    .filter(p -> p.getSpent() < 0)
                    .mapToDouble(Budget::getSpent)
                    .sum();

            String month = Util.toFriendlyMonthDisplay(entry.getKey());
            Point spendingPoint = new Point(month, entry.getKey().getTime(), spending, PointType.MONTHLY_SPEND);
            Point refundPoint = new Point(month, entry.getKey().getTime(), refund, PointType.MONTHLY_REFUND);
            Point budgetPoint = new Point(month, entry.getKey().getTime(), budget, PointType.MONTHLY_BUDGET);

            points.add(spendingPoint);
            points.add(refundPoint);
            points.add(budgetPoint);
        }
        return points;
    }

    public void deleteCategory(User user, long categoryId) {
        Category category = categoryDAO.find(user, categoryId);
        categoryDAO.delete(category);
    }

    public List<String> findCategorySuggestions(User user, String q) {
        return categoryDAO.findSuggestions(user, q);
    }

    //==================================================================
    // END CATEGORY
    //==================================================================
}
