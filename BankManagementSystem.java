import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.*;
import java.util.*;
import java.util.List;

// ─── Domain Models ───────────────────────────────────────────────────────────

class Transaction {
    enum Type { DEPOSIT, WITHDRAWAL, TRANSFER }
    private final String id;
    private final Type type;
    private final double amount;
    private final String description;
    private final Date date;
    private final double balanceAfter;

    public Transaction(Type type, double amount, String description, double balanceAfter) {
        this.id = "TXN" + System.currentTimeMillis();
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.date = new Date();
        this.balanceAfter = balanceAfter;
    }

    public Type getType()          { return type; }
    public double getAmount()      { return amount; }
    public String getDescription() { return description; }
    public Date getDate()          { return date; }
    public double getBalanceAfter(){ return balanceAfter; }
    public String getId()          { return id; }
}

class Account {
    enum AccountType { SAVINGS, CURRENT, FIXED_DEPOSIT }
    private final String accountNumber;
    private final String holderName;
    private final AccountType accountType;
    private double balance;
    private final String pin;
    private final Date createdDate;
    private final List<Transaction> transactions = new ArrayList<>();
    private boolean active = true;

    public Account(String holderName, AccountType accountType, double initialDeposit, String pin) {
        this.accountNumber = "ACC" + (100000 + new Random().nextInt(899999));
        this.holderName    = holderName;
        this.accountType   = accountType;
        this.balance       = initialDeposit;
        this.pin           = pin;
        this.createdDate   = new Date();
        if (initialDeposit > 0)
            transactions.add(new Transaction(Transaction.Type.DEPOSIT, initialDeposit, "Initial deposit", initialDeposit));
    }

    public boolean deposit(double amount) {
        if (amount <= 0 || !active) return false;
        balance += amount;
        transactions.add(new Transaction(Transaction.Type.DEPOSIT, amount, "Cash deposit", balance));
        return true;
    }

    public boolean withdraw(double amount) {
        if (amount <= 0 || amount > balance || !active) return false;
        balance -= amount;
        transactions.add(new Transaction(Transaction.Type.WITHDRAWAL, amount, "Cash withdrawal", balance));
        return true;
    }

    public boolean verifyPin(String p) { return pin.equals(p); }

    // Getters
    public String getAccountNumber() { return accountNumber; }
    public String getHolderName()    { return holderName; }
    public AccountType getAccountType() { return accountType; }
    public double getBalance()       { return balance; }
    public Date getCreatedDate()     { return createdDate; }
    public List<Transaction> getTransactions() { return transactions; }
    public boolean isActive()        { return active; }
    public void setActive(boolean a) { active = a; }
    public void addTransaction(Transaction t) { transactions.add(t); }
    public void setBalance(double b) { balance = b; }
}

class Bank {
    private final Map<String, Account> accounts = new LinkedHashMap<>();

    public Account createAccount(String name, Account.AccountType type, double deposit, String pin) {
        Account acc = new Account(name, type, deposit, pin);
        accounts.put(acc.getAccountNumber(), acc);
        return acc;
    }

    public Account findAccount(String number) { return accounts.get(number); }

    public boolean transfer(Account from, Account to, double amount) {
        if (from.getBalance() < amount || amount <= 0) return false;
        from.setBalance(from.getBalance() - amount);
        to.setBalance(to.getBalance() + amount);
        from.addTransaction(new Transaction(Transaction.Type.TRANSFER, amount,
                "Transfer to " + to.getAccountNumber(), from.getBalance()));
        to.addTransaction(new Transaction(Transaction.Type.TRANSFER, amount,
                "Transfer from " + from.getAccountNumber(), to.getBalance()));
        return true;
    }

    public Collection<Account> getAllAccounts() { return accounts.values(); }

    public double getTotalDeposits() {
        return accounts.values().stream().mapToDouble(Account::getBalance).sum();
    }

    public long getActiveCount() {
        return accounts.values().stream().filter(Account::isActive).count();
    }
}

// ─── UI Helpers ───────────────────────────────────────────────────────────────

class Theme {
    static final Color BG         = new Color(0x0D1117);
    static final Color SURFACE    = new Color(0x161B22);
    static final Color CARD       = new Color(0x1C2333);
    static final Color BORDER     = new Color(0x30363D);
    static final Color ACCENT     = new Color(0x238636);
    static final Color ACCENT2    = new Color(0x1F6FEB);
    static final Color DANGER     = new Color(0xDA3633);
    static final Color WARNING    = new Color(0xD29922);
    static final Color TEXT       = new Color(0xE6EDF3);
    static final Color TEXT_MUTED = new Color(0x8B949E);
    static final Color GREEN      = new Color(0x3FB950);
    static final Font  FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    static final Font  FONT_BODY  = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font  FONT_BOLD  = new Font("Segoe UI", Font.BOLD, 13);
    static final Font  FONT_MONO  = new Font("Consolas", Font.PLAIN, 13);
    static final Font  FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
}

class StyledButton extends JButton {
    private final Color base;
    private boolean hovered = false;

    public StyledButton(String text, Color color) {
        super(text);
        this.base = color;
        setFont(Theme.FONT_BOLD);
        setForeground(Color.WHITE);
        setBackground(color);
        setBorderPainted(false);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
            public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color c = hovered ? base.brighter() : base;
        g2.setColor(c);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
        super.paintComponent(g);
        g2.dispose();
    }
}

class Card extends JPanel {
    public Card() {
        setBackground(Theme.CARD);
        setBorder(new CompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(16, 18, 16, 18)
        ));
    }
}

class StyledField extends JTextField {
    public StyledField(int cols) {
        super(cols);
        setFont(Theme.FONT_BODY);
        setBackground(Theme.SURFACE);
        setForeground(Theme.TEXT);
        setCaretColor(Theme.TEXT);
        setBorder(new CompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));
    }
}

class StyledPasswordField extends JPasswordField {
    public StyledPasswordField(int cols) {
        super(cols);
        setFont(Theme.FONT_BODY);
        setBackground(Theme.SURFACE);
        setForeground(Theme.TEXT);
        setCaretColor(Theme.TEXT);
        setBorder(new CompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));
    }
}

class StyledCombo extends JComboBox<String> {
    public StyledCombo(String[] items) {
        super(items);
        setFont(Theme.FONT_BODY);
        setBackground(Theme.SURFACE);
        setForeground(Theme.TEXT);
        setBorder(new LineBorder(Theme.BORDER, 1, true));
        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean foc) {
                super.getListCellRendererComponent(l, v, i, sel, foc);
                setBackground(sel ? Theme.ACCENT2 : Theme.SURFACE);
                setForeground(Theme.TEXT);
                setBorder(new EmptyBorder(6, 10, 6, 10));
                return this;
            }
        });
    }
}

class StatCard extends JPanel {
    private final String label;
    private String value;
    private final Color accent;

    public StatCard(String label, String value, Color accent) {
        this.label = label; this.value = value; this.accent = accent;
        setBackground(Theme.CARD);
        setBorder(new CompoundBorder(new LineBorder(Theme.BORDER, 1, true), new EmptyBorder(14, 16, 14, 16)));
        setPreferredSize(new Dimension(180, 80));
    }

    public void setValue(String v) { this.value = v; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // accent bar
        g2.setColor(accent);
        g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
        // label
        g2.setFont(Theme.FONT_SMALL);
        g2.setColor(Theme.TEXT_MUTED);
        g2.drawString(label, 16, 24);
        // value
        g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
        g2.setColor(Theme.TEXT);
        g2.drawString(value, 16, 52);
        g2.dispose();
    }
}

// ─── Main Application ─────────────────────────────────────────────────────────

public class BankManagementSystem extends JFrame {

    private final Bank bank = new Bank();
    private final DecimalFormat currFmt = new DecimalFormat("₹#,##0.00");
    private final SimpleDateFormat dateFmt = new SimpleDateFormat("dd MMM yyyy HH:mm");

    // Stat cards
    private StatCard statAccounts, statBalance, statTxns;

    // Tables
    private DefaultTableModel accountsModel, txnModel;

    // ── Constructor ──────────────────────────────────────────────────────────

    public BankManagementSystem() {
        super("SecureBank — Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG);
        applyGlobalDefaults();
        seedDemoData();

        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        add(buildMain(), BorderLayout.CENTER);

        refreshStats();
        setVisible(true);
    }

    // ── Global Defaults ──────────────────────────────────────────────────────

    private void applyGlobalDefaults() {
        UIManager.put("Panel.background", Theme.BG);
        UIManager.put("Label.foreground", Theme.TEXT);
        UIManager.put("Label.font", Theme.FONT_BODY);
        UIManager.put("OptionPane.background", Theme.SURFACE);
        UIManager.put("OptionPane.messageForeground", Theme.TEXT);
        UIManager.put("Button.background", Theme.ACCENT);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("TextField.background", Theme.SURFACE);
        UIManager.put("TextField.foreground", Theme.TEXT);
        UIManager.put("TextArea.background", Theme.SURFACE);
        UIManager.put("TextArea.foreground", Theme.TEXT);
        UIManager.put("ComboBox.background", Theme.SURFACE);
        UIManager.put("ComboBox.foreground", Theme.TEXT);
        UIManager.put("ScrollBar.background", Theme.SURFACE);
        UIManager.put("ScrollBar.thumb", Theme.BORDER);
    }

    // ── Demo seed ────────────────────────────────────────────────────────────

    private void seedDemoData() {
        Account a1 = bank.createAccount("Arjun Sharma",    Account.AccountType.SAVINGS,       50000, "1234");
        Account a2 = bank.createAccount("Priya Mehta",     Account.AccountType.CURRENT,       150000, "5678");
        Account a3 = bank.createAccount("Ravi Kumar",      Account.AccountType.FIXED_DEPOSIT, 200000, "9012");
        a1.deposit(10000); a1.withdraw(3000);
        a2.deposit(25000);
        bank.transfer(a2, a1, 5000);
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setBackground(Theme.SURFACE);
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 0, 1, Theme.BORDER),
            new EmptyBorder(20, 0, 20, 0)
        ));
        side.setPreferredSize(new Dimension(220, 0));

        // Logo
        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 10));
        logo.setOpaque(false);
        JLabel icon = new JLabel("🏦");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        JLabel title = new JLabel("<html><b style='font-size:14px'>SecureBank</b><br><span style='color:#8B949E;font-size:10px'>Management System</span></html>");
        title.setForeground(Theme.TEXT);
        logo.add(icon); logo.add(title);
        side.add(logo);
        side.add(makeSep());

        // Nav buttons
        String[][] navItems = {
            {"📊", "Dashboard"},
            {"➕", "New Account"},
            {"💰", "Deposit"},
            {"💸", "Withdraw"},
            {"🔄", "Transfer"},
            {"📋", "All Accounts"},
            {"🗑️",  "Close Account"},
        };

        Runnable[] actions = {
            this::showDashboard,
            this::showNewAccount,
            this::showDeposit,
            this::showWithdraw,
            this::showTransfer,
            this::showAllAccounts,
            this::showCloseAccount,
        };

        for (int i = 0; i < navItems.length; i++) {
            side.add(buildNavBtn(navItems[i][0], navItems[i][1], actions[i]));
        }

        side.add(Box.createVerticalGlue());
        side.add(makeSep());
        JLabel ver = new JLabel("  v1.0  •  Java Swing");
        ver.setForeground(Theme.TEXT_MUTED);
        ver.setFont(Theme.FONT_SMALL);
        ver.setAlignmentX(Component.LEFT_ALIGNMENT);
        ver.setBorder(new EmptyBorder(8, 18, 0, 0));
        side.add(ver);
        return side;
    }

    private JPanel makeSep() {
        JPanel sep = new JPanel();
        sep.setOpaque(false);
        sep.setPreferredSize(new Dimension(220, 1));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setBackground(Theme.BORDER);
        sep.setBorder(new MatteBorder(1, 0, 0, 0, Theme.BORDER));
        return sep;
    }

    private JPanel buildNavBtn(String icon, String label, Runnable action) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 10));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel ico  = new JLabel(icon);
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        JLabel lbl  = new JLabel(label);
        lbl.setForeground(Theme.TEXT);
        lbl.setFont(Theme.FONT_BODY);

        p.add(ico); p.add(lbl);
        p.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e)  { action.run(); }
            public void mouseEntered(MouseEvent e)  { p.setBackground(Theme.CARD); p.setOpaque(true); }
            public void mouseExited(MouseEvent e)   { p.setOpaque(false); p.repaint(); }
        });
        return p;
    }

    // ── Main content area ────────────────────────────────────────────────────

    private final JPanel mainPanel = new JPanel(new BorderLayout());

    private JPanel buildMain() {
        mainPanel.setBackground(Theme.BG);
        showDashboard();
        return mainPanel;
    }

    private void setContent(JPanel p) {
        mainPanel.removeAll();
        mainPanel.add(p, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // ── Dashboard ─────────────────────────────────────────────────────────────

    private void showDashboard() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(Theme.BG);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        // Header
        JLabel h = new JLabel("Dashboard Overview");
        h.setFont(Theme.FONT_TITLE);
        h.setForeground(Theme.TEXT);
        panel.add(h, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(new EmptyBorder(20, 0, 0, 0));

        // Stats row
        JPanel stats = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        stats.setOpaque(false);
        statAccounts = new StatCard("Total Accounts",  "0",  Theme.ACCENT2);
        statBalance  = new StatCard("Total Deposits",  "₹0", Theme.ACCENT);
        statTxns     = new StatCard("All Transactions","0",  Theme.WARNING);
        stats.add(statAccounts); stats.add(statBalance); stats.add(statTxns);
        center.add(stats);
        center.add(Box.createVerticalStrut(22));

        // Accounts table
        JLabel tbl = new JLabel("Account Summary");
        tbl.setFont(Theme.FONT_BOLD);
        tbl.setForeground(Theme.TEXT_MUTED);
        center.add(tbl);
        center.add(Box.createVerticalStrut(8));
        center.add(buildAccountsTable());

        panel.add(center, BorderLayout.CENTER);
        setContent(panel);
        refreshStats();
    }

    private JScrollPane buildAccountsTable() {
        String[] cols = {"Account No.", "Holder Name", "Type", "Balance", "Status", "Created"};
        accountsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styleTable(new JTable(accountsModel));
        refreshAccountsTable();
        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(Theme.SURFACE);
        sp.setBackground(Theme.SURFACE);
        sp.setBorder(new LineBorder(Theme.BORDER, 1));
        return sp;
    }

    private JTable styleTable(JTable table) {
        table.setBackground(Theme.SURFACE);
        table.setForeground(Theme.TEXT);
        table.setFont(Theme.FONT_BODY);
        table.setGridColor(Theme.BORDER);
        table.setRowHeight(32);
        table.setSelectionBackground(Theme.ACCENT2);
        table.setSelectionForeground(Color.WHITE);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();
        header.setBackground(Theme.CARD);
        header.setForeground(Theme.TEXT_MUTED);
        header.setFont(Theme.FONT_BOLD);
        header.setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBackground(sel ? Theme.ACCENT2 : (row % 2 == 0 ? Theme.SURFACE : Theme.CARD));
                setForeground(sel ? Color.WHITE : Theme.TEXT);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                // Color code balance column
                if (!sel && col == 3 && v != null) setForeground(Theme.GREEN);
                if (!sel && col == 4 && v != null) {
                    setForeground("Active".equals(v) ? Theme.GREEN : Theme.DANGER);
                }
                return this;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) table.getColumnModel().getColumn(i).setCellRenderer(r);
        return table;
    }

    private void refreshAccountsTable() {
        if (accountsModel == null) return;
        accountsModel.setRowCount(0);
        for (Account a : bank.getAllAccounts()) {
            accountsModel.addRow(new Object[]{
                a.getAccountNumber(),
                a.getHolderName(),
                a.getAccountType().name().replace("_", " "),
                currFmt.format(a.getBalance()),
                a.isActive() ? "Active" : "Closed",
                dateFmt.format(a.getCreatedDate())
            });
        }
    }

    private void refreshStats() {
        if (statAccounts == null) return;
        int txnCount = bank.getAllAccounts().stream().mapToInt(a -> a.getTransactions().size()).sum();
        statAccounts.setValue(String.valueOf(bank.getActiveCount()));
        statBalance.setValue(currFmt.format(bank.getTotalDeposits()));
        statTxns.setValue(String.valueOf(txnCount));
        refreshAccountsTable();
    }

    // ── New Account ───────────────────────────────────────────────────────────

    private void showNewAccount() {
        JPanel panel = formPanel("New Account", "➕");
        JPanel form = new Card();
        form.setLayout(new GridBagLayout());
        GridBagConstraints gbc = formGbc();

        JTextField nameField   = new StyledField(20);
        StyledCombo typeCombo  = new StyledCombo(new String[]{"SAVINGS", "CURRENT", "FIXED_DEPOSIT"});
        JTextField depositField= new StyledField(20);
        JPasswordField pinField= new StyledPasswordField(20);
        JPasswordField pin2    = new StyledPasswordField(20);

        addFormRow(form, gbc, "Account Holder Name", nameField);
        addFormRow(form, gbc, "Account Type",        typeCombo);
        addFormRow(form, gbc, "Initial Deposit (₹)", depositField);
        addFormRow(form, gbc, "Set PIN (4 digits)",  pinField);
        addFormRow(form, gbc, "Confirm PIN",         pin2);

        StyledButton btn = new StyledButton("Create Account", Theme.ACCENT);
        gbc.gridx = 1; gbc.gridy++; gbc.anchor = GridBagConstraints.WEST;
        form.add(btn, gbc);

        btn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String dep  = depositField.getText().trim();
            String p1   = new String(pinField.getPassword());
            String p2   = new String(pin2.getPassword());

            if (name.isEmpty() || dep.isEmpty() || p1.isEmpty()) {
                error("All fields are required."); return;
            }
            if (!p1.equals(p2)) { error("PINs do not match."); return; }
            if (!p1.matches("\\d{4}")) { error("PIN must be exactly 4 digits."); return; }

            double amount;
            try { amount = Double.parseDouble(dep); } catch (NumberFormatException ex) {
                error("Invalid deposit amount."); return;
            }
            if (amount < 0) { error("Deposit must be non-negative."); return; }

            Account.AccountType type = Account.AccountType.valueOf((String) typeCombo.getSelectedItem());
            Account acc = bank.createAccount(name, type, amount, p1);
            refreshStats();
            success("Account created successfully!\n\nAccount Number: " + acc.getAccountNumber()
                    + "\nHolder: " + acc.getHolderName()
                    + "\nBalance: " + currFmt.format(acc.getBalance()));
            nameField.setText(""); depositField.setText(""); pinField.setText(""); pin2.setText("");
        });

        panel.add(form, BorderLayout.CENTER);
        setContent(panel);
    }

    // ── Deposit ───────────────────────────────────────────────────────────────

    private void showDeposit() {
        JPanel panel = formPanel("Deposit Funds", "💰");
        JPanel form  = new Card();
        form.setLayout(new GridBagLayout());
        GridBagConstraints gbc = formGbc();

        JTextField accField    = new StyledField(20);
        JTextField amountField = new StyledField(20);
        JLabel balLbl = infoLabel("Enter account number to check balance");

        accField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                Account a = bank.findAccount(accField.getText().trim());
                if (a != null) balLbl.setText("Current Balance: " + currFmt.format(a.getBalance())
                        + "  •  " + a.getHolderName());
                else balLbl.setText("Account not found");
            }
        });

        addFormRow(form, gbc, "Account Number", accField);
        addFormRow(form, gbc, "Amount (₹)",     amountField);
        gbc.gridx = 1; gbc.gridy++; form.add(balLbl, gbc);

        StyledButton btn = new StyledButton("Deposit", Theme.ACCENT);
        gbc.gridy++; form.add(btn, gbc);

        btn.addActionListener(e -> {
            Account a = bank.findAccount(accField.getText().trim());
            if (a == null) { error("Account not found."); return; }
            if (!a.isActive()) { error("Account is closed."); return; }
            try {
                double amt = Double.parseDouble(amountField.getText().trim());
                if (a.deposit(amt)) {
                    refreshStats();
                    balLbl.setText("New Balance: " + currFmt.format(a.getBalance()));
                    success("Deposited " + currFmt.format(amt) + " to " + a.getHolderName() + "'s account.");
                    amountField.setText("");
                } else error("Invalid amount.");
            } catch (NumberFormatException ex) { error("Enter a valid amount."); }
        });

        panel.add(form, BorderLayout.CENTER);
        setContent(panel);
    }

    // ── Withdraw ──────────────────────────────────────────────────────────────

    private void showWithdraw() {
        JPanel panel = formPanel("Withdraw Funds", "💸");
        JPanel form  = new Card();
        form.setLayout(new GridBagLayout());
        GridBagConstraints gbc = formGbc();

        JTextField accField    = new StyledField(20);
        JPasswordField pinField= new StyledPasswordField(20);
        JTextField amountField = new StyledField(20);
        JLabel balLbl = infoLabel("Enter account number to check balance");

        accField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                Account a = bank.findAccount(accField.getText().trim());
                if (a != null) balLbl.setText("Current Balance: " + currFmt.format(a.getBalance())
                        + "  •  " + a.getHolderName());
                else balLbl.setText("Account not found");
            }
        });

        addFormRow(form, gbc, "Account Number", accField);
        addFormRow(form, gbc, "PIN",             pinField);
        addFormRow(form, gbc, "Amount (₹)",      amountField);
        gbc.gridx = 1; gbc.gridy++; form.add(balLbl, gbc);

        StyledButton btn = new StyledButton("Withdraw", Theme.DANGER);
        gbc.gridy++; form.add(btn, gbc);

        btn.addActionListener(e -> {
            Account a = bank.findAccount(accField.getText().trim());
            if (a == null) { error("Account not found."); return; }
            if (!a.isActive()) { error("Account is closed."); return; }
            if (!a.verifyPin(new String(pinField.getPassword()))) { error("Incorrect PIN."); return; }
            try {
                double amt = Double.parseDouble(amountField.getText().trim());
                if (a.withdraw(amt)) {
                    refreshStats();
                    balLbl.setText("New Balance: " + currFmt.format(a.getBalance()));
                    success("Withdrew " + currFmt.format(amt) + " from " + a.getHolderName() + "'s account.");
                    amountField.setText(""); pinField.setText("");
                } else error("Insufficient funds or invalid amount.");
            } catch (NumberFormatException ex) { error("Enter a valid amount."); }
        });

        panel.add(form, BorderLayout.CENTER);
        setContent(panel);
    }

    // ── Transfer ──────────────────────────────────────────────────────────────

    private void showTransfer() {
        JPanel panel = formPanel("Fund Transfer", "🔄");
        JPanel form  = new Card();
        form.setLayout(new GridBagLayout());
        GridBagConstraints gbc = formGbc();

        JTextField fromField   = new StyledField(20);
        JPasswordField pinField= new StyledPasswordField(20);
        JTextField toField     = new StyledField(20);
        JTextField amountField = new StyledField(20);
        JLabel info = infoLabel("Fill details to transfer funds securely");

        addFormRow(form, gbc, "From Account",  fromField);
        addFormRow(form, gbc, "PIN",           pinField);
        addFormRow(form, gbc, "To Account",    toField);
        addFormRow(form, gbc, "Amount (₹)",    amountField);
        gbc.gridx = 1; gbc.gridy++; form.add(info, gbc);

        StyledButton btn = new StyledButton("Transfer Now", Theme.ACCENT2);
        gbc.gridy++; form.add(btn, gbc);

        btn.addActionListener(e -> {
            Account from = bank.findAccount(fromField.getText().trim());
            Account to   = bank.findAccount(toField.getText().trim());
            if (from == null) { error("Source account not found."); return; }
            if (to == null)   { error("Destination account not found."); return; }
            if (from == to)   { error("Cannot transfer to the same account."); return; }
            if (!from.isActive()) { error("Source account is closed."); return; }
            if (!to.isActive())   { error("Destination account is closed."); return; }
            if (!from.verifyPin(new String(pinField.getPassword()))) { error("Incorrect PIN."); return; }
            try {
                double amt = Double.parseDouble(amountField.getText().trim());
                if (bank.transfer(from, to, amt)) {
                    refreshStats();
                    info.setText("✓ Transferred " + currFmt.format(amt) + " → " + to.getHolderName());
                    success("Transfer successful!\n" + currFmt.format(amt) + " sent to " + to.getHolderName());
                    amountField.setText(""); pinField.setText("");
                } else error("Transfer failed. Check balance.");
            } catch (NumberFormatException ex) { error("Enter a valid amount."); }
        });

        panel.add(form, BorderLayout.CENTER);
        setContent(panel);
    }

    // ── All Accounts ──────────────────────────────────────────────────────────

    private void showAllAccounts() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(Theme.BG);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel h = new JLabel("📋  All Accounts");
        h.setFont(Theme.FONT_TITLE);
        h.setForeground(Theme.TEXT);
        top.add(h, BorderLayout.WEST);

        // Search
        JTextField search = new StyledField(18);
        search.setToolTipText("Search by name or account number");
        top.add(search, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);

        String[] cols = {"Account No.", "Holder Name", "Type", "Balance", "Transactions", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styleTable(new JTable(model));
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) showTransactionHistory((String) model.getValueAt(row, 0));
                }
            }
        });

        Runnable populate = () -> {
            String q = search.getText().toLowerCase();
            model.setRowCount(0);
            for (Account a : bank.getAllAccounts()) {
                if (q.isEmpty() || a.getHolderName().toLowerCase().contains(q)
                        || a.getAccountNumber().toLowerCase().contains(q)) {
                    model.addRow(new Object[]{
                        a.getAccountNumber(), a.getHolderName(),
                        a.getAccountType().name().replace("_", " "),
                        currFmt.format(a.getBalance()),
                        a.getTransactions().size(),
                        a.isActive() ? "Active" : "Closed"
                    });
                }
            }
        };
        populate.run();
        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { populate.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { populate.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e){ populate.run(); }
        });

        JLabel hint = new JLabel("Double-click a row to view transaction history");
        hint.setForeground(Theme.TEXT_MUTED);
        hint.setFont(Theme.FONT_SMALL);

        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(Theme.SURFACE);
        sp.setBorder(new LineBorder(Theme.BORDER, 1));

        panel.add(sp,   BorderLayout.CENTER);
        panel.add(hint, BorderLayout.SOUTH);
        setContent(panel);
    }

    // ── Transaction History ───────────────────────────────────────────────────

    private void showTransactionHistory(String accNo) {
        Account a = bank.findAccount(accNo);
        if (a == null) return;

        JDialog dlg = new JDialog(this, "Transactions — " + a.getHolderName(), true);
        dlg.setSize(680, 480);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(Theme.BG);
        dlg.setLayout(new BorderLayout(0, 12));

        JLabel hdr = new JLabel("  " + a.getAccountNumber() + "  •  " + currFmt.format(a.getBalance()));
        hdr.setFont(Theme.FONT_BOLD);
        hdr.setForeground(Theme.GREEN);
        hdr.setBorder(new EmptyBorder(14, 14, 0, 14));
        dlg.add(hdr, BorderLayout.NORTH);

        String[] cols = {"Date", "Type", "Amount", "Balance After", "Description"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        List<Transaction> txns = a.getTransactions();
        for (int i = txns.size() - 1; i >= 0; i--) {
            Transaction t = txns.get(i);
            model.addRow(new Object[]{
                dateFmt.format(t.getDate()),
                t.getType().name(),
                currFmt.format(t.getAmount()),
                currFmt.format(t.getBalanceAfter()),
                t.getDescription()
            });
        }

        JTable table = styleTable(new JTable(model));
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(sel ? Theme.ACCENT2 : (r % 2 == 0 ? Theme.SURFACE : Theme.CARD));
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!sel) {
                    String s = String.valueOf(v);
                    setForeground(s.equals("DEPOSIT") ? Theme.GREEN : s.equals("WITHDRAWAL") ? Theme.DANGER : Theme.ACCENT2);
                }
                return this;
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(Theme.SURFACE);
        sp.setBorder(new CompoundBorder(new EmptyBorder(0, 14, 14, 14), new LineBorder(Theme.BORDER, 1)));
        dlg.add(sp, BorderLayout.CENTER);
        dlg.setVisible(true);
    }

    // ── Close Account ─────────────────────────────────────────────────────────

    private void showCloseAccount() {
        JPanel panel = formPanel("Close Account", "🗑️");
        JPanel form  = new Card();
        form.setLayout(new GridBagLayout());
        GridBagConstraints gbc = formGbc();

        JTextField accField    = new StyledField(20);
        JPasswordField pinField= new StyledPasswordField(20);
        JLabel infoLbl = infoLabel("Enter account number and PIN to close");

        accField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                Account a = bank.findAccount(accField.getText().trim());
                if (a != null) infoLbl.setText(a.getHolderName() + "  •  Balance: " + currFmt.format(a.getBalance()));
                else infoLbl.setText("Account not found");
            }
        });

        addFormRow(form, gbc, "Account Number", accField);
        addFormRow(form, gbc, "PIN",             pinField);
        gbc.gridx = 1; gbc.gridy++; form.add(infoLbl, gbc);

        JLabel warn = new JLabel("⚠  This action cannot be undone.");
        warn.setForeground(Theme.DANGER);
        warn.setFont(Theme.FONT_SMALL);
        gbc.gridy++; form.add(warn, gbc);

        StyledButton btn = new StyledButton("Close Account", Theme.DANGER);
        gbc.gridy++; form.add(btn, gbc);

        btn.addActionListener(e -> {
            Account a = bank.findAccount(accField.getText().trim());
            if (a == null) { error("Account not found."); return; }
            if (!a.isActive()) { error("Account is already closed."); return; }
            if (!a.verifyPin(new String(pinField.getPassword()))) { error("Incorrect PIN."); return; }

            int confirm = JOptionPane.showConfirmDialog(this,
                "Close account " + a.getAccountNumber() + " of " + a.getHolderName() + "?\n"
                        + "Remaining balance: " + currFmt.format(a.getBalance()),
                "Confirm Close", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                a.setActive(false);
                refreshStats();
                success("Account " + a.getAccountNumber() + " has been closed.");
                accField.setText(""); pinField.setText("");
                infoLbl.setText("Account closed.");
            }
        });

        panel.add(form, BorderLayout.CENTER);
        setContent(panel);
    }

    // ── Form helpers ──────────────────────────────────────────────────────────

    private JPanel formPanel(String title, String icon) {
        JPanel p = new JPanel(new BorderLayout(0, 20));
        p.setBackground(Theme.BG);
        p.setBorder(new EmptyBorder(28, 28, 28, 28));
        JLabel h = new JLabel(icon + "  " + title);
        h.setFont(Theme.FONT_TITLE);
        h.setForeground(Theme.TEXT);
        p.add(h, BorderLayout.NORTH);
        return p;
    }

    private GridBagConstraints formGbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.gridy = 0;
        g.anchor = GridBagConstraints.WEST;
        return g;
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, String label, JComponent field) {
        gbc.gridx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setForeground(Theme.TEXT_MUTED);
        lbl.setFont(Theme.FONT_SMALL);
        form.add(lbl, gbc);
        gbc.gridx = 1;
        field.setPreferredSize(new Dimension(260, 36));
        form.add(field, gbc);
        gbc.gridy++;
    }

    private JLabel infoLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Theme.TEXT_MUTED);
        l.setFont(Theme.FONT_SMALL);
        return l;
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void success(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    } 

    // ── Entry point ───────────────────────────────────────────────────────────

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(BankManagementSystem::new);
    }
}