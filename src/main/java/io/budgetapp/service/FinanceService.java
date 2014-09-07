package io.budgetapp.service;

import io.budgetapp.crypto.PasswordEncoder;
import io.budgetapp.dao.AuthTokenDAO;
import io.budgetapp.dao.LedgerDAO;
import io.budgetapp.dao.RecurringDAO;
import io.budgetapp.model.AuthToken;
import io.budgetapp.model.LedgerType;
import io.budgetapp.model.Point;
import io.budgetapp.model.Transaction;
import io.budgetapp.model.form.report.SearchFilter;
import io.budgetapp.util.Util;
import io.budgetapp.application.DataConstraintException;
import io.budgetapp.dao.CategoryDAO;
import io.budgetapp.dao.LedgerTypeDAO;
import io.budgetapp.dao.TransactionDAO;
import io.budgetapp.dao.UserDAO;
import io.budgetapp.model.AccountSummary;
import io.budgetapp.model.Category;
import io.budgetapp.model.CategoryType;
import io.budgetapp.model.Group;
import io.budgetapp.model.Ledger;
import io.budgetapp.model.PointType;
import io.budgetapp.model.Recurring;
import io.budgetapp.model.UsageSummary;
import io.budgetapp.model.User;
import io.budgetapp.model.form.ledger.AddLedgerForm;
import io.budgetapp.model.form.LoginForm;
import io.budgetapp.model.form.SignUpForm;
import io.budgetapp.model.form.TransactionForm;
import io.budgetapp.model.form.ledger.UpdateLedgerForm;
import io.budgetapp.model.form.user.Profile;
import com.google.common.base.Optional;
import io.budgetapp.application.DataConstraintException;
import io.budgetapp.dao.CategoryDAO;
import io.budgetapp.dao.LedgerTypeDAO;
import io.budgetapp.dao.TransactionDAO;
import io.budgetapp.dao.UserDAO;
import io.budgetapp.model.AccountSummary;
import io.budgetapp.model.Category;
import io.budgetapp.model.CategoryType;
import io.budgetapp.model.Group;
import io.budgetapp.model.Ledger;
import io.budgetapp.model.PointType;
import io.budgetapp.model.Recurring;
import io.budgetapp.model.UsageSummary;
import io.budgetapp.model.User;
import io.budgetapp.model.form.LoginForm;
import io.budgetapp.model.form.SignUpForm;
import io.budgetapp.model.form.TransactionForm;
import io.budgetapp.model.form.ledger.AddLedgerForm;
import io.budgetapp.model.form.ledger.UpdateLedgerForm;
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
    private final LedgerDAO ledgerDAO;
    private final LedgerTypeDAO ledgerTypeDAO;
    private final CategoryDAO categoryDAO;
    private final TransactionDAO transactionDAO;
    private final RecurringDAO recurringDAO;
    private final AuthTokenDAO authTokenDAO;

    private final PasswordEncoder passwordEncoder;

    public FinanceService(SessionFactory sessionFactory, UserDAO userDAO, LedgerDAO ledgerDAO, LedgerTypeDAO ledgerTypeDAO, CategoryDAO categoryDAO, TransactionDAO transactionDAO, RecurringDAO recurringDAO, AuthTokenDAO authTokenDAO, PasswordEncoder passwordEncoder) {
        this.sessionFactory = sessionFactory;
        this.userDAO = userDAO;
        this.ledgerDAO = ledgerDAO;
        this.ledgerTypeDAO = ledgerTypeDAO;
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

    public com.google.common.base.Optional<User> findUserByToken(String token) {

        com.google.common.base.Optional<AuthToken> authToken = authTokenDAO.find(token);

        if(authToken.isPresent()) {
            return com.google.common.base.Optional.of(authToken.get().getUser());
        } else {
            return com.google.common.base.Optional.absent();
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

        return Optional.absent();
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
        List<Ledger> ledgers = ledgerDAO.findLedgers(user, month, year);

        // no ledgers, first time access
        if(ledgers.isEmpty()) {
            LOGGER.debug("First time access ledgers {} {}-{}", user, month, year);
            Collection<Category> categories = categoryDAO.findCategories(user);
            // no categories, first time access
            if(categories.isEmpty()) {
                LOGGER.debug("Create default categories and ledgers {} {}-{}", user, month, year);
                generateDefaultCategoriesAndLedgers(user, month, year);
            } else {
                LOGGER.debug("Copy ledgers {} {}-{}", user, month, year);
                generateLedgers(user, month, year);
            }
            ledgers = ledgerDAO.findLedgers(user, month, year);
        }
        Map<Category, List<Ledger>> grouped = ledgers
                .stream()
                .collect(Collectors.groupingBy(Ledger::getCategory));

        for(Map.Entry<Category, List<Ledger>> entry: grouped.entrySet()) {
            Category category = entry.getKey();
            double budget = entry.getValue().stream().mapToDouble(Ledger::getBudget).sum();
            double spent = entry.getValue().stream().mapToDouble(Ledger::getSpent).sum();
            Group group = new Group(category.getId(), category.getName());
            group.setType(category.getType());
            group.setBudget(budget);
            group.setSpent(spent);
            group.setLedgers(entry.getValue());
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

        List<Ledger> ledgers = ledgerDAO.findLedgers(user, month, year);
        double budget =
                ledgers
                        .stream()
                        .filter(p -> p.getCategory().getType() == CategoryType.EXPENSE)
                        .mapToDouble(Ledger::getBudget)
                        .sum();

        double spent =
                ledgers
                        .stream()
                        .filter(p -> p.getCategory().getType() == CategoryType.EXPENSE)
                        .mapToDouble(Ledger::getSpent)
                        .sum();
        return new UsageSummary(budget, spent);
    }

    private void generateDefaultCategoriesAndLedgers(User user, int month, int year) {
        Collection<Category> categories = categoryDAO.addDefaultCategories(user);
        Map<String, List<Ledger>> defaultLedgers = ledgerDAO.findDefaultLedgers();
        Date period = Util.yearMonthDate(month, year);
        for(Category category: categories) {
            List<Ledger> ledgers = defaultLedgers.get(category.getName());
            if(ledgers != null) {
                for(Ledger ledger: ledgers) {
                    LedgerType ledgerType = ledgerTypeDAO.addLedgerType();
                    Ledger newLedger = new Ledger();
                    newLedger.setName(ledger.getName());
                    newLedger.setPeriod(period);
                    newLedger.setCategory(category);
                    newLedger.setLedgerType(ledgerType);
                    ledgerDAO.addLedger(user, newLedger);
                }
            }
        }
    }

    //==================================================================
    // LEDGER
    //==================================================================

    public Ledger addLedger(User user, AddLedgerForm ledgerForm) {
        LedgerType ledgerType = ledgerTypeDAO.addLedgerType();
        Ledger ledger = new Ledger(ledgerForm);
        ledger.setLedgerType(ledgerType);
        return ledgerDAO.addLedger(user, ledger);
    }

    public Ledger updateLedger(User user, UpdateLedgerForm ledgerForm) {
        Ledger ledger = ledgerDAO.findById(user, ledgerForm.getId());
        ledger.setName(ledgerForm.getName());
        ledger.setBudget(ledgerForm.getBudget());
        ledgerDAO.update(ledger);
        return ledger;
    }

    public void deleteLedger(User user, long ledgerId) {
        Ledger ledger = ledgerDAO.findById(user, ledgerId);
        ledgerDAO.delete(ledger);
    }

    public List<Ledger> findLedgersByUser(User user) {
        return ledgerDAO.findLedgers(user);
    }

    public Ledger findLedgerById(User user, long ledgerId) {
        return ledgerDAO.findById(user, ledgerId);
    }

    public List<Ledger> findLedgersByCategory(User user, long categoryId) {
        return ledgerDAO.findByUserAndCategory(user, categoryId);
    }

    public List<String> findLedgerSuggestions(User user, String q) {
        return ledgerDAO.findSuggestions(user, q);
    }

    private void generateLedgers(User user, int month, int year) {
        List<Ledger> originalLedgers = ledgerDAO.findLedgers(user);
        Date period = Util.yearMonthDate(month, year);
        for(Ledger ledger: originalLedgers) {
            Ledger newLedger = new Ledger();
            newLedger.setName(ledger.getName());
            newLedger.setBudget(ledger.getBudget());
            newLedger.setPeriod(period);
            newLedger.setCategory(ledger.getCategory());
            newLedger.setLedgerType(ledger.getLedgerType());
            ledgerDAO.addLedger(user, newLedger);
        }
    }

    //==================================================================
    // END LEDGER
    //==================================================================


    //==================================================================
    // RECURRING
    //==================================================================
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

            // ledger
            Ledger ledger = ledgerDAO.findByLedgerType(recurring.getLedgerType().getId());
            ledger.setSpent(ledger.getSpent() + recurring.getAmount());
            ledgerDAO.update(ledger);
            // end ledger

            // recurring
            recurring.setLastRunAt(new Date());
            recurringDAO.update(recurring);
            // end recurring

            // transaction
            Transaction transaction = new Transaction();
            transaction.setName(ledger.getName());
            transaction.setAmount(recurring.getAmount());
            transaction.setRecurring(recurring);
            transaction.setRemark(recurring.getRecurringTypeDisplay() + " recurring for " + ledger.getName());
            transaction.setLedger(ledger);
            transaction.setTransactionOn(new Date());
            transactionDAO.addTransaction(transaction);
            // end transaction

        }
        LOGGER.debug("Finish update recurrings...");
    }

    private void populateRecurring(Recurring recurring) {
        Ledger ledger = ledgerDAO.findByLedgerType(recurring.getLedgerType().getId());
        recurring.setName(ledger.getName());
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

        Ledger ledger = ledgerDAO.findById(user, transactionForm.getLedger().getId());

        // validation
        if(Boolean.TRUE.equals(transactionForm.getRecurring()) && transactionForm.getRecurringType() == null) {
            throw new DataConstraintException("recurringType", "Recurring Type is required");
        }

        Date transactionOn = transactionForm.getTransactionOn();
        if(!Util.inMonth(transactionOn, ledger.getPeriod())) {
            throw new DataConstraintException("transactionOn", "Transaction Date must within " + Util.toFriendlyMonthDisplay(ledger.getPeriod()) + " " + (ledger.getPeriod().getYear() + 1900));
        }
        // end validation


        ledger.setSpent(ledger.getSpent() + transactionForm.getAmount());
        ledgerDAO.update(ledger);

        if(Boolean.TRUE.equals(transactionForm.getRecurring())) {
            LOGGER.debug("Add recurring {} by {}", transactionForm, user);
            Recurring recurring = new Recurring();
            recurring.setAmount(transactionForm.getAmount());
            recurring.setRecurringType(transactionForm.getRecurringType());
            recurring.setLedgerType(ledger.getLedgerType());
            recurringDAO.addRecurring(recurring);
        }

        Transaction transaction = new Transaction();
        transaction.setName(ledger.getName());
        transaction.setAmount(transactionForm.getAmount());
        transaction.setRemark(transactionForm.getRemark());
        transaction.setTransactionOn(transactionForm.getTransactionOn());
        transaction.setLedger(transactionForm.getLedger());
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

    public List<Transaction> findTransactionsByLedger(User user, long ledgerId) {
        return transactionDAO.findByLedger(user, ledgerId);
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
        List<Ledger> ledgers = ledgerDAO.findLedgers(user, now.getMonthValue(), now.getYear());
        Map<Category, List<Ledger>> groups = ledgers
                .stream()
                .collect(Collectors.groupingBy(Ledger::getCategory));
        for (Map.Entry<Category, List<Ledger>> entry : groups.entrySet()) {
            double total = entry.getValue()
                    .stream()
                    .mapToDouble(Ledger::getSpent)
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
        List<Ledger> ledgers = ledgerDAO.findByRange(user, start.getMonthValue(), start.getYear(), end.getMonthValue(), end.getYear());

        // group by period
        Map<Date, List<Ledger>> groups = ledgers
                .stream()
                .collect(Collectors.groupingBy(Ledger::getPeriod, TreeMap::new, Collectors.toList()));

        LocalDate now = LocalDate.now();
        // populate empty months, if any
        for (int i = 0; i < 6; i++) {
            LocalDate day = LocalDate.of(now.getYear(), now.getMonthValue() - i, 1);
            groups.putIfAbsent(Util.toDate(day), Collections.emptyList());
        }

        // generate points
        for (Map.Entry<Date, List<Ledger>> entry : groups.entrySet()) {
            // budget
            double budget = entry.getValue()
                    .stream()
                    .mapToDouble(Ledger::getBudget)
                    .sum();

            // spending
            double spending = entry.getValue()
                    .stream()
                    .filter(p -> p.getSpent() > 0)
                    .mapToDouble(Ledger::getSpent)
                    .sum();

            // refund
            double refund = entry.getValue()
                    .stream()
                    .filter(p -> p.getSpent() < 0)
                    .mapToDouble(Ledger::getSpent)
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
